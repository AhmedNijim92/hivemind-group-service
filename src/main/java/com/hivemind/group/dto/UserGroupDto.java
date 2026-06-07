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
public class UserGroupDto
{
    private UUID groupId;
    private String role;
    private LocalDateTime joinedAt;
    private GroupDto group;
}
