package com.hivemind.group.service;

import com.hivemind.group.dto.AddCommentRequest;
import com.hivemind.group.dto.CreatePostRequest;
import com.hivemind.group.dto.PostDto;
import com.hivemind.group.entity.Comment;

import java.util.List;
import java.util.UUID;

public interface IPostService
{
    PostDto createPost(UUID authorId, String authorName, CreatePostRequest request);

    PostDto getPostById(UUID groupId, UUID postId);

    List<PostDto> getPostsByGroup(UUID groupId);

    /** Get posts from multiple groups for feed aggregation, sorted by creation time descending */
    List<PostDto> getPostsByGroups(List<UUID> groupIds);

    void likePost(UUID groupId, UUID postId, UUID userId);

    Comment addComment(UUID groupId, UUID postId, UUID authorId, String authorName, AddCommentRequest request);

    List<Comment> getComments(UUID postId);
}
