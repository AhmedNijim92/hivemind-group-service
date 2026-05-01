package com.hivemind.group.service.impl;

import com.hivemind.common.event.GroupCreatedEvent;
import com.hivemind.group.dto.CreateGroupRequest;
import com.hivemind.group.dto.GroupDto;
import com.hivemind.group.entity.Group;
import com.hivemind.group.entity.GroupMember;
import com.hivemind.group.repository.GroupMemberRepository;
import com.hivemind.group.repository.GroupRepository;
import com.hivemind.group.service.IGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupServiceImpl implements IGroupService
{
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final KafkaTemplate<String, GroupCreatedEvent> kafkaTemplate;

    @Override
    public GroupDto createGroup(UUID creatorId, CreateGroupRequest request)
    {
        Group group = Group.builder()
                .groupId(UUID.randomUUID())
                .creatorId(creatorId)
                .name(request.getName())
                .description(request.getDescription())
                .privacy(request.getPrivacy())
                .memberCount(1)
                .createdAt(LocalDateTime.now())
                .build();

        groupRepository.save(group);

        // Add creator as admin member
        GroupMember adminMember = GroupMember.builder()
                .groupId(group.getGroupId())
                .userId(creatorId)
                .role("ADMIN")
                .joinedAt(LocalDateTime.now())
                .build();
        groupMemberRepository.save(adminMember);

        // Publish event
        GroupCreatedEvent event = GroupCreatedEvent.builder()
                .groupId(group.getGroupId())
                .creatorId(creatorId)
                .groupName(group.getName())
                .timestamp(LocalDateTime.now())
                .build();
        kafkaTemplate.send("group-created-topic", event);

        log.info("Group created: {} by user: {}", group.getGroupId(), creatorId);
        return toDto(group);
    }

    @Override
    public GroupDto getGroupById(UUID groupId)
    {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));
        return toDto(group);
    }

    @Override
    public List<GroupDto> getGroupsByCreator(UUID creatorId)
    {
        return groupRepository.findByCreatorId(creatorId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void joinGroup(UUID groupId, UUID userId)
    {
        groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .ifPresent(m -> { throw new RuntimeException("Already a member of this group"); });

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));

        GroupMember member = GroupMember.builder()
                .groupId(groupId)
                .userId(userId)
                .role("MEMBER")
                .joinedAt(LocalDateTime.now())
                .build();
        groupMemberRepository.save(member);

        group.setMemberCount(group.getMemberCount() + 1);
        groupRepository.save(group);
        log.info("User {} joined group {}", userId, groupId);
    }

    @Override
    public void leaveGroup(UUID groupId, UUID userId)
    {
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new RuntimeException("Not a member of this group"));
        groupMemberRepository.delete(member);

        groupRepository.findById(groupId).ifPresent(group -> {
            group.setMemberCount(Math.max(0, group.getMemberCount() - 1));
            groupRepository.save(group);
        });
        log.info("User {} left group {}", userId, groupId);
    }

    @Override
    public List<GroupMember> getMembers(UUID groupId)
    {
        return groupMemberRepository.findByGroupId(groupId);
    }

    @Override
    public void removeMember(UUID groupId, UUID userId, UUID requesterId)
    {
        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(groupId, requesterId)
                .orElseThrow(() -> new RuntimeException("Requester is not a member"));

        if (!"ADMIN".equals(requester.getRole()) && !"MODERATOR".equals(requester.getRole()))
        {
            throw new RuntimeException("Insufficient permissions to remove members");
        }

        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new RuntimeException("User is not a member of this group"));
        groupMemberRepository.delete(member);

        groupRepository.findById(groupId).ifPresent(group -> {
            group.setMemberCount(Math.max(0, group.getMemberCount() - 1));
            groupRepository.save(group);
        });
        log.info("User {} removed from group {} by {}", userId, groupId, requesterId);
    }

    private GroupDto toDto(Group group)
    {
        return GroupDto.builder()
                .groupId(group.getGroupId())
                .creatorId(group.getCreatorId())
                .name(group.getName())
                .description(group.getDescription())
                .privacy(group.getPrivacy())
                .memberCount(group.getMemberCount())
                .createdAt(group.getCreatedAt())
                .build();
    }
}
