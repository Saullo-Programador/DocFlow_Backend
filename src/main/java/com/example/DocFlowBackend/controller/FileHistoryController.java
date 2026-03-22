package com.example.DocFlowBackend.controller;

import com.example.DocFlowBackend.dto.FileHistoryResponseDTO;
import com.example.DocFlowBackend.repository.FileHistoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/history")
@CrossOrigin("*")
public class FileHistoryController {

    private final FileHistoryRepository historyRepository;

    public FileHistoryController(FileHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @GetMapping
    public ResponseEntity<List<FileHistoryResponseDTO>> getHistory() {
        return ResponseEntity.ok(
                historyRepository.findAll()
                        .stream()
                        .map(h -> new FileHistoryResponseDTO(
                                h.getId(),
                                h.getDocumentName(),
                                h.getAction(),
                                h.getUserId(),
                                h.getCreatedAt()
                        ))
                        .toList()
        );
    }
}
