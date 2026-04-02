package com.example.DocFlowBackend.controller;

import com.example.DocFlowBackend.exception.GlobalExceptionHandler;
import com.example.DocFlowBackend.dto.FileResponseDTO;
import com.example.DocFlowBackend.dto.FolderContentResponse;
import com.example.DocFlowBackend.dto.UploadResponseDTO;
import com.example.DocFlowBackend.security.SecurityUtil;
import com.example.DocFlowBackend.service.FileService;
import com.example.DocFlowBackend.storage.FileStorageService;
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
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Stream;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/files")
public class FileController {
    private final FileStorageService storageService;
    private final FileService fileService;
    private final Path rootPath;

    public FileController(FileStorageService storageService, FileService fileService) {
        this.fileService = fileService;
        this.storageService = storageService;
        this.rootPath = Paths.get(storageService.getUploadPath())
                .toAbsolutePath()
                .normalize();
    }

    // =========================================================
    // 📂 Listar conteúdo de pasta
    // =========================================================
    @GetMapping
    public ResponseEntity<FolderContentResponse> list(
            @RequestParam(defaultValue = "") String path
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

            List<FileResponseDTO> files = all.stream()
                    .filter(Files::isRegularFile)
                    .map(p -> {
                        try {
                            return new FileResponseDTO(
                                    null,
                                    p.getFileName().toString(),
                                    p.toString(),
                                    serverUrl + "/files/download?path=" +
                                            rootPath.relativize(p).toString().replace("\\", "/"),
                                    Files.size(p),
                                    LocalDateTime.ofInstant(Files.getLastModifiedTime(p).toInstant(), ZoneId.systemDefault()),
                                    null
                            );
                        } catch (IOException e) {
                            return null;
                        }
                    })
                    .toList();

            return ResponseEntity.ok(new FolderContentResponse(folders, files));
        }
    }

    // =========================================================
    // ⬆️ Upload (com subpasta opcional)
    // =========================================================
    @PostMapping("/upload")
    public ResponseEntity<UploadResponseDTO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "") String path
    ) throws IOException {

        if (file.isEmpty()) {
            throw new GlobalExceptionHandler.FileStorageException("Arquivo vazio");
        }

        Path targetDir = resolveSafePath(path);

        try {
            Files.createDirectories(targetDir);
        } catch (Exception e) {
            throw new GlobalExceptionHandler.FileStorageException("Erro ao criar diretório");
        }

        String fileName = storageService.save(file, targetDir);

        Long userId = SecurityUtil.getCurrentUserId();

        Path savedFile = targetDir.resolve(fileName);

        fileService.saveFile(
                fileName,
                savedFile.toString(),
                file.getSize(),
                null,
                userId
        );

        return ResponseEntity.ok(
                new UploadResponseDTO("Arquivo salvo com sucesso", fileName)
        );
    }

    // =========================================================
    // ⬇️ Download (SUPORTA SUBPASTAS)
    // =========================================================
    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam String path) throws IOException {

        Path file = resolveSafePath(path);

        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            throw new GlobalExceptionHandler.ResourceNotFoundException("Arquivo não encontrado");
        }

        Resource resource = new UrlResource(file.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFileName() + "\"")
                .body(resource);
    }


    // =========================================================
    // Deletar Arquivo
    // =========================================================
    @DeleteMapping
    public ResponseEntity<String> deleteFile(@RequestParam String path) throws IOException {

        Path safePath = resolveSafePath(path);

        boolean deleted = storageService.deleteFile(safePath);

        if (!deleted) {
            throw new GlobalExceptionHandler.FileStorageException("Erro ao deletar arquivo");
        }

        Long userId = SecurityUtil.getCurrentUserId();
        String message = fileService.deleteFile(safePath.toString(), userId);

        return ResponseEntity.ok(message);
    }

    @PutMapping("/rename")
    public ResponseEntity<Boolean> renameFile(
            @RequestParam String path,
            @RequestParam String newName
    ) {

        try {

            Path original = resolveSafePath(path);

            if (!Files.exists(original)) {
                return ResponseEntity.notFound().build();
            }

            if (newName == null || newName.isBlank()) {
                return ResponseEntity.badRequest().body(false);
            }

            Path newPath = original.resolveSibling(newName);

            Files.move(original, newPath, StandardCopyOption.REPLACE_EXISTING);

            Long userId = SecurityUtil.getCurrentUserId();
            fileService.renameFile(original.toString(), newName, newPath.toString(), userId);

            return ResponseEntity.ok(true);

        } catch (SecurityException e) {
            return ResponseEntity.badRequest().body(false);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(false);
        }
    }

    @PutMapping("/move")
    public ResponseEntity<Boolean> moveFile(
            @RequestParam String path,
            @RequestParam String destination
    ) {

        try {

            Path source = resolveSafePath(path);
            Path targetFolder = resolveSafePath(destination);

            if (!Files.exists(source)) {
                return ResponseEntity.notFound().build();
            }

            Files.createDirectories(targetFolder);

            Path target = targetFolder.resolve(source.getFileName());

            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);

            Long userId = SecurityUtil.getCurrentUserId();
            fileService.moveFile(source.toString(), target.toString(), userId);

            return ResponseEntity.ok(true);

        } catch (SecurityException e) {
            return ResponseEntity.badRequest().body(false);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(false);
        }
    }

    // =========================================================
    // History(Ultimos uploads Feitos)
    // =========================================================
    @GetMapping("/latest-uploads")
    public ResponseEntity<List<FileResponseDTO>> getHistory(
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
            List<FileResponseDTO> files = stream
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
                    .map(p -> {
                        try {
                            return new FileResponseDTO(
                                    null,
                                    p.getFileName().toString(),
                                    p.toString(),
                                    serverUrl + "/files/download?path=" +
                                            rootPath.relativize(p).toString().replace("\\", "/"),
                                    Files.size(p),
                                    LocalDateTime.ofInstant(Files.getLastModifiedTime(p).toInstant(), ZoneId.systemDefault()),
                                    null
                            );
                        } catch (IOException e) {
                            return null;
                        }
                    })
                    .toList();
            return ResponseEntity.ok(files);
        }
    }

    // =========================================================
    // Contagem Total de Arquivos
    // =========================================================
    @GetMapping("/count")
    public ResponseEntity<Long> getCount() {
        return ResponseEntity.ok(fileService.countTotalFiles());
    }

    private Path resolveSafePath(String relativePath) {
        Path target = rootPath
                .resolve(relativePath)
                .normalize()
                .toAbsolutePath();

        if (!target.startsWith(rootPath)) {
            throw new GlobalExceptionHandler.InvalidPathException("Path traversal detectado");
        }

        return target;
    }
}
