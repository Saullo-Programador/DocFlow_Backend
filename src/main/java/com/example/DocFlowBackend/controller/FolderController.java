package com.example.DocFlowBackend.controller;

import com.example.DocFlowBackend.auth.GlobalExceptionHandler;
import com.example.DocFlowBackend.dto.FolderResponseDTO;
import com.example.DocFlowBackend.entity.Folder;
import com.example.DocFlowBackend.entity.User;
import com.example.DocFlowBackend.mapper.FolderMapper;
import com.example.DocFlowBackend.repository.UserRepository;
import com.example.DocFlowBackend.security.SecurityUtil;
import com.example.DocFlowBackend.service.FolderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/folders")
@CrossOrigin("*")
public class FolderController {

    private final FolderService folderService;
    private final UserRepository userRepository;

    public FolderController(FolderService folderService, UserRepository userRepository) {
        this.folderService = folderService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<FolderResponseDTO> create(
            @RequestParam String name,
            @RequestParam(required = false) Long parentId
    ) {

        Long userId = SecurityUtil.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Usuário não encontrado"));

        Folder folder = folderService.createFolder(name, parentId, user);

        return ResponseEntity.ok(FolderMapper.toResponse(folder));
    }

    @GetMapping
    public ResponseEntity<List<FolderResponseDTO>> list() {
        return ResponseEntity.ok(
                folderService.listFolders()
                        .stream()
                        .map(FolderMapper::toResponse)
                        .toList()
        );
    }

    @GetMapping("/children")
    public ResponseEntity<List<FolderResponseDTO>> getChildren(
            @RequestParam Long parentId
    ) {
        return ResponseEntity.ok(
                folderService.getSubFolders(parentId)
                        .stream()
                        .map(FolderMapper::toResponse)
                        .toList()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<FolderResponseDTO> rename(
            @PathVariable Long id,
            @RequestParam String name
    ) {
        Folder folder = folderService.renameFolder(id, name);
        return ResponseEntity.ok(FolderMapper.toResponse(folder));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        folderService.deleteFolder(id);
        return ResponseEntity.noContent().build();
    }
}