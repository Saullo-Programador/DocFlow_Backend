package com.example.DocFlowBackend.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "documents_history")
public class FileHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_name")
    private String documentName;

    private String action;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "created_at")
    private Timestamp createdAt;
}
