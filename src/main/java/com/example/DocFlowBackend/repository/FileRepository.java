package com.example.DocFlowBackend.repository;

import com.example.DocFlowBackend.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    List<FileEntity> findByNameContainingIgnoreCase(String name);
    List<FileEntity> findByFolderIdAndNameContainingIgnoreCase(
            Long folderId,
            String name
    );
}
