package com.example.DocFlowBackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private String password;

    @OneToMany(mappedBy = "user")
    private List<Folder> folders;

    @OneToMany(mappedBy = "user")
    private List<FileEntity> files;

    @Column(name = "created_at")
    private Timestamp createdAt;
}