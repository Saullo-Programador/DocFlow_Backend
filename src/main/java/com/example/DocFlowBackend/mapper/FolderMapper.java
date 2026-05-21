package com.example.DocFlowBackend.mapper;

import com.example.DocFlowBackend.dto.FolderResponseDTO;
import com.example.DocFlowBackend.entity.Folder;

public class FolderMapper {
    public static FolderResponseDTO toResponse(Folder folder) {
        Long parentId = null;
        if (folder.getParent() != null) {
            parentId = folder.getParent().getId();
        }
        return new FolderResponseDTO(
                folder.getId(),
                folder.getName(),
                folder.getPath(), // ✅ adicionar
                parentId,
                folder.getCreatedAt(),
                folder.getUser() != null ? folder.getUser().getId() : null
        );
    }
}