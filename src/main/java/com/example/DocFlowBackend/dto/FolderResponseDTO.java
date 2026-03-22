package com.example.DocFlowBackend.dto;

import java.time.LocalDateTime;

public class FolderResponseDTO {

    private Long id;
    private String name;
    private Long parentId;
    private LocalDateTime createdAt;
    private Long userId;

    public FolderResponseDTO(Long id, String name, Long parentId, LocalDateTime createdAt, Long userId) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.createdAt = createdAt;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getParentId() {
        return parentId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getUserId() {
        return userId;
    }
}
