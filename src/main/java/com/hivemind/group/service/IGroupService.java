package com.hivemind.group.service;

import com.hivemind.group.dto.CreateGroupRequest;
import com.hivemind.group.dto.GroupDto;
import com.hivemind.group.entity.GroupMember;

import java.util.List;
import java.util.UUID;

public interface IGroupService
{
    GroupDto createGroup(UUID creatorId, CreateGroupRequest request);

    GroupDto getGroupById(UUID groupId);

    List<GroupDto> getGroupsByCreator(UUID creatorId);

    void joinGroup(UUID groupId, UUID userId);

    void leaveGroup(UUID groupId, UUID userId);

    List<GroupMember> getMembers(UUID groupId);

    void removeMember(UUID groupId, UUID userId, UUID requesterId);
}
