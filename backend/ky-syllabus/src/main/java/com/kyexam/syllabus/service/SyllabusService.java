package com.kyexam.syllabus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kyexam.common.enums.RoleCode;
import com.kyexam.common.exception.BusinessException;
import com.kyexam.syllabus.dto.SyllabusNodeRequest;
import com.kyexam.syllabus.dto.SyllabusNodeTree;
import com.kyexam.syllabus.entity.SyllabusNode;
import com.kyexam.syllabus.mapper.SyllabusNodeMapper;
import com.kyexam.system.security.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SyllabusService {
    private final SyllabusNodeMapper syllabusNodeMapper;

    public SyllabusService(SyllabusNodeMapper syllabusNodeMapper) {
        this.syllabusNodeMapper = syllabusNodeMapper;
    }

    public List<SyllabusNodeTree> tree() {
        List<SyllabusNode> nodes = enabledNodes();
        return toTree(nodes);
    }

    public List<SyllabusNodeTree> children(Long parentId) {
        List<SyllabusNode> nodes = syllabusNodeMapper.selectList(new LambdaQueryWrapper<SyllabusNode>()
                .eq(SyllabusNode::getParentId, parentId)
                .eq(SyllabusNode::getEnabled, true)
                .orderByAsc(SyllabusNode::getSortOrder)
                .orderByAsc(SyllabusNode::getId));
        return toTree(nodes);
    }

    public List<SyllabusNode> enabledNodes() {
        return syllabusNodeMapper.selectList(new LambdaQueryWrapper<SyllabusNode>()
                .eq(SyllabusNode::getEnabled, true)
                .orderByAsc(SyllabusNode::getLevelNo)
                .orderByAsc(SyllabusNode::getSortOrder)
                .orderByAsc(SyllabusNode::getId));
    }

    @Transactional
    public SyllabusNode saveNode(SyllabusNodeRequest request) {
        UserContext.requireRole(RoleCode.SYSTEM_ADMIN);
        SyllabusNode node = request.getId() == null ? new SyllabusNode() : syllabusNodeMapper.selectById(request.getId());
        if (node == null) {
            throw new BusinessException(404, "大纲节点不存在");
        }
        Integer levelNo = resolveLevel(request.getParentId());
        LocalDateTime now = LocalDateTime.now();
        node.setParentId(request.getParentId());
        node.setCode(request.getCode());
        node.setTitle(request.getTitle());
        node.setLabel(blankToDefault(request.getLabel(), "第" + levelNo + "级"));
        node.setLevelNo(levelNo);
        node.setSortOrder(request.getSortOrder());
        node.setDescription(request.getDescription());
        node.setEnabled(true);
        if (node.getCreatedAt() == null) {
            node.setCreatedAt(now);
        }
        node.setUpdatedAt(now);
        saveOrUpdate(node);
        return node;
    }

    private Integer resolveLevel(Long parentId) {
        if (parentId == null) {
            return 1;
        }
        SyllabusNode parent = syllabusNodeMapper.selectById(parentId);
        if (parent == null) {
            throw new BusinessException(404, "父级大纲节点不存在");
        }
        return parent.getLevelNo() + 1;
    }

    private void saveOrUpdate(SyllabusNode node) {
        if (node.getId() == null) {
            syllabusNodeMapper.insert(node);
        } else {
            syllabusNodeMapper.updateById(node);
        }
    }

    private static List<SyllabusNodeTree> toTree(List<SyllabusNode> nodes) {
        Map<Long, SyllabusNodeTree> byId = new LinkedHashMap<Long, SyllabusNodeTree>();
        List<SyllabusNodeTree> roots = new ArrayList<SyllabusNodeTree>();
        for (SyllabusNode node : nodes) {
            byId.put(node.getId(), toView(node));
        }
        for (SyllabusNode node : nodes) {
            SyllabusNodeTree view = byId.get(node.getId());
            if (node.getParentId() == null || !byId.containsKey(node.getParentId())) {
                roots.add(view);
            } else {
                byId.get(node.getParentId()).getChildren().add(view);
            }
        }
        return roots;
    }

    private static SyllabusNodeTree toView(SyllabusNode node) {
        SyllabusNodeTree view = new SyllabusNodeTree();
        view.setId(node.getId());
        view.setParentId(node.getParentId());
        view.setCode(node.getCode());
        view.setTitle(node.getTitle());
        view.setLabel(node.getLabel());
        view.setLevelNo(node.getLevelNo());
        view.setSortOrder(node.getSortOrder());
        view.setDescription(node.getDescription());
        return view;
    }

    private static String blankToDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }
}
