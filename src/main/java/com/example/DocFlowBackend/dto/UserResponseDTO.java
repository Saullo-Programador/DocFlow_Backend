package com.example.DocFlowBackend.dto;

import lombok.Getter;

@Getter
public class UserResponseDTO {
    private Long id;
    private String name;
    private String email;

    public UserResponseDTO(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
}
