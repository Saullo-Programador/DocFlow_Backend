package com.example.DocFlowBackend.repository;

import com.example.DocFlowBackend.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByNameContainingIgnoreCase(String name);
}
