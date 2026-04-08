package com.example.DocFlowBackend.controller;

import com.example.DocFlowBackend.enums.FileStatus;
import com.example.DocFlowBackend.exception.GlobalExceptionHandler;
import com.example.DocFlowBackend.dto.FileResponseDTO;
import com.example.DocFlowBackend.dto.FolderContentResponse;
import com.example.DocFlowBackend.dto.UploadResponseDTO;
import com.example.DocFlowBackend.entity.FileEntity;
import com.example.DocFlowBackend.entity.Folder;
import com.example.DocFlowBackend.mapper.FileMapper;
import com.example.DocFlowBackend.repository.FileRepository;
import com.example.DocFlowBackend.repository.FolderRepository;
import com.example.DocFlowBackend.security.SecurityUtil;
import com.example.DocFlowBackend.service.FileService;
import com.example.DocFlowBackend.storage.FileStorageService;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/files")
public class FileController {
    private final FileStorageService storageService;
    private final FileService fileService;
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final Path rootPath;

    public FileController(FileStorageService storageService, 
                          FileService fileService, 
                          FileRepository fileRepository, 
                          FolderRepository folderRepository) {
        this.fileService = fileService;
        this.storageService = storageService;
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
        this.rootPath = Paths.get(storageService.getStoragePath())
                .toAbsolutePath()
                .normalize();
    }

    @GetMapping
    public ResponseEntity<FolderContentResponse> list(@RequestParam(defaultValue = "") String path) {
        
        Long folderId = null;
        if (!path.isEmpty()) {
            String dbPath = path.startsWith("/") ? path : "/" + path;
            Optional<Folder> folder = folderRepository.findByPath(dbPath);
            if (folder.isEmpty()) {
                return ResponseEntity.ok(new FolderContentResponse(List.of(), List.of()));
            }
            folderId = folder.get().getId();
        }

        List<String> folders = folderRepository.findByParent_Id(folderId)
                .stream()
                .map(Folder::getName)
                .toList();

        String serverUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        
        List<FileEntity> fileEntities;
        if (folderId == null) {
            fileEntities = fileRepository.findByFolderIdAndStatus(null, FileStatus.ACTIVE);
        } else {
            fileEntities = fileRepository.findByFolderIdAndStatus(folderId, FileStatus.ACTIVE);
        }

        List<FileResponseDTO> files = fileEntities.stream()
                .map(f -> FileMapper.fromEntity(f, serverUrl + "/files/download?path=" + f.getFilePath()))
                .toList();

        return ResponseEntity.ok(new FolderContentResponse(folders, files));
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponseDTO> upload(
            @RequestParam("file") MultipartFile file, 
            @RequestParam(defaultValue = "") String path
    ) throws IOException {
        
        if (file.isEmpty()) throw new GlobalExceptionHandler.FileStorageException("Arquivo vazio");

        Long folderId = null;
        if (!path.isEmpty()) {
            String dbPath = path.startsWith("/") ? path : "/" + path;
            Folder folder = folderRepository.findByPath(dbPath)
                    .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Pasta de destino não existe no banco"));
            folderId = folder.getId();
        }

        Path targetDir = resolveSafePath(path);
        Files.createDirectories(targetDir);
        String fileName = storageService.save(file, targetDir);
        Path savedFile = targetDir.resolve(fileName);

        Long userId = SecurityUtil.getCurrentUserId();
        fileService.saveFile(fileName, savedFile.toString(), file.getSize(), folderId, userId);

        return ResponseEntity.ok(new UploadResponseDTO("Arquivo salvo com sucesso", fileName));
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam String path) throws IOException {
        Path file = resolveSafePath(path);
        if (!Files.exists(file) || !Files.isRegularFile(file)) throw new GlobalExceptionHandler.ResourceNotFoundException("Arquivo não encontrado");
        Resource resource = new UrlResource(file.toUri());
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"").body(resource);
    }

    @DeleteMapping
    public ResponseEntity<String> requestDelete(@RequestParam String path) {
        Long userId = SecurityUtil.getCurrentUserId();
        String message = fileService.requestDeletion(path, userId);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/pending-deletions")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<FileResponseDTO>> listPending() {
        String serverUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        return ResponseEntity.ok(fileService.listPendingDeletions().stream()
                .map(f -> FileMapper.fromEntity(f, serverUrl + "/files/download?path=" + f.getFilePath()))
                .toList());
    }

    @PostMapping("/approve-deletion/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<String> approveDelete(@PathVariable Long id) throws IOException {
        Long adminId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(fileService.approveDeletion(id, adminId));
    }

    @PostMapping("/reject-deletion/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<String> rejectDelete(@PathVariable Long id) {
        Long adminId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(fileService.rejectDeletion(id, adminId));
    }

    @PutMapping("/rename")
    public ResponseEntity<Boolean> renameFile(@RequestParam String path, @RequestParam String newName) {
        Path original = resolveSafePath(path);
        Path newPath = original.resolveSibling(newName);
        try {
            Files.move(original, newPath, StandardCopyOption.REPLACE_EXISTING);
            Long userId = SecurityUtil.getCurrentUserId();
            fileService.renameFile(original.toString(), newName, newPath.toString(), userId);
            return ResponseEntity.ok(true);
        } catch (Exception e) { return ResponseEntity.internalServerError().body(false); }
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

            Files.createDirectories(targetFolder); // Garante que a pasta destino exista fisicamente

            Path target = targetFolder.resolve(source.getFileName());

            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);

            // Buscar a pasta destino no banco para obter o folderId
            String dbDestinationPath = destination.startsWith("/") ? destination : "/" + destination;
            Folder destinationFolder = folderRepository.findByPath(dbDestinationPath)
                    .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Pasta de destino não encontrada no banco"));

            Long userId = SecurityUtil.getCurrentUserId();
            fileService.moveFile(source.toString(), target.toString(), destinationFolder.getId(), userId);

            return ResponseEntity.ok(true);

        } catch (SecurityException e) {
            return ResponseEntity.badRequest().body(false);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(false);
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getCount() { return ResponseEntity.ok(fileService.countTotalFiles()); }

    private Path resolveSafePath(String relativePath) {
        Path target = rootPath.resolve(relativePath).normalize().toAbsolutePath();
        if (!target.startsWith(rootPath)) throw new GlobalExceptionHandler.InvalidPathException("Path traversal detectado");
        return target;
    }
}
