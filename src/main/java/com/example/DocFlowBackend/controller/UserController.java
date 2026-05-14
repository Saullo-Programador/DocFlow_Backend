package com.example.DocFlowBackend.controller;

import com.example.DocFlowBackend.dto.UserResponseDTO;
import com.example.DocFlowBackend.mapper.UserMapper;
import com.example.DocFlowBackend.security.SecurityUtil;
import com.example.DocFlowBackend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')") // Restrição de negócio
    public ResponseEntity<List<UserResponseDTO>> listUsers() {
        return ResponseEntity.ok(userService.listUsers()
                .stream()
                .map(UserMapper::toDTO)
                .toList()
        );
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMe(){
        Long userId = SecurityUtil.getCurrentUserId();
        UserResponseDTO user = userService.getById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Long> getCount(){ 
        return ResponseEntity.ok(userService.countTotalUsers());
    }

    @DeleteMapping("/deleteUser")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<UserResponseDTO> deleteUser(@RequestParam String userId){
        return ResponseEntity.ok(userService.deleteUser(userId));
    }
}
