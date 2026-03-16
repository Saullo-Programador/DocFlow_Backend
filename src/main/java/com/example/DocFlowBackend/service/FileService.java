package com.example.DocFlowBackend.service;

import com.example.DocFlowBackend.entity.FileEntity;
import com.example.DocFlowBackend.repository.FileRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FileService {
    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public FileEntity saveFile(
            String name,
            String path,
            Long size,
            Long folderId,
            Long userId
    ) {

        FileEntity file = new FileEntity();

        file.setName(name);
        file.setFilePath(path);
        file.setSize(size);
        file.setFolderId(folderId);
        file.setUserId(userId);
        file.setCreatedAt(LocalDateTime.now());

        return fileRepository.save(file);
    }
}
