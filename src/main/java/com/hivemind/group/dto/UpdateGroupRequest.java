package com.hivemind.group.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGroupRequest
{
    private String name;
    private String description;
    private String profilePictureUrl;
    private String coverPictureUrl;
}
