package com.example.DocFlowBackend.repository;

import com.example.DocFlowBackend.entity.FileHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FileHistoryRepository extends JpaRepository<FileHistory, Long> {
    
    // Busca as atividades mais recentes ordenadas pela data
    @Query("SELECT f FROM FileHistory f ORDER BY f.createdAt DESC")
    List<FileHistory> findRecentActivities(Pageable pageable);
}
