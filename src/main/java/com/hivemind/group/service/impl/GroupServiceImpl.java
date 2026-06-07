package com.hivemind.group.service.impl;

import com.hivemind.common.event.GroupCreatedEvent;
import com.hivemind.group.dto.CreateGroupRequest;
import com.hivemind.group.dto.GroupDto;
import com.hivemind.group.dto.GroupFollowDto;
import com.hivemind.group.dto.UserGroupDto;
import com.hivemind.group.entity.Group;
import com.hivemind.group.entity.GroupFollow;
import com.hivemind.group.entity.GroupLike;
import com.hivemind.group.entity.GroupMember;
import com.hivemind.group.entity.UserGroup;
import com.hivemind.group.repository.GroupFollowRepository;
import com.hivemind.group.repository.GroupLikeRepository;
import com.hivemind.group.repository.GroupMemberRepository;
import com.hivemind.group.repository.GroupRepository;
import com.hivemind.group.repository.UserGroupRepository;
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
    private final UserGroupRepository userGroupRepository;
    private final GroupFollowRepository groupFollowRepository;
    private final GroupLikeRepository groupLikeRepository;
    private final KafkaTemplate<String, GroupCreatedEvent> kafkaTemplate;

    @Override
    public GroupDto createGroup(UUID creatorId, CreateGroupRequest request)
    {
        // Enforce one-group-per-user limit
        List<Group> existingGroups = groupRepository.findByCreatorId(creatorId);
        if (!existingGroups.isEmpty())
        {
            throw new RuntimeException("Group creation limit reached: each user may create a maximum of one group");
        }

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

        // Add to user_groups reverse-lookup table
        UserGroup userGroup = UserGroup.builder()
                .userId(creatorId)
                .groupId(group.getGroupId())
                .role("ADMIN")
                .joinedAt(LocalDateTime.now())
                .build();
        userGroupRepository.save(userGroup);

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
    public List<UserGroupDto> getUserGroups(UUID userId)
    {
        List<UserGroup> userGroups = userGroupRepository.findByUserId(userId);
        return userGroups.stream()
                .map(ug -> {
                    GroupDto groupDto = null;
                    try
                    {
                        groupDto = getGroupById(ug.getGroupId());
                    }
                    catch (RuntimeException e)
                    {
                        log.warn("Group not found for user_groups entry: {}", ug.getGroupId());
                    }
                    return UserGroupDto.builder()
                            .groupId(ug.getGroupId())
                            .role(ug.getRole())
                            .joinedAt(ug.getJoinedAt())
                            .group(groupDto)
                            .build();
                })
                .filter(dto -> dto.getGroup() != null)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasApprovedMembership(UUID groupId, UUID userId)
    {
        return groupMemberRepository.findByGroupIdAndUserId(groupId, userId).isPresent();
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

        // Add to user_groups reverse-lookup table
        UserGroup userGroup = UserGroup.builder()
                .userId(userId)
                .groupId(groupId)
                .role("MEMBER")
                .joinedAt(LocalDateTime.now())
                .build();
        userGroupRepository.save(userGroup);

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

        // Remove from user_groups reverse-lookup table
        userGroupRepository.findByUserIdAndGroupId(userId, groupId)
                .ifPresent(userGroupRepository::delete);

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

        // Remove from user_groups reverse-lookup table
        userGroupRepository.findByUserIdAndGroupId(userId, groupId)
                .ifPresent(userGroupRepository::delete);

        groupRepository.findById(groupId).ifPresent(group -> {
            group.setMemberCount(Math.max(0, group.getMemberCount() - 1));
            groupRepository.save(group);
        });
        log.info("User {} removed from group {} by {}", userId, groupId, requesterId);
    }

    // ─── Group Follow (Admin Only) ─────────────────────────────────────────────

    @Override
    public GroupFollowDto followGroup(UUID groupId, UUID targetGroupId, UUID userId)
    {
        if (groupId.equals(targetGroupId))
        {
            throw new RuntimeException("A group cannot follow itself");
        }

        // Verify requester is admin of the follower group
        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new RuntimeException("User is not a member of this group"));

        if (!"ADMIN".equals(requester.getRole()))
        {
            throw new RuntimeException("Only group admins can follow other groups");
        }

        // Verify target group exists
        groupRepository.findById(targetGroupId)
                .orElseThrow(() -> new RuntimeException("Target group not found: " + targetGroupId));

        // Check if already following (idempotent)
        if (groupFollowRepository.findByGroupIdAndFollowedGroupId(groupId, targetGroupId).isPresent())
        {
            // Return existing without creating duplicate
            GroupFollow existing = groupFollowRepository.findByGroupIdAndFollowedGroupId(groupId, targetGroupId).get();
            return toFollowDto(existing);
        }

        GroupFollow follow = GroupFollow.builder()
                .groupId(groupId)
                .followedGroupId(targetGroupId)
                .createdBy(userId)
                .createdAt(LocalDateTime.now())
                .build();
        groupFollowRepository.save(follow);

        log.info("Group {} now follows group {} (by admin {})", groupId, targetGroupId, userId);
        return toFollowDto(follow);
    }

    @Override
    public void unfollowGroup(UUID groupId, UUID targetGroupId, UUID userId)
    {
        // Verify requester is admin of the follower group
        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new RuntimeException("User is not a member of this group"));

        if (!"ADMIN".equals(requester.getRole()))
        {
            throw new RuntimeException("Only group admins can unfollow groups");
        }

        GroupFollow follow = groupFollowRepository.findByGroupIdAndFollowedGroupId(groupId, targetGroupId)
                .orElseThrow(() -> new RuntimeException("Follow relationship not found"));

        groupFollowRepository.delete(follow);
        log.info("Group {} unfollowed group {} (by admin {})", groupId, targetGroupId, userId);
    }

    @Override
    public List<GroupFollowDto> getFollowedGroups(UUID groupId)
    {
        return groupFollowRepository.findByGroupId(groupId).stream()
                .map(this::toFollowDto)
                .collect(Collectors.toList());
    }

    // ─── Group Like ─────────────────────────────────────────────────────────────

    @Override
    public void likeGroup(UUID groupId, UUID userId)
    {
        if (groupLikeRepository.findByGroupIdAndUserId(groupId, userId).isPresent())
        {
            throw new RuntimeException("Group already liked by this user");
        }

        GroupLike like = GroupLike.builder()
                .groupId(groupId)
                .userId(userId)
                .createdAt(LocalDateTime.now())
                .build();
        groupLikeRepository.save(like);
        log.info("User {} liked group {}", userId, groupId);
    }

    @Override
    public void unlikeGroup(UUID groupId, UUID userId)
    {
        GroupLike like = groupLikeRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new RuntimeException("No existing like found for this group"));
        groupLikeRepository.delete(like);
        log.info("User {} unliked group {}", userId, groupId);
    }

    @Override
    public int getLikeCount(UUID groupId)
    {
        return groupLikeRepository.findByGroupId(groupId).size();
    }

    @Override
    public boolean isLikedByUser(UUID groupId, UUID userId)
    {
        return groupLikeRepository.findByGroupIdAndUserId(groupId, userId).isPresent();
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

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

    private GroupFollowDto toFollowDto(GroupFollow follow)
    {
        return GroupFollowDto.builder()
                .groupId(follow.getGroupId())
                .followedGroupId(follow.getFollowedGroupId())
                .createdBy(follow.getCreatedBy())
                .createdAt(follow.getCreatedAt())
                .build();
    }
}
