package com.example.DocFlowBackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "files")
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "folder_id")
    private Long folderId;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    private Long size;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
