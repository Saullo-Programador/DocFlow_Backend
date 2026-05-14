package com.example.DocFlowBackend.repository;

import com.example.DocFlowBackend.entity.FileEntity;
import com.example.DocFlowBackend.enums.FileStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    List<FileEntity> findByNameContainingIgnoreCase(String name);
    List<FileEntity> findByFolderIdAndNameContainingIgnoreCase(
            Long folderId,
            String name
    );
    Optional<FileEntity> findByFilePath(String filePath);
    List<FileEntity> findByFolderId(Long folderId);
    
    // Busca arquivos por pasta e status (ex: ATIVO)
    List<FileEntity> findByFolderIdAndStatus(Long folderId, FileStatus status);
    
    // Busca todos com status pendente
    List<FileEntity> findByStatus(FileStatus status);

    List<FileEntity> findByFolderIdIsNull();

    Long countByStatus (FileStatus status);
}
