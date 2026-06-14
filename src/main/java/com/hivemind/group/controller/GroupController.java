package com.hivemind.group.controller;

import com.hivemind.common.dto.ApiResponse;
import com.hivemind.group.dto.CreateGroupRequest;
import com.hivemind.group.dto.GroupDto;
import com.hivemind.group.dto.GroupFollowDto;
import com.hivemind.group.dto.GroupFollowRequest;
import com.hivemind.group.dto.UpdateGroupRequest;
import com.hivemind.group.dto.UserGroupDto;
import com.hivemind.group.entity.GroupMember;
import com.hivemind.group.service.IGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController
{
    private final IGroupService groupService;

    @PostMapping
    public ResponseEntity<GroupDto> createGroup(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CreateGroupRequest request)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.createGroup(userId, request));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDto> getGroupById(@PathVariable UUID groupId)
    {
        return ResponseEntity.ok(groupService.getGroupById(groupId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<GroupDto>> searchGroups(@RequestParam String q)
    {
        return ResponseEntity.ok(groupService.searchGroups(q));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<GroupDto> updateGroup(
            @PathVariable UUID groupId,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody UpdateGroupRequest request)
    {
        return ResponseEntity.ok(groupService.updateGroup(groupId, userId, request));
    }

    /** Get groups created by the current user */
    @GetMapping("/my")
    public ResponseEntity<List<GroupDto>> getMyGroups(@RequestHeader("X-User-Id") UUID userId)
    {
        return ResponseEntity.ok(groupService.getGroupsByCreator(userId));
    }

    /** Get all groups where the user has an approved membership (for group selection entry screen) */
    @GetMapping("/memberships")
    public ResponseEntity<List<UserGroupDto>> getUserMemberships(@RequestHeader("X-User-Id") UUID userId)
    {
        return ResponseEntity.ok(groupService.getUserGroups(userId));
    }

    /** Check if user has approved membership in a specific group */
    @GetMapping("/{groupId}/membership/check")
    public ResponseEntity<ApiResponse> checkMembership(
            @PathVariable UUID groupId,
            @RequestHeader("X-User-Id") UUID userId)
    {
        boolean isMember = groupService.hasApprovedMembership(groupId, userId);
        if (!isMember)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse("User is not an approved member of this group"));
        }
        return ResponseEntity.ok(new ApiResponse("Membership verified"));
    }

    @PostMapping("/{groupId}/join")
    public ResponseEntity<ApiResponse> joinGroup(
            @PathVariable UUID groupId,
            @RequestHeader("X-User-Id") UUID userId)
    {
        groupService.joinGroup(groupId, userId);
        return ResponseEntity.ok(new ApiResponse("Joined group successfully"));
    }

    @PostMapping("/{groupId}/leave")
    public ResponseEntity<ApiResponse> leaveGroup(
            @PathVariable UUID groupId,
            @RequestHeader("X-User-Id") UUID userId)
    {
        groupService.leaveGroup(groupId, userId);
        return ResponseEntity.ok(new ApiResponse("Left group successfully"));
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMember>> getMembers(@PathVariable UUID groupId)
    {
        return ResponseEntity.ok(groupService.getMembers(groupId));
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<ApiResponse> removeMember(
            @PathVariable UUID groupId,
            @PathVariable UUID userId,
            @RequestHeader("X-User-Id") UUID requesterId)
    {
        groupService.removeMember(groupId, userId, requesterId);
        return ResponseEntity.ok(new ApiResponse("Member removed successfully"));
    }

    // ─── Group Follow Endpoints (Admin Only) ────────────────────────────────────

    @PostMapping("/{groupId}/follow")
    public ResponseEntity<GroupFollowDto> followGroup(
            @PathVariable UUID groupId,
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody GroupFollowRequest request)
    {
        GroupFollowDto result = groupService.followGroup(groupId, request.getTargetGroupId(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @DeleteMapping("/{groupId}/follow/{targetGroupId}")
    public ResponseEntity<ApiResponse> unfollowGroup(
            @PathVariable UUID groupId,
            @PathVariable UUID targetGroupId,
            @RequestHeader("X-User-Id") UUID userId)
    {
        groupService.unfollowGroup(groupId, targetGroupId, userId);
        return ResponseEntity.ok(new ApiResponse("Unfollowed group successfully"));
    }

    @GetMapping("/{groupId}/following")
    public ResponseEntity<List<GroupFollowDto>> getFollowedGroups(@PathVariable UUID groupId)
    {
        return ResponseEntity.ok(groupService.getFollowedGroups(groupId));
    }

    // ─── Group Like Endpoints ───────────────────────────────────────────────────

    @PostMapping("/{groupId}/like")
    public ResponseEntity<ApiResponse> likeGroup(
            @PathVariable UUID groupId,
            @RequestHeader("X-User-Id") UUID userId)
    {
        groupService.likeGroup(groupId, userId);
        return ResponseEntity.ok(new ApiResponse("Group liked"));
    }

    @DeleteMapping("/{groupId}/like")
    public ResponseEntity<ApiResponse> unlikeGroup(
            @PathVariable UUID groupId,
            @RequestHeader("X-User-Id") UUID userId)
    {
        groupService.unlikeGroup(groupId, userId);
        return ResponseEntity.ok(new ApiResponse("Group unliked"));
    }

    @GetMapping("/{groupId}/likes/count")
    public ResponseEntity<Integer> getLikeCount(@PathVariable UUID groupId)
    {
        return ResponseEntity.ok(groupService.getLikeCount(groupId));
    }

    @GetMapping("/{groupId}/likes/check")
    public ResponseEntity<Boolean> isLikedByUser(
            @PathVariable UUID groupId,
            @RequestHeader("X-User-Id") UUID userId)
    {
        return ResponseEntity.ok(groupService.isLikedByUser(groupId, userId));
    }
}
