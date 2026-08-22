package com.hivemind.group.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddCommentRequest
{
    @NotBlank(message = "Comment content is required")
    private String content;
}
