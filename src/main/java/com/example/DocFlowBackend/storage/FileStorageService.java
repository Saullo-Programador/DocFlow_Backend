package com.example.DocFlowBackend.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${docflow.upload.path}")
    private String uploadPath;

    public String save(MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new RuntimeException("Arquivo vazio");
        }

        String fileName = UUID.randomUUID() + ".pdf";

        Path path = Paths.get(uploadPath, fileName);

        Files.copy(
                file.getInputStream(),
                path,
                StandardCopyOption.REPLACE_EXISTING
        );

        return path.toString();
    }
}

