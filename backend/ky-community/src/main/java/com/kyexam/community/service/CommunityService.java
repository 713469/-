package com.kyexam.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kyexam.common.enums.RoleCode;
import com.kyexam.common.exception.BusinessException;
import com.kyexam.community.dto.CommunityPostCreateRequest;
import com.kyexam.community.dto.CommunityPostStatusRequest;
import com.kyexam.community.entity.CommunityPost;
import com.kyexam.community.mapper.CommunityPostMapper;
import com.kyexam.system.security.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommunityService {
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_REJECTED = "REJECTED";

    private final CommunityPostMapper communityPostMapper;

    public CommunityService(CommunityPostMapper communityPostMapper) {
        this.communityPostMapper = communityPostMapper;
    }

    public List<CommunityPost> publishedPosts() {
        return communityPostMapper.selectList(new LambdaQueryWrapper<CommunityPost>()
                .eq(CommunityPost::getStatus, STATUS_PUBLISHED)
                .orderByDesc(CommunityPost::getCreatedAt)
                .last("limit 20"));
    }

    public List<CommunityPost> managedPosts() {
        requireCommunityAdmin();
        return communityPostMapper.selectList(new LambdaQueryWrapper<CommunityPost>()
                .orderByDesc(CommunityPost::getCreatedAt)
                .last("limit 50"));
    }

    @Transactional
    public CommunityPost createPost(CommunityPostCreateRequest request) {
        CommunityPost post = new CommunityPost();
        post.setAuthorId(UserContext.current().getId());
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setStatus(STATUS_PENDING);
        post.setCreatedAt(LocalDateTime.now());
        communityPostMapper.insert(post);
        return post;
    }

    @Transactional
    public CommunityPost updateStatus(Long id, CommunityPostStatusRequest request) {
        requireCommunityAdmin();
        String status = normalizeStatus(request.getStatus());
        CommunityPost post = communityPostMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(404, "帖子不存在");
        }
        post.setStatus(status);
        communityPostMapper.updateById(post);
        return post;
    }

    private static void requireCommunityAdmin() {
        UserContext.requireAnyRole(RoleCode.COMMUNITY_ADMIN, RoleCode.SYSTEM_ADMIN);
    }

    private static String normalizeStatus(String status) {
        if (status == null) {
            throw new BusinessException(400, "帖子状态不能为空");
        }
        String normalized = status.trim().toUpperCase();
        if (STATUS_PENDING.equals(normalized) || STATUS_PUBLISHED.equals(normalized) || STATUS_REJECTED.equals(normalized)) {
            return normalized;
        }
        throw new BusinessException(400, "帖子状态只支持 PENDING/PUBLISHED/REJECTED");
    }
}
