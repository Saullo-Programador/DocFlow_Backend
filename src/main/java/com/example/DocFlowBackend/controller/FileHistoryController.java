package com.example.DocFlowBackend.controller;

import com.example.DocFlowBackend.dto.FileHistoryResponseDTO;
import com.example.DocFlowBackend.repository.FileHistoryRepository;
import com.example.DocFlowBackend.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/history")
@CrossOrigin("*")
public class FileHistoryController {

    private final FileHistoryRepository historyRepository;
    private final UserRepository userRepository;

    public FileHistoryController(FileHistoryRepository historyRepository, UserRepository userRepository) {
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<FileHistoryResponseDTO>> getHistory(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(
                historyRepository.findRecentActivities(PageRequest.of(0, limit))
                        .stream()
                        .map(h -> {
                            String userName = userRepository.findById(h.getUserId())
                                    .map(u -> u.getName())
                                    .orElse("Desconhecido");

                            return new FileHistoryResponseDTO(
                                    h.getId(),
                                    h.getDocumentName(),
                                    h.getAction(),
                                    h.getUserId(),
                                    userName,
                                    h.getCreatedAt()
                            );
                        })
                        .toList()
        );
    }
}
