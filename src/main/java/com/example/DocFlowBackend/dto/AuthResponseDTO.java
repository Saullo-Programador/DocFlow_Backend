package com.example.DocFlowBackend.dto;

import com.example.DocFlowBackend.dto.UserResponseDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponseDTO {

    private String accessToken;
    private String refreshToken;
    private UserResponseDTO user;

    public AuthResponseDTO(String accessToken, String refreshToken, UserResponseDTO user){
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }
}
