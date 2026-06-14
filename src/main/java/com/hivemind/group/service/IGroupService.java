package com.hivemind.group.service;

import com.hivemind.group.dto.CreateGroupRequest;
import com.hivemind.group.dto.GroupDto;
import com.hivemind.group.dto.GroupFollowDto;
import com.hivemind.group.dto.UpdateGroupRequest;
import com.hivemind.group.dto.UserGroupDto;
import com.hivemind.group.entity.GroupMember;

import java.util.List;
import java.util.UUID;

public interface IGroupService
{
    GroupDto createGroup(UUID creatorId, CreateGroupRequest request);

    GroupDto getGroupById(UUID groupId);

    GroupDto updateGroup(UUID groupId, UUID userId, UpdateGroupRequest request);

    List<GroupDto> getGroupsByCreator(UUID creatorId);

    /** Search all public groups by name */
    List<GroupDto> searchGroups(String query);

    /** Get all groups where the user has an approved membership */
    List<UserGroupDto> getUserGroups(UUID userId);

    /** Verify if a user has approved membership in a specific group */
    boolean hasApprovedMembership(UUID groupId, UUID userId);

    void joinGroup(UUID groupId, UUID userId);

    void leaveGroup(UUID groupId, UUID userId);

    List<GroupMember> getMembers(UUID groupId);

    void removeMember(UUID groupId, UUID userId, UUID requesterId);

    // Group Follow (Admin only)
    GroupFollowDto followGroup(UUID groupId, UUID targetGroupId, UUID userId);

    void unfollowGroup(UUID groupId, UUID targetGroupId, UUID userId);

    List<GroupFollowDto> getFollowedGroups(UUID groupId);

    // Group Like
    void likeGroup(UUID groupId, UUID userId);

    void unlikeGroup(UUID groupId, UUID userId);

    int getLikeCount(UUID groupId);

    boolean isLikedByUser(UUID groupId, UUID userId);
}
