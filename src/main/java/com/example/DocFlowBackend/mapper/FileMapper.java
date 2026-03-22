package com.example.DocFlowBackend.mapper;

import com.example.DocFlowBackend.dto.FileResponseDTO;
import com.example.DocFlowBackend.entity.FileEntity;

import java.nio.file.Path;

public class FileMapper {

    public static FileResponseDTO fromEntity(FileEntity file, String downloadUrl){
        return new FileResponseDTO(
                file.getId(),
                file.getName(),
                file.getFilePath(),
                downloadUrl,
                file.getSize(),
                file.getCreatedAt(),
                file.getFolderId()
        );
    }
}
