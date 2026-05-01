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
public class GroupDto
{
    private UUID groupId;
    private UUID creatorId;
    private String name;
    private String description;
    private String privacy;
    private int memberCount;
    private LocalDateTime createdAt;
}
