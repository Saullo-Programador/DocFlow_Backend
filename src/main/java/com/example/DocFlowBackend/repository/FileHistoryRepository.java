package com.example.DocFlowBackend.repository;

import com.example.DocFlowBackend.entity.FileHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileHistoryRepository extends JpaRepository<FileHistory, Long> {
}
