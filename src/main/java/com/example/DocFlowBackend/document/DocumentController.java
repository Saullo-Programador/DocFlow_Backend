package com.example.DocFlowBackend.document;

import com.example.DocFlowBackend.storage.FileStorageService;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/documents")
public class DocumentController {
    private final FileStorageService storageService;

    public DocumentController(FileStorageService storageService){
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String home() {
        return "DocFlow Backend OK";
    }


    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file")MultipartFile file
    ){
        try {
            String fileName = storageService.save(file);
            Map<String, String> response = Map.of(
                    "message", "Arquivo salvo com sucesso",
                    "fileName", fileName
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> download(@PathVariable String fileName) {
        try {
            Path file = Paths.get(storageService.getUploadPath()).resolve(fileName).normalize();

            // Verificar se o arquivo existe
            if (!Files.exists(file)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(file.toUri());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (MalformedURLException e){
            return ResponseEntity.internalServerError()
                    .body(null);
        }
    }

}
