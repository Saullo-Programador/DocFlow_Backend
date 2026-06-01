package com.example.DocFlowBackend.service;

import com.example.DocFlowBackend.enums.FileStatus;
import com.example.DocFlowBackend.exception.GlobalExceptionHandler;
import com.example.DocFlowBackend.entity.FileEntity;
import com.example.DocFlowBackend.entity.FileHistory;
import com.example.DocFlowBackend.entity.User;
import com.example.DocFlowBackend.repository.FileHistoryRepository;
import com.example.DocFlowBackend.repository.FileRepository;
import com.example.DocFlowBackend.repository.UserRepository;
import com.example.DocFlowBackend.storage.FileStorageService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileService {
    private final FileRepository fileRepository;
    private final FileHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final FileStorageService storageService;

    public FileService(FileRepository fileRepository, 
                       FileHistoryRepository historyRepository, 
                       UserRepository userRepository,
                       FileStorageService storageService) {
        this.fileRepository = fileRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
    }

    public FileEntity saveFile(String name, String path, Long size, Long folderId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException("Usuário não encontrado"));

        FileEntity file = new FileEntity();
        file.setName(name);
        file.setFilePath(path);
        file.setSize(size);
        file.setFolderId(folderId);
        file.setUser(user);
        file.setCreatedAt(LocalDateTime.now());
        file.setStatus(FileStatus.ACTIVE);

        FileEntity savedFile = fileRepository.save(file);
        saveHistory(name, "UPLOAD", userId);
        return savedFile;
    }

    public String requestDeletion(String path, Long userId) {
        FileEntity file = fileRepository.findByFilePath(path)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Arquivo não encontrado no banco"));
        file.setStatus(FileStatus.PENDING_DELETION);
        fileRepository.save(file);
        saveHistory(file.getName(), "REQUEST_DELETION", userId);
        return "Exclusão solicitada.";
    }

    public String approveDeletion(Long fileId, Long adminId) throws IOException {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Arquivo não encontrado"));
        storageService.deleteFile(Paths.get(file.getFilePath()));
        fileRepository.delete(file);
        saveHistory(file.getName(), "DELETE_APPROVED", adminId);
        return "Arquivo deletado.";
    }

    public String rejectDeletion(Long fileId, Long adminId) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Arquivo não encontrado"));
        file.setStatus(FileStatus.ACTIVE);
        fileRepository.save(file);
        saveHistory(file.getName(), "DELETE_REJECTED", adminId);
        return "Exclusão rejeitada.";
    }

    public List<FileEntity> listPendingDeletions() {
        return fileRepository.findByStatus(FileStatus.PENDING_DELETION);
    }

    public void deleteFilesByFolderId(Long folderId, Long userId) {
        List<FileEntity> files = fileRepository.findByFolderId(folderId);
        for (FileEntity file : files) {
            try {
                storageService.deleteFile(Paths.get(file.getFilePath())); // ✅ disco
            } catch (IOException e) {
                log.warn("Erro ao deletar arquivo do disco: {}", file.getFilePath(), e);
            }
            fileRepository.delete(file);
            saveHistory(file.getName(), "DELETE (FOLDER CASCADE)", userId);
        }
    }

    public void renameFile(String oldPath, String newName, String newPath, Long userId) {
        Optional<FileEntity> fileOpt = fileRepository.findByFilePath(oldPath);
        fileOpt.ifPresent(file -> {
            String oldName = file.getName();
            file.setName(newName);
            file.setFilePath(newPath);
            fileRepository.save(file);
            saveHistory(oldName, "RENAME -> " + newName, userId);
        });
    }

    public void moveFile(String oldPath, String newPath, Long folderId, Long userId) {
        Optional<FileEntity> fileOpt = fileRepository.findByFilePath(oldPath);
        fileOpt.ifPresent(file -> {
            file.setFilePath(newPath);
            file.setFolderId(folderId); // Agora atualiza também o vínculo da pasta no banco
            fileRepository.save(file);
            saveHistory(file.getName(), "MOVE", userId);
        });
    }

    // Método crucial para sincronizar caminhos de arquivos quando uma PASTA é renomeada
    public void updateFilesPathInFolder(Long folderId, String oldPathPrefix, String newPathPrefix) {
        List<FileEntity> files = fileRepository.findByFolderId(folderId);
        for (FileEntity file : files) {
            String newFilePath = file.getFilePath().replace(oldPathPrefix, newPathPrefix);
            file.setFilePath(newFilePath);
            fileRepository.save(file);
        }
    }

    public Long countTotalFiles() { return fileRepository.count(); }

    public void saveHistory(String docName, String action, Long userId) {
        FileHistory history = new FileHistory();
        history.setDocumentName(docName);
        history.setAction(action);
        history.setUserId(userId);
        history.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        historyRepository.save(history);
    }
}
