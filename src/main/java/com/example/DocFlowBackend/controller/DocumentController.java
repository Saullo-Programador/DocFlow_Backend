package com.example.DocFlowBackend.controller;

import com.example.DocFlowBackend.dto.FolderResponseDTO;
import com.example.DocFlowBackend.entity.FileEntity;
import com.example.DocFlowBackend.entity.Folder;
import com.example.DocFlowBackend.mapper.FolderMapper;
import com.example.DocFlowBackend.repository.FileRepository;
import com.example.DocFlowBackend.repository.FolderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/documents")
@CrossOrigin("*")
public class DocumentController {

    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;

    public DocumentController(FolderRepository folderRepository, FileRepository fileRepository) {
        this.folderRepository = folderRepository;
        this.fileRepository = fileRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getFolderContent(
            @RequestParam(defaultValue = "") String path
    ) {
        List<Folder> folders;
        List<FileEntity> files;

        if (path.isEmpty()) {
            // Raiz: pastas sem pai
            folders = folderRepository.findByParentIsNull();
            files = fileRepository.findByFolderIdIsNull();
        } else {
            // Busca a pasta pelo path e retorna filhos
            folders = folderRepository.findByPath(path)
                    .map(f -> folderRepository.findByParent_Id(f.getId()))
                    .orElse(List.of());

            files = folderRepository.findByPath(path)
                    .map(f -> fileRepository.findByFolderId(f.getId()))
                    .orElse(List.of());
        }

        List<FolderResponseDTO> folderDTOs = folders.stream()
                .map(FolderMapper::toResponse)
                .toList();

        return ResponseEntity.ok(Map.of(
                "folders", folderDTOs,
                "files", files,
                "currentPath", path
        ));
    }
}