package com.example.DocFlowBackend.controller;

import com.example.DocFlowBackend.dto.UserResponseDTO;
import com.example.DocFlowBackend.entity.User;
import com.example.DocFlowBackend.mapper.UserMapper;
import com.example.DocFlowBackend.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin("*")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponseDTO> listUsers() {
        return userService.listUsers()
                .stream()
                .map(UserMapper::toDTO)
                .toList();
    }
}
