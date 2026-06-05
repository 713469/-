package com.kyexam.learning.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kyexam.common.exception.BusinessException;
import com.kyexam.learning.dto.LearningNodeTree;
import com.kyexam.learning.dto.StudyTreeRequest;
import com.kyexam.learning.dto.TreeGroupRequest;
import com.kyexam.learning.entity.StudyTree;
import com.kyexam.learning.entity.TreeGroup;
import com.kyexam.learning.mapper.StudyTreeMapper;
import com.kyexam.learning.mapper.TreeGroupMapper;
import com.kyexam.system.security.UserContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudyTreeService {
    private static final String DEFAULT_GROUP_NAME = "专业课";
    private static final String DEFAULT_TREE_NAME = "408";

    private final TreeGroupMapper treeGroupMapper;
    private final StudyTreeMapper studyTreeMapper;
    private final LearningTreeService learningTreeService;

    public StudyTreeService(
            TreeGroupMapper treeGroupMapper,
            StudyTreeMapper studyTreeMapper,
            @Lazy LearningTreeService learningTreeService) {
        this.treeGroupMapper = treeGroupMapper;
        this.studyTreeMapper = studyTreeMapper;
        this.learningTreeService = learningTreeService;
    }

    public List<TreeGroup> groups() {
        Long userId = UserContext.current().getId();
        return treeGroupMapper.selectList(new LambdaQueryWrapper<TreeGroup>()
                .eq(TreeGroup::getUserId, userId)
                .orderByAsc(TreeGroup::getSortOrder)
                .orderByAsc(TreeGroup::getId));
    }

    public TreeGroup group(Long id) {
        return requireOwnedGroup(id, UserContext.current().getId());
    }

    @Transactional
    public TreeGroup createGroup(TreeGroupRequest request) {
        Long userId = UserContext.current().getId();
        LocalDateTime now = LocalDateTime.now();
        TreeGroup group = new TreeGroup();
        group.setUserId(userId);
        group.setName(request.getName().trim());
        group.setDescription(trimToNull(request.getDescription()));
        group.setSortOrder(defaultOrder(request.getSortOrder()));
        group.setCreatedAt(now);
        group.setUpdatedAt(now);
        treeGroupMapper.insert(group);
        return group;
    }

    @Transactional
    public TreeGroup updateGroup(Long id, TreeGroupRequest request) {
        Long userId = UserContext.current().getId();
        TreeGroup group = requireOwnedGroup(id, userId);
        group.setName(request.getName().trim());
        group.setDescription(trimToNull(request.getDescription()));
        group.setSortOrder(defaultOrder(request.getSortOrder()));
        group.setUpdatedAt(LocalDateTime.now());
        treeGroupMapper.updateById(group);
        return group;
    }

    @Transactional
    public void deleteGroup(Long id) {
        Long userId = UserContext.current().getId();
        TreeGroup group = requireOwnedGroup(id, userId);
        Long treeCount = studyTreeMapper.selectCount(new LambdaQueryWrapper<StudyTree>()
                .eq(StudyTree::getUserId, userId)
                .eq(StudyTree::getGroupId, group.getId()));
        if (treeCount > 0) {
            throw new BusinessException(400, "该分组下仍有考研树，请先移动或删除这些树后再删除分组");
        }
        treeGroupMapper.deleteById(group.getId());
    }

    public List<StudyTree> trees() {
        Long userId = UserContext.current().getId();
        ensureDefaultTree(userId);
        return studyTreeMapper.selectList(new LambdaQueryWrapper<StudyTree>()
                .eq(StudyTree::getUserId, userId)
                .orderByAsc(StudyTree::getSortOrder)
                .orderByAsc(StudyTree::getId));
    }

    @Transactional
    public StudyTree createTree(StudyTreeRequest request) {
        Long userId = UserContext.current().getId();
        validateGroup(request.getGroupId(), userId);
        LocalDateTime now = LocalDateTime.now();
        StudyTree tree = new StudyTree();
        tree.setUserId(userId);
        tree.setGroupId(request.getGroupId());
        tree.setName(request.getName().trim());
        tree.setDescription(trimToNull(request.getDescription()));
        tree.setSortOrder(defaultOrder(request.getSortOrder()));
        tree.setCreatedAt(now);
        tree.setUpdatedAt(now);
        studyTreeMapper.insert(tree);
        return tree;
    }

    @Transactional
    public StudyTree updateTree(Long id, StudyTreeRequest request) {
        Long userId = UserContext.current().getId();
        StudyTree tree = requireOwnedTree(id, userId);
        validateGroup(request.getGroupId(), userId);
        tree.setGroupId(request.getGroupId());
        tree.setName(request.getName().trim());
        tree.setDescription(trimToNull(request.getDescription()));
        tree.setSortOrder(defaultOrder(request.getSortOrder()));
        tree.setUpdatedAt(LocalDateTime.now());
        studyTreeMapper.updateById(tree);
        return tree;
    }

    @Transactional
    public void deleteTree(Long id) {
        Long userId = UserContext.current().getId();
        StudyTree tree = requireOwnedTree(id, userId);
        learningTreeService.deleteTreeNodes(tree.getId(), userId);
        studyTreeMapper.deleteById(tree.getId());
    }

    public StudyTree tree(Long id) {
        return requireOwnedTree(id, UserContext.current().getId());
    }

    public List<LearningNodeTree> rootChildren(Long id) {
        Long userId = UserContext.current().getId();
        requireOwnedTree(id, userId);
        return learningTreeService.rootChildrenForOwnedTree(id, userId);
    }

    @Transactional
    public StudyTree ensureDefaultTree(Long userId) {
        StudyTree existing = studyTreeMapper.selectOne(new LambdaQueryWrapper<StudyTree>()
                .eq(StudyTree::getUserId, userId)
                .orderByAsc(StudyTree::getSortOrder)
                .orderByAsc(StudyTree::getId)
                .last("LIMIT 1"));
        if (existing != null) {
            return existing;
        }

        TreeGroup group = treeGroupMapper.selectOne(new LambdaQueryWrapper<TreeGroup>()
                .eq(TreeGroup::getUserId, userId)
                .eq(TreeGroup::getName, DEFAULT_GROUP_NAME)
                .last("LIMIT 1"));
        LocalDateTime now = LocalDateTime.now();
        if (group == null) {
            group = new TreeGroup();
            group.setUserId(userId);
            group.setName(DEFAULT_GROUP_NAME);
            group.setDescription("考研专业课知识树分组");
            group.setSortOrder(10);
            group.setCreatedAt(now);
            group.setUpdatedAt(now);
            treeGroupMapper.insert(group);
        }

        StudyTree tree = new StudyTree();
        tree.setUserId(userId);
        tree.setGroupId(group.getId());
        tree.setName(DEFAULT_TREE_NAME);
        tree.setDescription("默认考研专业课学习树");
        tree.setSortOrder(10);
        tree.setCreatedAt(now);
        tree.setUpdatedAt(now);
        studyTreeMapper.insert(tree);
        learningTreeService.attachLegacyNodesToTree(userId, tree.getId());
        return tree;
    }

    public StudyTree requireOwnedTree(Long id, Long userId) {
        if (id == null) {
            throw new BusinessException(400, "treeId 不能为空");
        }
        StudyTree tree = studyTreeMapper.selectOne(new LambdaQueryWrapper<StudyTree>()
                .eq(StudyTree::getId, id)
                .eq(StudyTree::getUserId, userId));
        if (tree == null) {
            throw new BusinessException(404, "考研树不存在或不属于当前用户");
        }
        return tree;
    }

    private TreeGroup requireOwnedGroup(Long id, Long userId) {
        TreeGroup group = treeGroupMapper.selectOne(new LambdaQueryWrapper<TreeGroup>()
                .eq(TreeGroup::getId, id)
                .eq(TreeGroup::getUserId, userId));
        if (group == null) {
            throw new BusinessException(404, "树分组不存在或不属于当前用户");
        }
        return group;
    }

    private void validateGroup(Long groupId, Long userId) {
        if (groupId != null) {
            requireOwnedGroup(groupId, userId);
        }
    }

    private static Integer defaultOrder(Integer value) {
        return value == null ? 0 : value;
    }

    private static String trimToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
