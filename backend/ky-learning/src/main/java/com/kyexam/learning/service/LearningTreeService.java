package com.kyexam.learning.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kyexam.common.enums.LearningStatus;
import com.kyexam.common.exception.BusinessException;
import com.kyexam.learning.dto.LearningNodeConnectionRequest;
import com.kyexam.learning.dto.LearningNodeConnectionView;
import com.kyexam.learning.dto.LearningNodeCreateRequest;
import com.kyexam.learning.dto.LearningNodeTagRequest;
import com.kyexam.learning.dto.LearningNodeTagView;
import com.kyexam.learning.dto.LearningNodeTree;
import com.kyexam.learning.dto.LearningNodeUpdateRequest;
import com.kyexam.learning.entity.LearningNodeConnection;
import com.kyexam.learning.entity.LearningNodeTag;
import com.kyexam.learning.entity.LearningReflection;
import com.kyexam.learning.entity.UserSyllabusNode;
import com.kyexam.learning.mapper.LearningNodeConnectionMapper;
import com.kyexam.learning.mapper.LearningNodeTagMapper;
import com.kyexam.learning.mapper.LearningReflectionMapper;
import com.kyexam.learning.mapper.UserSyllabusNodeMapper;
import com.kyexam.syllabus.entity.SyllabusNode;
import com.kyexam.syllabus.service.SyllabusService;
import com.kyexam.system.security.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LearningTreeService {
    private final UserSyllabusNodeMapper userSyllabusNodeMapper;
    private final LearningNodeTagMapper learningNodeTagMapper;
    private final LearningNodeConnectionMapper learningNodeConnectionMapper;
    private final LearningReflectionMapper learningReflectionMapper;
    private final SyllabusService syllabusService;
    private final StudyTreeService studyTreeService;

    public LearningTreeService(
            UserSyllabusNodeMapper userSyllabusNodeMapper,
            LearningNodeTagMapper learningNodeTagMapper,
            LearningNodeConnectionMapper learningNodeConnectionMapper,
            LearningReflectionMapper learningReflectionMapper,
            SyllabusService syllabusService,
            StudyTreeService studyTreeService) {
        this.userSyllabusNodeMapper = userSyllabusNodeMapper;
        this.learningNodeTagMapper = learningNodeTagMapper;
        this.learningNodeConnectionMapper = learningNodeConnectionMapper;
        this.learningReflectionMapper = learningReflectionMapper;
        this.syllabusService = syllabusService;
        this.studyTreeService = studyTreeService;
    }

    public List<LearningNodeTree> tree(Long treeId) {
        Long userId = UserContext.current().getId();
        Long resolvedTreeId = resolveTreeId(userId, treeId);
        List<UserSyllabusNode> nodes = selectUserNodes(userId, resolvedTreeId);
        return toTree(nodes, groupTagsByNodeId(userId, resolvedTreeId));
    }

    public List<LearningNodeTree> rootChildren(Long treeId) {
        Long userId = UserContext.current().getId();
        studyTreeService.requireOwnedTree(treeId, userId);
        return rootChildrenForOwnedTree(treeId, userId);
    }

    public List<LearningNodeTree> rootChildrenForOwnedTree(Long treeId, Long userId) {
        List<UserSyllabusNode> nodes = userSyllabusNodeMapper.selectList(new LambdaQueryWrapper<UserSyllabusNode>()
                .eq(UserSyllabusNode::getUserId, userId)
                .eq(UserSyllabusNode::getTreeId, treeId)
                .isNull(UserSyllabusNode::getParentId)
                .orderByAsc(UserSyllabusNode::getSortOrder)
                .orderByAsc(UserSyllabusNode::getId));
        return toTree(nodes, groupTagsByNodeId(userId, treeId));
    }

    public List<LearningNodeTagView> tags(Long nodeId) {
        Long userId = UserContext.current().getId();
        requireOwnedNode(nodeId, userId);
        return toTagViews(selectTagsByNodeId(userId, nodeId));
    }

    public List<LearningNodeConnectionView> connections() {
        Long userId = UserContext.current().getId();
        return toConnectionViews(selectConnections(userId));
    }

    public UserSyllabusNode requireCurrentUserNode(Long id) {
        return requireOwnedNode(id, UserContext.current().getId());
    }

    @Transactional
    public List<LearningNodeTree> importOfficialTree(Long treeId) {
        Long userId = UserContext.current().getId();
        Long resolvedTreeId = resolveTreeId(userId, treeId);
        if (hasUserTree(userId, resolvedTreeId)) {
            return tree(resolvedTreeId);
        }
        Map<Long, Long> officialToUser = new LinkedHashMap<Long, Long>();
        List<SyllabusNode> officialNodes = syllabusService.enabledNodes();
        LocalDateTime now = LocalDateTime.now();
        for (SyllabusNode official : officialNodes) {
            UserSyllabusNode copy = new UserSyllabusNode();
            copy.setUserId(userId);
            copy.setTreeId(resolvedTreeId);
            copy.setOfficialNodeId(official.getId());
            copy.setParentId(official.getParentId() == null ? null : officialToUser.get(official.getParentId()));
            copy.setTitle(official.getTitle());
            copy.setLabel(official.getLabel());
            copy.setLevelNo(official.getLevelNo());
            copy.setSortOrder(official.getSortOrder());
            copy.setStatus(LearningStatus.NOT_STARTED);
            copy.setReviewCount(0);
            copy.setCustomNode(false);
            copy.setWeakScore(BigDecimal.ZERO);
            copy.setCreatedAt(now);
            copy.setUpdatedAt(now);
            userSyllabusNodeMapper.insert(copy);
            officialToUser.put(official.getId(), copy.getId());
        }
        return tree(resolvedTreeId);
    }

    @Transactional
    public UserSyllabusNode createCustomNode(LearningNodeCreateRequest request) {
        Long userId = UserContext.current().getId();
        studyTreeService.requireOwnedTree(request.getTreeId(), userId);
        Integer levelNo = 1;
        if (request.getParentId() != null) {
            UserSyllabusNode parent = requireOwnedNode(request.getParentId(), userId);
            if (!request.getTreeId().equals(parent.getTreeId())) {
                throw new BusinessException(400, "父节点不属于当前考研树");
            }
            levelNo = parent.getLevelNo() + 1;
        }
        LocalDateTime now = LocalDateTime.now();
        UserSyllabusNode node = new UserSyllabusNode();
        node.setUserId(userId);
        node.setTreeId(request.getTreeId());
        node.setOfficialNodeId(null);
        node.setParentId(request.getParentId());
        node.setTitle(request.getTitle());
        node.setLabel(blankToDefault(request.getLabel(), "第" + levelNo + "级"));
        node.setIconKey(null);
        node.setLevelNo(levelNo);
        node.setSortOrder(request.getSortOrder());
        node.setStatus(LearningStatus.NOT_STARTED);
        node.setReviewCount(0);
        node.setCustomNode(true);
        node.setWeakScore(BigDecimal.ZERO);
        node.setCreatedAt(now);
        node.setUpdatedAt(now);
        userSyllabusNodeMapper.insert(node);
        return node;
    }

    @Transactional
    public UserSyllabusNode updateNode(Long id, LearningNodeUpdateRequest request) {
        Long userId = UserContext.current().getId();
        UserSyllabusNode node = requireOwnedNode(id, userId);
        if (!isBlank(request.getTitle())) {
            node.setTitle(request.getTitle());
        }
        if (!isBlank(request.getLabel())) {
            node.setLabel(request.getLabel());
        }
        node.setStatus(request.getStatus());
        node.setPlainUnderstanding(request.getPlainUnderstanding());
        node.setTodayFeeling(request.getTodayFeeling());
        node.setWeakScore(calculateWeakScore(node));
        node.setUpdatedAt(LocalDateTime.now());
        userSyllabusNodeMapper.updateById(node);
        persistReflectionIfNeeded(node, "FEELING", request.getTodayFeeling());
        return node;
    }

    @Transactional
    public UserSyllabusNode updateNodeIcon(Long id, String iconKey) {
        Long userId = UserContext.current().getId();
        UserSyllabusNode node = requireOwnedNode(id, userId);
        node.setIconKey(normalizeIconKey(iconKey));
        node.setUpdatedAt(LocalDateTime.now());
        userSyllabusNodeMapper.updateById(node);
        return node;
    }

    @Transactional
    public LearningNodeTagView createTag(Long nodeId, LearningNodeTagRequest request) {
        Long userId = UserContext.current().getId();
        requireOwnedNode(nodeId, userId);
        LocalDateTime now = LocalDateTime.now();
        LearningNodeTag tag = new LearningNodeTag();
        tag.setUserId(userId);
        tag.setUserNodeId(nodeId);
        tag.setName(request.getName().trim());
        tag.setColor(normalizeColor(request.getColor()));
        tag.setCreatedAt(now);
        tag.setUpdatedAt(now);
        learningNodeTagMapper.insert(tag);
        return toTagView(tag);
    }

    @Transactional
    public LearningNodeTagView updateTag(Long tagId, LearningNodeTagRequest request) {
        Long userId = UserContext.current().getId();
        LearningNodeTag tag = requireOwnedTag(tagId, userId);
        tag.setName(request.getName().trim());
        tag.setColor(normalizeColor(request.getColor()));
        tag.setUpdatedAt(LocalDateTime.now());
        learningNodeTagMapper.updateById(tag);
        return toTagView(tag);
    }

    @Transactional
    public void deleteTag(Long tagId) {
        Long userId = UserContext.current().getId();
        LearningNodeTag tag = requireOwnedTag(tagId, userId);
        learningNodeTagMapper.deleteById(tag.getId());
    }

    @Transactional
    public LearningNodeConnectionView createConnection(LearningNodeConnectionRequest request) {
        Long userId = UserContext.current().getId();
        if (request.getSourceNodeId().equals(request.getTargetNodeId())) {
            throw new BusinessException(400, "节点连接的起点和终点不能相同");
        }
        requireOwnedNode(request.getSourceNodeId(), userId);
        requireOwnedNode(request.getTargetNodeId(), userId);
        ensureConnectionMissing(userId, request.getSourceNodeId(), request.getTargetNodeId());

        LocalDateTime now = LocalDateTime.now();
        LearningNodeConnection connection = new LearningNodeConnection();
        connection.setUserId(userId);
        connection.setSourceNodeId(request.getSourceNodeId());
        connection.setTargetNodeId(request.getTargetNodeId());
        connection.setRelationType(blankToDefault(request.getRelationType(), "RELATED"));
        connection.setLabel(trimToNull(request.getLabel()));
        connection.setCreatedAt(now);
        connection.setUpdatedAt(now);
        learningNodeConnectionMapper.insert(connection);
        return toConnectionView(connection);
    }

    @Transactional
    public LearningNodeConnectionView updateConnection(Long connectionId, LearningNodeConnectionRequest request) {
        Long userId = UserContext.current().getId();
        LearningNodeConnection connection = requireOwnedConnection(connectionId, userId);
        if (request.getSourceNodeId().equals(request.getTargetNodeId())) {
            throw new BusinessException(400, "节点连接的起点和终点不能相同");
        }
        requireOwnedNode(request.getSourceNodeId(), userId);
        requireOwnedNode(request.getTargetNodeId(), userId);
        ensureConnectionMissing(userId, request.getSourceNodeId(), request.getTargetNodeId(), connectionId);

        connection.setSourceNodeId(request.getSourceNodeId());
        connection.setTargetNodeId(request.getTargetNodeId());
        connection.setRelationType(blankToDefault(request.getRelationType(), "RELATED"));
        connection.setLabel(trimToNull(request.getLabel()));
        connection.setUpdatedAt(LocalDateTime.now());
        learningNodeConnectionMapper.updateById(connection);
        return toConnectionView(connection);
    }

    @Transactional
    public void deleteConnection(Long connectionId) {
        Long userId = UserContext.current().getId();
        LearningNodeConnection connection = requireOwnedConnection(connectionId, userId);
        learningNodeConnectionMapper.deleteById(connection.getId());
    }

    @Transactional
    public UserSyllabusNode review(Long id) {
        Long userId = UserContext.current().getId();
        UserSyllabusNode node = requireOwnedNode(id, userId);
        node.setReviewCount(node.getReviewCount() + 1);
        if (node.getStatus() == LearningStatus.NOT_STARTED) {
            node.setStatus(LearningStatus.IN_PROGRESS);
        }
        node.setWeakScore(calculateWeakScore(node));
        node.setUpdatedAt(LocalDateTime.now());
        userSyllabusNodeMapper.updateById(node);
        return node;
    }

    private UserSyllabusNode requireOwnedNode(Long id, Long userId) {
        UserSyllabusNode node = userSyllabusNodeMapper.selectOne(new LambdaQueryWrapper<UserSyllabusNode>()
                .eq(UserSyllabusNode::getId, id)
                .eq(UserSyllabusNode::getUserId, userId));
        if (node == null) {
            throw new BusinessException(404, "学习节点不存在或不属于当前用户");
        }
        return node;
    }

    @Transactional
    public void attachLegacyNodesToTree(Long userId, Long treeId) {
        List<UserSyllabusNode> legacyNodes = userSyllabusNodeMapper.selectList(new LambdaQueryWrapper<UserSyllabusNode>()
                .eq(UserSyllabusNode::getUserId, userId)
                .isNull(UserSyllabusNode::getTreeId));
        for (UserSyllabusNode node : legacyNodes) {
            node.setTreeId(treeId);
            node.setUpdatedAt(LocalDateTime.now());
            userSyllabusNodeMapper.updateById(node);
        }
    }

    @Transactional
    public void deleteTreeNodes(Long treeId, Long userId) {
        List<UserSyllabusNode> nodes = selectUserNodes(userId, treeId);
        if (nodes.isEmpty()) {
            return;
        }
        List<Long> nodeIds = new ArrayList<Long>();
        for (UserSyllabusNode node : nodes) {
            nodeIds.add(node.getId());
        }
        learningNodeConnectionMapper.delete(new LambdaQueryWrapper<LearningNodeConnection>()
                .eq(LearningNodeConnection::getUserId, userId)
                .and(wrapper -> wrapper
                        .in(LearningNodeConnection::getSourceNodeId, nodeIds)
                        .or()
                        .in(LearningNodeConnection::getTargetNodeId, nodeIds)));
        learningNodeTagMapper.delete(new LambdaQueryWrapper<LearningNodeTag>()
                .eq(LearningNodeTag::getUserId, userId)
                .in(LearningNodeTag::getUserNodeId, nodeIds));
        learningReflectionMapper.delete(new LambdaQueryWrapper<LearningReflection>()
                .eq(LearningReflection::getUserId, userId)
                .in(LearningReflection::getUserNodeId, nodeIds));
        List<UserSyllabusNode> reversed = new ArrayList<UserSyllabusNode>(nodes);
        Collections.reverse(reversed);
        for (UserSyllabusNode node : reversed) {
            userSyllabusNodeMapper.deleteById(node.getId());
        }
    }

    private void persistReflectionIfNeeded(UserSyllabusNode node, String type, String content) {
        if (isBlank(content)) {
            return;
        }
        LearningReflection reflection = new LearningReflection();
        reflection.setUserId(node.getUserId());
        reflection.setUserNodeId(node.getId());
        reflection.setContentType(type);
        reflection.setContent(content);
        reflection.setCreatedAt(LocalDateTime.now());
        learningReflectionMapper.insert(reflection);
    }

    private List<UserSyllabusNode> selectUserNodes(Long userId, Long treeId) {
        return userSyllabusNodeMapper.selectList(new LambdaQueryWrapper<UserSyllabusNode>()
                .eq(UserSyllabusNode::getUserId, userId)
                .eq(UserSyllabusNode::getTreeId, treeId)
                .orderByAsc(UserSyllabusNode::getLevelNo)
                .orderByAsc(UserSyllabusNode::getSortOrder)
                .orderByAsc(UserSyllabusNode::getId));
    }

    private boolean hasUserTree(Long userId, Long treeId) {
        return userSyllabusNodeMapper.selectCount(new LambdaQueryWrapper<UserSyllabusNode>()
                .eq(UserSyllabusNode::getUserId, userId)
                .eq(UserSyllabusNode::getTreeId, treeId)) > 0;
    }

    private Long resolveTreeId(Long userId, Long treeId) {
        if (treeId == null) {
            return studyTreeService.ensureDefaultTree(userId).getId();
        }
        studyTreeService.requireOwnedTree(treeId, userId);
        return treeId;
    }

    private BigDecimal calculateWeakScore(UserSyllabusNode node) {
        int score = 0;
        String feeling = node.getTodayFeeling() == null ? "" : node.getTodayFeeling();
        if (node.getStatus() == LearningStatus.NOT_STARTED) {
            score += 20;
        }
        if (node.getStatus() == LearningStatus.IN_PROGRESS) {
            score += 10;
        }
        if (containsAny(feeling, "不懂", "迷茫", "不会", "卡", "错", "崩", "混乱")) {
            score += 55;
        }
        if (node.getReviewCount() >= 3 && node.getStatus() != LearningStatus.MASTERED) {
            score += 20;
        }
        return BigDecimal.valueOf(Math.min(100, score));
    }

    private static List<LearningNodeTree> toTree(List<UserSyllabusNode> nodes, Map<Long, List<LearningNodeTagView>> tagsByNodeId) {
        Map<Long, LearningNodeTree> byId = new LinkedHashMap<Long, LearningNodeTree>();
        List<LearningNodeTree> roots = new ArrayList<LearningNodeTree>();
        for (UserSyllabusNode node : nodes) {
            byId.put(node.getId(), toView(node, tagsByNodeId.get(node.getId())));
        }
        for (UserSyllabusNode node : nodes) {
            LearningNodeTree view = byId.get(node.getId());
            if (node.getParentId() == null || !byId.containsKey(node.getParentId())) {
                roots.add(view);
            } else {
                byId.get(node.getParentId()).getChildren().add(view);
            }
        }
        return roots;
    }

    private static LearningNodeTree toView(UserSyllabusNode node, List<LearningNodeTagView> tags) {
        LearningNodeTree view = new LearningNodeTree();
        view.setId(node.getId());
        view.setTreeId(node.getTreeId());
        view.setOfficialNodeId(node.getOfficialNodeId());
        view.setParentId(node.getParentId());
        view.setTitle(node.getTitle());
        view.setLabel(node.getLabel());
        view.setIconKey(node.getIconKey());
        view.setLevelNo(node.getLevelNo());
        view.setSortOrder(node.getSortOrder());
        view.setStatus(node.getStatus());
        view.setReviewCount(node.getReviewCount());
        view.setPlainUnderstanding(node.getPlainUnderstanding());
        view.setTodayFeeling(node.getTodayFeeling());
        view.setCustomNode(node.getCustomNode());
        view.setWeakScore(node.getWeakScore());
        view.setTags(tags == null ? new ArrayList<LearningNodeTagView>() : tags);
        return view;
    }

    private Map<Long, List<LearningNodeTagView>> groupTagsByNodeId(Long userId, Long treeId) {
        List<UserSyllabusNode> nodes = selectUserNodes(userId, treeId);
        if (nodes.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> nodeIds = new ArrayList<Long>();
        for (UserSyllabusNode node : nodes) {
            nodeIds.add(node.getId());
        }
        List<LearningNodeTag> tags = learningNodeTagMapper.selectList(new LambdaQueryWrapper<LearningNodeTag>()
                .eq(LearningNodeTag::getUserId, userId)
                .in(LearningNodeTag::getUserNodeId, nodeIds)
                .orderByAsc(LearningNodeTag::getId));
        Map<Long, List<LearningNodeTagView>> grouped = new HashMap<Long, List<LearningNodeTagView>>();
        for (LearningNodeTag tag : tags) {
            Long nodeId = tag.getUserNodeId();
            if (!grouped.containsKey(nodeId)) {
                grouped.put(nodeId, new ArrayList<LearningNodeTagView>());
            }
            grouped.get(nodeId).add(toTagView(tag));
        }
        return grouped;
    }

    private List<LearningNodeTag> selectTagsByNodeId(Long userId, Long nodeId) {
        return learningNodeTagMapper.selectList(new LambdaQueryWrapper<LearningNodeTag>()
                .eq(LearningNodeTag::getUserId, userId)
                .eq(LearningNodeTag::getUserNodeId, nodeId)
                .orderByAsc(LearningNodeTag::getId));
    }

    private List<LearningNodeConnection> selectConnections(Long userId) {
        return learningNodeConnectionMapper.selectList(new LambdaQueryWrapper<LearningNodeConnection>()
                .eq(LearningNodeConnection::getUserId, userId)
                .orderByAsc(LearningNodeConnection::getId));
    }

    private LearningNodeTag requireOwnedTag(Long tagId, Long userId) {
        LearningNodeTag tag = learningNodeTagMapper.selectOne(new LambdaQueryWrapper<LearningNodeTag>()
                .eq(LearningNodeTag::getId, tagId)
                .eq(LearningNodeTag::getUserId, userId));
        if (tag == null) {
            throw new BusinessException(404, "节点标签不存在或不属于当前用户");
        }
        return tag;
    }

    private LearningNodeConnection requireOwnedConnection(Long connectionId, Long userId) {
        LearningNodeConnection connection = learningNodeConnectionMapper.selectOne(new LambdaQueryWrapper<LearningNodeConnection>()
                .eq(LearningNodeConnection::getId, connectionId)
                .eq(LearningNodeConnection::getUserId, userId));
        if (connection == null) {
            throw new BusinessException(404, "节点连接不存在或不属于当前用户");
        }
        return connection;
    }

    private void ensureConnectionMissing(Long userId, Long sourceNodeId, Long targetNodeId) {
        ensureConnectionMissing(userId, sourceNodeId, targetNodeId, null);
    }

    private void ensureConnectionMissing(Long userId, Long sourceNodeId, Long targetNodeId, Long excludeId) {
        List<LearningNodeConnection> existing = learningNodeConnectionMapper.selectList(new LambdaQueryWrapper<LearningNodeConnection>()
                .eq(LearningNodeConnection::getUserId, userId)
                .eq(LearningNodeConnection::getSourceNodeId, sourceNodeId)
                .eq(LearningNodeConnection::getTargetNodeId, targetNodeId));
        for (LearningNodeConnection item : existing) {
            if (excludeId == null || !excludeId.equals(item.getId())) {
                throw new BusinessException(400, "该节点连接已经存在");
            }
        }
    }

    private static List<LearningNodeTagView> toTagViews(List<LearningNodeTag> tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }
        List<LearningNodeTagView> result = new ArrayList<LearningNodeTagView>();
        for (LearningNodeTag tag : tags) {
            result.add(toTagView(tag));
        }
        return result;
    }

    private static List<LearningNodeConnectionView> toConnectionViews(List<LearningNodeConnection> connections) {
        if (connections == null || connections.isEmpty()) {
            return Collections.emptyList();
        }
        List<LearningNodeConnectionView> result = new ArrayList<LearningNodeConnectionView>();
        for (LearningNodeConnection connection : connections) {
            result.add(toConnectionView(connection));
        }
        return result;
    }

    private static LearningNodeTagView toTagView(LearningNodeTag tag) {
        LearningNodeTagView view = new LearningNodeTagView();
        view.setId(tag.getId());
        view.setUserNodeId(tag.getUserNodeId());
        view.setName(tag.getName());
        view.setColor(tag.getColor());
        return view;
    }

    private static LearningNodeConnectionView toConnectionView(LearningNodeConnection connection) {
        LearningNodeConnectionView view = new LearningNodeConnectionView();
        view.setId(connection.getId());
        view.setSourceNodeId(connection.getSourceNodeId());
        view.setTargetNodeId(connection.getTargetNodeId());
        view.setRelationType(connection.getRelationType());
        view.setLabel(connection.getLabel());
        return view;
    }

    private static boolean containsAny(String value, String... patterns) {
        for (String pattern : patterns) {
            if (value.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String blankToDefault(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }

    private static String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private static String normalizeColor(String value) {
        return isBlank(value) ? "#6bfb9a" : value.trim();
    }

    private static String normalizeIconKey(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
