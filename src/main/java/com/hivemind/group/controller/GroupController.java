package com.hivemind.group.controller;

import com.hivemind.common.dto.ApiResponse;
import com.hivemind.group.dto.CreateGroupRequest;
import com.hivemind.group.dto.GroupDto;
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

    @GetMapping("/my")
    public ResponseEntity<List<GroupDto>> getMyGroups(@RequestHeader("X-User-Id") UUID userId)
    {
        return ResponseEntity.ok(groupService.getGroupsByCreator(userId));
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
}
