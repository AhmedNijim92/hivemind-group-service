package com.hivemind.group.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupFollowDto
{
    private UUID groupId;
    private UUID followedGroupId;
    private UUID createdBy;
    private LocalDateTime createdAt;
}
