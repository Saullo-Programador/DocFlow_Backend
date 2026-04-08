package com.example.DocFlowBackend.dto;

import com.example.DocFlowBackend.enums.UserRole;
import lombok.Getter;

@Getter
public class UserResponseDTO {
    private Long id;
    private String name;
    private String email;
    private UserRole role; // Cargo do usuário

    public UserResponseDTO(Long id, String name, String email, UserRole role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }
}
