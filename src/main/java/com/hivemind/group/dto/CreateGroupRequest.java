package com.hivemind.group.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupRequest
{
    @NotBlank(message = "Group name is required")
    private String name;

    private String description;

    private String privacy = "PUBLIC";
}
