package com.example.DocFlowBackend.dto;

import java.time.LocalDateTime;

public class FolderResponseDTO {
    private Long id;
    private String name;
    private String path; // ✅ adicionar
    private Long parentId;
    private LocalDateTime createdAt;
    private Long userId;

    public FolderResponseDTO(Long id, String name, String path, Long parentId, LocalDateTime createdAt, Long userId) {
        this.id = id;
        this.name = name;
        this.path = path; // ✅
        this.parentId = parentId;
        this.createdAt = createdAt;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPath() { return path; } // ✅
    public Long getParentId() { return parentId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Long getUserId() { return userId; }
}
