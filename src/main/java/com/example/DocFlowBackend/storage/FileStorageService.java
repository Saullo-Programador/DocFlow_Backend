package com.example.DocFlowBackend.storage;

import com.example.DocFlowBackend.document.DocumentResponse;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Getter
@Service
public class FileStorageService {
    //local
    @Value("${docflow.win.path}")
    //service
    //@Value("${docflow.upload.path}")
    private String uploadPath;


    public String save(MultipartFile file, Path targetDir) throws IOException {
        // Verificar se o arquivo está vazio
        if (file.isEmpty()) {
            throw new RuntimeException("Arquivo vazio");
        }

        // Obter o nome original do arquivo
        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null) {
            throw new RuntimeException("Arquivo inválido");
        }
        // Converter o nome para minúsculas para validar a extensão
        String lowerFileName = originalFileName.toLowerCase();

        // Validar extensões permitidas: PDF, Word e Excel
        if (!lowerFileName.endsWith(".pdf") &&
            !lowerFileName.endsWith(".docx") &&
            !lowerFileName.endsWith(".xlsx")) {
            throw new RuntimeException("Apenas arquivos PDF, Word (.docx) ou Excel (.xlsx) são permitidos");
        }

        // Obter extensão do arquivo (geralmente ".pdf")
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));

        // Gerar nome aleatório com UUID
        String fileName = UUID.randomUUID() + extension;

        // Criar o path completo
        Path path = Paths.get(uploadPath).resolve(fileName);

        // Salvar o arquivo no sistema de arquivos, sobrescrevendo se existir
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    public List<DocumentResponse> listFiles (String serverUrl) throws IOException{
        Path root = Paths.get(uploadPath);

        if (!Files.exists(root)){
            return List.of();
        }

        try (Stream<Path> paths = Files.list(root)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(path -> {
                        String fileName = path.getFileName().toString();
                        String url = serverUrl + "/documents/download/" + fileName;

                        return new DocumentResponse(
                                fileName,
                                path.toString(),
                                url
                        );
                    })
                    .toList();
        }
    }
}

