package com.example.DocFlowBackend.controller;

import com.example.DocFlowBackend.dto.FolderRequestDTO;
import com.example.DocFlowBackend.dto.FolderResponseDTO;
import com.example.DocFlowBackend.entity.Folder;
import com.example.DocFlowBackend.exception.GlobalExceptionHandler;
import com.example.DocFlowBackend.mapper.FolderMapper;
import com.example.DocFlowBackend.repository.FolderRepository;
import com.example.DocFlowBackend.security.SecurityUtil;
import com.example.DocFlowBackend.service.FolderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/documents/folders")
@CrossOrigin("*")
public class FolderController {

    private final FolderService folderService;
    private final FolderRepository folderRepository;

    public FolderController(FolderService folderService, FolderRepository folderRepository) {
        this.folderService = folderService;
        this.folderRepository = folderRepository;
    }

    @PostMapping
    public ResponseEntity<FolderResponseDTO> create(
            @RequestBody FolderRequestDTO request
    ) {

        Long userId = SecurityUtil.getCurrentUserId();
        Folder folder = folderService.createFolder(request.getName(), request.getParentId(), userId);

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
        Long userId = SecurityUtil.getCurrentUserId();
        Folder folder = folderService.renameFolder(id, name, userId);
        return ResponseEntity.ok(FolderMapper.toResponse(folder));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteByPath(
            @RequestParam String path
    ) {
        Long userId = SecurityUtil.getCurrentUserId();

        Folder folder = folderRepository.findByPath(path)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Pasta não encontrada"));

        String message = folderService.deleteFolder(folder.getId(), userId);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getCount(){ return ResponseEntity.ok(folderService.countTotalFolder());}
}
