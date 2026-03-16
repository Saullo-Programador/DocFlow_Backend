package com.example.DocFlowBackend.mapper;

import com.example.DocFlowBackend.dto.FileResponseDTO;

import java.nio.file.Path;

public class FileMapper {

    public static FileResponseDTO fromPath(Path file, String downloadUrl){

        return new FileResponseDTO(
                file.getFileName().toString(),
                file.toString(),
                downloadUrl
        );

    }

}