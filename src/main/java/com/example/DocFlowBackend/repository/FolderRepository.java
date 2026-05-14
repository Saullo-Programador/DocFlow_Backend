package com.example.DocFlowBackend.repository;

import com.example.DocFlowBackend.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository <Folder,Long> {
    List<Folder> findByNameContainingIgnoreCase(String name);
    List<Folder> findByParent_Id(Long parentId);
    Optional<Folder> findByPath(String path); // Busca pasta pelo caminho relativo

    List<Folder> findByParentIsNull();
}
