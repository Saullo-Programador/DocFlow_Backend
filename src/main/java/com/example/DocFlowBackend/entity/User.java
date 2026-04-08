package com.example.DocFlowBackend.entity;

import com.example.DocFlowBackend.enums.UserRole;
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

    @Column(unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role; // ADMIN, MANAGER, EMPLOYEE

    @OneToMany(mappedBy = "user")
    private List<Folder> folders;

    @OneToMany(mappedBy = "user")
    private List<FileEntity> files;

    @Column(name = "created_at")
    private Timestamp createdAt;
}
