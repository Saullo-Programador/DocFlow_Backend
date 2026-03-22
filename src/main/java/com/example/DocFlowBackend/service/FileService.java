package com.example.DocFlowBackend.service;

import com.example.DocFlowBackend.exception.GlobalExceptionHandler;
import com.example.DocFlowBackend.entity.FileEntity;
import com.example.DocFlowBackend.entity.FileHistory;
import com.example.DocFlowBackend.entity.User;
import com.example.DocFlowBackend.repository.FileHistoryRepository;
import com.example.DocFlowBackend.repository.FileRepository;
import com.example.DocFlowBackend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FileService {
    private final FileRepository fileRepository;
    private final FileHistoryRepository historyRepository;
    private final UserRepository userRepository; 

    public FileService(FileRepository fileRepository, FileHistoryRepository historyRepository, UserRepository userRepository) {
        this.fileRepository = fileRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
    }

    public FileEntity saveFile(
            String name,
            String path,
            Long size,
            Long folderId,
            Long userId 
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException("Usuário não encontrado"));

        FileEntity file = new FileEntity();

        file.setName(name);
        file.setFilePath(path);
        file.setSize(size);
        file.setFolderId(folderId);
        file.setUser(user);
        file.setCreatedAt(LocalDateTime.now());

        FileEntity savedFile = fileRepository.save(file);

        saveHistory(name, "UPLOAD", userId);

        return savedFile;
    }

    public String deleteFile(String path, Long userId) {
        Optional<FileEntity> fileOpt = fileRepository.findByFilePath(path);

        if(fileOpt.isPresent()) {
            FileEntity file = fileOpt.get();
            fileRepository.delete(file);
            saveHistory(file.getName(), "DELETE", userId);
            return "Arquivo '" + file.getName() + "' deletado com sucesso.";
        } else {
            // Se o arquivo não existe no banco, mas foi deletado do disco, retornamos ok
            return "Arquivo deletado (registro não encontrado no banco).";
        }
    }

    public void deleteFilesByFolderId(Long folderId, Long userId) {
        List<FileEntity> files = fileRepository.findByFolderId(folderId);
        for (FileEntity file : files) {
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

    public void moveFile(String oldPath, String newPath, Long userId) {
        Optional<FileEntity> fileOpt = fileRepository.findByFilePath(oldPath);

        fileOpt.ifPresent(file -> {
            file.setFilePath(newPath);
            fileRepository.save(file);
            saveHistory(file.getName(), "MOVE", userId);
        });
    }

    public void saveHistory(String docName, String action, Long userId) {
        FileHistory history = new FileHistory();
        history.setDocumentName(docName);
        history.setAction(action);
        history.setUserId(userId);
        history.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        historyRepository.save(history);
    }
}
