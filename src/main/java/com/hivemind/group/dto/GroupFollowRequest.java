package com.hivemind.group.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupFollowRequest
{
    @NotNull(message = "Target group ID is required")
    private UUID targetGroupId;
}
