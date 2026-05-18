package com.example.DocFlowBackend.dto;

import com.example.DocFlowBackend.enums.UserRole;
import lombok.Getter;

@Getter
public class UpdateUserRequestDTO {
    private String name;
    private UserRole role;
}