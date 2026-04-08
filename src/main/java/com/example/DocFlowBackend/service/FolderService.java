package com.example.DocFlowBackend.service;

import com.example.DocFlowBackend.exception.GlobalExceptionHandler;
import com.example.DocFlowBackend.entity.Folder;
import com.example.DocFlowBackend.entity.User;
import com.example.DocFlowBackend.repository.FolderRepository;
import com.example.DocFlowBackend.repository.UserRepository;
import com.example.DocFlowBackend.storage.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FolderService {

    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final FileService fileService; 
    private final FileStorageService storageService;

    public FolderService(FolderRepository folderRepository, UserRepository userRepository, FileService fileService, FileStorageService storageService) {
        this.folderRepository = folderRepository;
        this.userRepository = userRepository;
        this.fileService = fileService;
        this.storageService = storageService;
    }

    public Folder createFolder(String name, Long parentId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException("Usuário não encontrado"));

        Folder parent = null;
        String relativePath = "";

        if (parentId != null) {
            parent = folderRepository.findById(parentId)
                    .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Pasta pai não encontrada"));
            String parentPath = (parent.getPath() != null) ? parent.getPath() : ("/" + parent.getName());
            relativePath = parentPath + "/" + name;
        } else {
            relativePath = "/" + name;
        }

        Path rootUploadPath = Paths.get(storageService.getUploadPath()).toAbsolutePath().normalize();
        String safeRelativePath = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        Path folderPath = rootUploadPath.resolve(safeRelativePath);

        try {
            Files.createDirectories(folderPath);
        } catch (IOException e) {
            throw new GlobalExceptionHandler.FileStorageException("Erro ao criar pasta no disco: " + e.getMessage());
        }

        Folder folder = new Folder();
        folder.setName(name);
        folder.setPath(relativePath);
        folder.setParent(parent);
        folder.setUser(user);
        folder.setCreatedAt(LocalDateTime.now());

        Folder savedFolder = folderRepository.save(folder);
        fileService.saveHistory(name, "FOLDER_CREATE", userId);
        return savedFolder;
    }

    public List<Folder> listFolders() { return folderRepository.findAll(); }
    public List<Folder> getSubFolders(Long parentId) { return folderRepository.findByParent_Id(parentId); }

    @Transactional
    public Folder renameFolder(Long folderId, String newName, Long userId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Pasta não encontrada"));

        String oldName = folder.getName();
        String oldPath = folder.getPath();
        
        String parentPath = (folder.getParent() != null && folder.getParent().getPath() != null) ? folder.getParent().getPath() : ""; 
        String newRelativePath = parentPath.isEmpty() ? "/" + newName : parentPath + "/" + newName;

        // 1. Sincronização Física (Disco)
        Path rootUploadPath = Paths.get(storageService.getUploadPath()).toAbsolutePath().normalize();
        Path oldDir = rootUploadPath.resolve(oldPath.startsWith("/") ? oldPath.substring(1) : oldPath);
        Path newDir = rootUploadPath.resolve(newRelativePath.startsWith("/") ? newRelativePath.substring(1) : newRelativePath);

        try {
            if(Files.exists(oldDir)){
                Files.move(oldDir, newDir);
            }
        } catch (IOException e) {
             throw new GlobalExceptionHandler.FileStorageException("Erro ao renomear pasta no disco: " + e.getMessage());
        }

        // 2. Sincronização Lógica (Banco - Recursiva para filhos)
        updatePathsRecursively(folder, oldPath, newRelativePath);
        
        folder.setName(newName);
        folder.setPath(newRelativePath);
        Folder savedFolder = folderRepository.save(folder);

        fileService.saveHistory(oldName, "FOLDER_RENAME -> " + newName, userId);
        return savedFolder;
    }

    private void updatePathsRecursively(Folder folder, String oldPathPrefix, String newPathPrefix) {
        // Atualiza arquivos dentro desta pasta
        fileService.updateFilesPathInFolder(folder.getId(), oldPathPrefix, newPathPrefix);

        // Busca subpastas e atualiza recursivamente
        List<Folder> subFolders = folderRepository.findByParent_Id(folder.getId());
        for (Folder sub : subFolders) {
            String newSubPath = sub.getPath().replaceFirst(oldPathPrefix, newPathPrefix);
            sub.setPath(newSubPath);
            folderRepository.save(sub);
            updatePathsRecursively(sub, oldPathPrefix, newPathPrefix);
        }
    }

    @Transactional
    public String deleteFolder(Long folderId, Long userId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Pasta não encontrada"));

        String folderName = folder.getName(); 
        String path = folder.getPath();

        if(path != null && !path.isBlank()){
             try {
                String safePath = path.startsWith("/") ? path.substring(1) : path;
                storageService.deleteFolder(safePath);
            } catch (IOException e) {
                throw new GlobalExceptionHandler.FileStorageException("Erro ao deletar pasta do disco: " + e.getMessage());
            }
        }
        
        deleteFolderRecursive(folder, userId);
        fileService.saveHistory(folderName, "FOLDER_DELETE", userId);
        return "Pasta '" + folderName + "' deletada com sucesso.";
    }

    private void deleteFolderRecursive(Folder currentFolder, Long userId) {
        List<Folder> subFolders = folderRepository.findByParent_Id(currentFolder.getId());
        for (Folder sub : subFolders) {
            deleteFolderRecursive(sub, userId);
        }
        fileService.deleteFilesByFolderId(currentFolder.getId(), userId);
        folderRepository.delete(currentFolder);
    }

    public Long countTotalFolder(){ return folderRepository.count();}
}
