package com.example.DocFlowBackend.document;

import com.example.DocFlowBackend.storage.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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


    @PostMapping("/uploads")
    public ResponseEntity<String> upload(
            @RequestParam("file")MultipartFile file
    ){
        try {
            String path = storageService.save(file);
            return ResponseEntity.ok("Arquivo salvo em: " + path);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erro no upload"+ e.getMessage());
        }
    }
}
