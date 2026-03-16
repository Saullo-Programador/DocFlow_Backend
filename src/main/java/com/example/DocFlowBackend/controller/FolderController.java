package com.example.DocFlowBackend.controller;

import com.example.DocFlowBackend.dto.FolderResponseDTO;
import com.example.DocFlowBackend.entity.Folder;
import com.example.DocFlowBackend.mapper.FolderMapper;
import com.example.DocFlowBackend.service.FolderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/folders")
@CrossOrigin("*")
public class FolderController {
    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @PostMapping
    public FolderResponseDTO create(
            @RequestParam String name,
            @RequestParam(required = false) Long parentId
    ){
        Folder folder = folderService.createFolder(name, parentId, 1L);

        return FolderMapper.toResponse(folder);
    }

    @GetMapping
    public List<FolderResponseDTO> list(){
        return folderService.listFolders()
                .stream()
                .map(FolderMapper::toResponse)
                .toList();
    }

    @GetMapping("/children")
    public List<FolderResponseDTO> getChildren(
            @RequestParam Long parentId
    ){
        return folderService.getSubFolders(parentId)
                .stream()
                .map(FolderMapper::toResponse)
                .toList();
    }

    @PutMapping("/{id}")
    public FolderResponseDTO rename(
            @PathVariable Long id,
            @RequestParam String name
    ){
        Folder folder = folderService.renameFolder(id, name);
        return FolderMapper.toResponse(folder);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable Long id){
        folderService.deleteFolder(id);
        return ResponseEntity.ok(true);
    }
}
