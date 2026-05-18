package com.example.DocFlowBackend.controller;

import com.example.DocFlowBackend.dto.FileResponseDTO;
import com.example.DocFlowBackend.dto.FolderContentResponse;
import com.example.DocFlowBackend.dto.FolderRequestDTO;
import com.example.DocFlowBackend.dto.FolderResponseDTO;
import com.example.DocFlowBackend.entity.Folder;
import com.example.DocFlowBackend.enums.FileStatus;
import com.example.DocFlowBackend.exception.GlobalExceptionHandler;
import com.example.DocFlowBackend.mapper.FileMapper;
import com.example.DocFlowBackend.mapper.FolderMapper;
import com.example.DocFlowBackend.repository.FileRepository;
import com.example.DocFlowBackend.repository.FolderRepository;
import com.example.DocFlowBackend.security.SecurityUtil;
import com.example.DocFlowBackend.service.FolderService;
import com.example.DocFlowBackend.storage.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/documents/folders")
@CrossOrigin("*")
public class FolderController {

    private final FolderService folderService;
    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;
    private final FileStorageService storageService;

    public FolderController(
            FolderService folderService,
            FolderRepository folderRepository,
            FileRepository fileRepository,
            FileStorageService storageService
    ) {
        this.folderService = folderService;
        this.folderRepository = folderRepository;
        this.fileRepository = fileRepository;
        this.storageService = storageService;
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
    public ResponseEntity<FolderContentResponse> list(@RequestParam(defaultValue = "") String path) {

        Folder currentFolder = null;
        Long folderId = null;

        if (!path.isEmpty()) {
            String dbPath = path.startsWith("/") ? path : "/" + path;
            Optional<Folder> found = folderRepository.findByPath(dbPath);

            if (found.isEmpty()) {
                return ResponseEntity.ok(new FolderContentResponse(List.of(), List.of()));
            }

            currentFolder = found.get();
            folderId = currentFolder.getId();
        }

        List<FolderResponseDTO> folders = folderRepository
                .findByParent_Id(folderId)
                .stream()
                .map(FolderMapper::toResponse) // ✅ Retorna objeto completo com ID
                .toList();

        String serverUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath().build().toUriString();

        List<FileResponseDTO> files = fileRepository
                .findByFolderIdAndStatus(folderId, FileStatus.ACTIVE)
                .stream()
                .map(f -> FileMapper.fromEntity(f,
                        serverUrl + "/documents/files/download?path=" + f.getFilePath()))
                .toList();

        FolderResponseDTO currentFolderDTO = currentFolder != null
                ? FolderMapper.toResponse(currentFolder)
                : null;

        // Raiz: construtor sem currentFolder
        if (currentFolderDTO == null) {
            return ResponseEntity.ok(new FolderContentResponse(folders, files));
        }

        // Subpasta: construtor com currentFolder
        return ResponseEntity.ok(new FolderContentResponse(currentFolderDTO, folders, files));
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
