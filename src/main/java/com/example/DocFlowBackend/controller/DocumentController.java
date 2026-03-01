package com.example.DocFlowBackend.controller;

import com.example.DocFlowBackend.document.DocumentResponse;
import com.example.DocFlowBackend.document.FolderContentResponse;
import com.example.DocFlowBackend.storage.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/documents")
public class DocumentController {
    private final FileStorageService storageService;
    private final Path rootPath;

    public DocumentController(FileStorageService storageService) {
        this.storageService = storageService;
        this.rootPath = Paths.get(storageService.getUploadPath())
                .toAbsolutePath()
                .normalize();
    }


    @GetMapping("/")
    public String home() {
        return "DocFlow Backend OK";
    }

    // =========================================================
    // 📁 Criar pasta
    // =========================================================
    @PostMapping("/folders")
    public ResponseEntity<String> createFolder(@RequestParam String path) {
        try {
            Path target = resolveSafePath(path);
            Files.createDirectories(target);
            return ResponseEntity.ok("Pasta criada");

        } catch (SecurityException e) {
            return ResponseEntity.badRequest().body("Caminho inválido");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao criar pasta");
        }
    }

    // =========================================================
    // 📁 Listar pastas raiz
    // =========================================================
    @GetMapping("/folders")
    public ResponseEntity<List<String>> listFolders() throws IOException {

        if (!Files.exists(rootPath)) {
            return ResponseEntity.ok(List.of());
        }

        try (Stream<Path> stream = Files.list(rootPath)) {
            List<String> folders = stream
                    .filter(Files::isDirectory)
                    .map(p -> p.getFileName().toString())
                    .toList();

            return ResponseEntity.ok(folders);
        }
    }

    // =========================================================
    // 📂 Listar conteúdo de pasta
    // =========================================================
    @GetMapping
    public ResponseEntity<FolderContentResponse> list(
            @RequestParam(defaultValue = "") String path,
            HttpServletRequest request
    ) throws IOException {

        Path target;
        try {
            target = resolveSafePath(path);
        } catch (SecurityException e) {
            return ResponseEntity.badRequest()
                    .body(new FolderContentResponse(List.of(), List.of()));
        }

        if (!Files.exists(target)) {
            return ResponseEntity.ok(new FolderContentResponse(List.of(), List.of()));
        }

        String serverUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .build()
                .toUriString();

        try (Stream<Path> stream = Files.list(target)) {

            List<Path> all = stream.toList();

            List<String> folders = all.stream()
                    .filter(Files::isDirectory)
                    .map(p -> p.getFileName().toString())
                    .toList();

            List<DocumentResponse> files = all.stream()
                    .filter(Files::isRegularFile)
                    .map(p -> buildDocResponse(p, serverUrl))
                    .toList();

            return ResponseEntity.ok(new FolderContentResponse(folders, files));
        }
    }

    // =========================================================
    // ⬆️ Upload (com subpasta opcional)
    // =========================================================
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "") String path
    ) {
        try {
            System.out.println("📥 PATH RECEBIDO: " + path);

            Path targetDir = resolveSafePath(path);
            Files.createDirectories(targetDir);

            String fileName = storageService.save(file, targetDir);

            return ResponseEntity.ok(Map.of(
                    "message", "Arquivo salvo com sucesso",
                    "fileName", fileName
            ));

        } catch (SecurityException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Caminho inválido"));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // =========================================================
    // ⬇️ Download (SUPORTA SUBPASTAS)
    // =========================================================
    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam String path) throws IOException {

        Path file;
        try {
            file = resolveSafePath(path);
        } catch (SecurityException e) {
            return ResponseEntity.badRequest().build();
        }

        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(file.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFileName() + "\"")
                .body(resource);
    }


    @GetMapping("/folders/{folderName}/files")
    public ResponseEntity<List<DocumentResponse>> listFilesInsideFolder(
            @PathVariable String folderName,
            HttpServletRequest request
    ) throws IOException {
        String serverUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        //<--- Pegar o Arquivo nos uploads --->
        Path folderPath = Paths.get(storageService.getUploadPath(), folderName);

        //<--- Verificar se Arquivo Existe --->
        if (!Files.exists(folderPath)) return ResponseEntity.ok(List.of());

        List<DocumentResponse> docs = Files
                .list(folderPath)
                .filter(Files::isRegularFile)
                .map(path -> {
                    String fileName = path.getFileName().toString();
                    return new DocumentResponse(
                            fileName,
                            path.toString(),
                            serverUrl + "/documents/download/" + fileName
                    );
                }).toList();

        return ResponseEntity.ok(docs);
    }

    // =========================================================
    // Deletar Arquivo
    // =========================================================
    @DeleteMapping("/delete/file")
    public ResponseEntity<Boolean> deleteFile(@RequestParam String path) throws IOException{
        try{
            Path safePath = resolveSafePath(path);

            boolean deleted = storageService.deleteFile(safePath);

            return ResponseEntity.ok(deleted);

        } catch (SecurityException e) {
            return ResponseEntity.badRequest().body( false);

        }catch (Exception e) {
            return ResponseEntity.internalServerError().body(false);
        }
    }

    // =========================================================
    // Deletar Pasta
    // =========================================================
    @DeleteMapping("/delete/folder")
    public ResponseEntity<Boolean> deleteFolder(@RequestParam String path)throws IOException{
        try {
            return ResponseEntity.ok(storageService.deleteFolder(path));
        } catch (Exception e){
            return ResponseEntity.badRequest().body(false);
        }
    }

    // =========================================================
    // History(Ultimos uploads Feitos)
    // =========================================================
    @GetMapping("/history")
    public ResponseEntity<List<DocumentResponse>> getHistory(
            @RequestParam(defaultValue = "50") int limit
    ) throws IOException{
        if (!Files.exists(rootPath)){
            return ResponseEntity.ok(List.of());
        }
        String serverUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .build()
                .toUriString();

        try (Stream<Path> stream = Files.walk(rootPath)){
            List<DocumentResponse> files = stream
                    .filter(Files::isRegularFile)
                    .sorted((p1,p2) -> {
                        try {
                            return Files.getLastModifiedTime(p2)
                                    .compareTo(Files.getLastModifiedTime(p1));
                        } catch (IOException e){
                            return 0;
                        }
                    })
                    .limit(limit)
                    .map(p-> buildDocResponse(p,serverUrl))
                    .toList();
            return ResponseEntity.ok(files);
        }
    }

    private Path resolveSafePath(String relativePath) {
        Path target = rootPath
                .resolve(relativePath)
                .normalize()
                .toAbsolutePath();

        if (!target.startsWith(rootPath)) {
            throw new SecurityException("Path traversal detectado");
        }

        return target;
    }

    private DocumentResponse buildDocResponse(Path file, String serverUrl) {

        String relative = rootPath
                .relativize(file)
                .toString()
                .replace("\\", "/");

        return new DocumentResponse(
                file.getFileName().toString(),
                file.toString(),
                serverUrl + "/documents/download?path=" + relative
        );
    }
}