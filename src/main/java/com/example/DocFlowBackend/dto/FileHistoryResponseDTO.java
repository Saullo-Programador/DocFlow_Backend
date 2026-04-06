package com.example.DocFlowBackend.dto;

import java.sql.Timestamp;

public class FileHistoryResponseDTO {

    private Long id;
    private String documentName;
    private String action;
    private Long userId;
    private String userName; // Nome do usuário para o frontend
    private Timestamp createdAt;

    public FileHistoryResponseDTO(Long id, String documentName, String action, Long userId, String userName, Timestamp createdAt) {
        this.id = id;
        this.documentName = documentName;
        this.action = action;
        this.userId = userId;
        this.userName = userName;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getDocumentName() { return documentName; }
    public String getAction() { return action; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public Timestamp getCreatedAt() { return createdAt; }
}
