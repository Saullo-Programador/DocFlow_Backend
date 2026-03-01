package com.example.DocFlowBackend.repository;

import com.example.DocFlowBackend.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FolderRepository extends JpaRepository <Folder,Long> {
    List<Folder> findByNameContainingIgnoreCase(String name);
}
