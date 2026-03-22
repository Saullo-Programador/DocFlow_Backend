package com.example.DocFlowBackend.dto;

import java.time.LocalDateTime;

public class FileResponseDTO {
    private Long id;
    private String name;
    private String path;
    private String downloadUrl;
    private Long size;
    private LocalDateTime createdAt;
    private Long folderId;

    public FileResponseDTO(Long id, String name, String path, String downloadUrl, Long size, LocalDateTime createdAt, Long folderId){
        this.id = id;
        this.name = name;
        this.path = path;
        this.downloadUrl = downloadUrl;
        this.size = size;
        this.createdAt = createdAt;
        this.folderId = folderId;
    }

    public Long getId() { return id; }
    public String getName(){ return name; }
    public String getPath() { return path; }
    public String getDownloadUrl() { return downloadUrl; }
    public Long getSize() { return size; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Long getFolderId() { return folderId; }
}
