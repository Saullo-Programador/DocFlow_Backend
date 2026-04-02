package com.example.DocFlowBackend.service;

import com.example.DocFlowBackend.exception.GlobalExceptionHandler;
import com.example.DocFlowBackend.entity.Folder;
import com.example.DocFlowBackend.entity.User;
import com.example.DocFlowBackend.repository.FolderRepository;
import com.example.DocFlowBackend.repository.UserRepository;
import com.example.DocFlowBackend.storage.FileStorageService;
import org.springframework.http.ResponseEntity;
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
    private final FileService fileService; // Para histórico
    private final FileStorageService storageService; // Para criar pasta física

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
            
            // Assume que o pai já tem o path correto (se for antigo e nulo, usa o nome)
            String parentPath = (parent.getPath() != null) ? parent.getPath() : ("/" + parent.getName());
            relativePath = parentPath + "/" + name;
        } else {
            relativePath = "/" + name;
        }

        // Criar pasta física
        Path rootUploadPath = Paths.get(storageService.getUploadPath()).toAbsolutePath().normalize();
        
        // Remove a barra inicial para evitar problemas com resolve (se houver)
        String safeRelativePath = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        Path folderPath = rootUploadPath.resolve(safeRelativePath);

        try {
            Files.createDirectories(folderPath);
        } catch (IOException e) {
            throw new GlobalExceptionHandler.FileStorageException("Erro ao criar pasta no disco: " + e.getMessage());
        }

        Folder folder = new Folder();
        folder.setName(name);
        folder.setPath(relativePath); // Salva o caminho no banco
        folder.setParent(parent);
        folder.setUser(user);
        folder.setCreatedAt(LocalDateTime.now());

        Folder savedFolder = folderRepository.save(folder);

        fileService.saveHistory(name, "FOLDER_CREATE", userId);

        return savedFolder;
    }

    public List<Folder> listFolders() {
        return folderRepository.findAll();
    }

    public List<Folder> getSubFolders(Long parentId) {
        return folderRepository.findByParent_Id(parentId);
    }

    public Folder renameFolder(Long folderId, String newName, Long userId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Pasta não encontrada"));

        String oldName = folder.getName();
        String oldPath = folder.getPath();
        
        String parentPath = (folder.getParent() != null && folder.getParent().getPath() != null) 
                ? folder.getParent().getPath() 
                : ""; 
        
        String newRelativePath = parentPath + "/" + newName;
        if(parentPath.isEmpty()) newRelativePath = "/" + newName;

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

        folder.setName(newName);
        folder.setPath(newRelativePath);
        
        Folder savedFolder = folderRepository.save(folder);

        fileService.saveHistory(oldName, "FOLDER_RENAME -> " + newName, userId);

        return savedFolder;
    }

    @Transactional
    public String deleteFolder(Long folderId, Long userId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Pasta não encontrada"));

        String folderName = folder.getName(); // Guardar nome para histórico

        // 1. Deletar Fisicamente (apaga tudo dentro do disco)
        String path = folder.getPath();
        if(path != null && !path.isBlank()){
             try {
                String safePath = path.startsWith("/") ? path.substring(1) : path;
                boolean deleted = storageService.deleteFolder(safePath);
                
                // Se o diretório existe e não foi possível deletar
                Path physicalPath = Paths.get(storageService.getUploadPath()).resolve(safePath);
                if (Files.exists(physicalPath) && !deleted) {
                    throw new GlobalExceptionHandler.FileStorageException("Falha ao remover o diretório físico.");
                }

            } catch (IOException e) {
                e.printStackTrace();
                throw new GlobalExceptionHandler.FileStorageException("Erro de I/O ao apagar pasta do disco: " + e.getMessage());
            }
        }
        
        // 2. Limpar Banco de Dados Recursivamente
        try {
            deleteFolderRecursive(folder, userId);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao limpar registros do banco: " + e.getMessage());
        }

        // 3. Salvar histórico da ação principal
        fileService.saveHistory(folderName, "FOLDER_DELETE", userId);

        return "Pasta '" + folderName + "' e todo seu conteúdo foram deletados com sucesso.";
    }

    private void deleteFolderRecursive(Folder currentFolder, Long userId) {
        // Encontrar subpastas
        List<Folder> subFolders = folderRepository.findByParent_Id(currentFolder.getId());

        // Recursão para deletar subpastas primeiro
        for (Folder sub : subFolders) {
            deleteFolderRecursive(sub, userId);
        }

        // Deletar arquivos desta pasta
        fileService.deleteFilesByFolderId(currentFolder.getId(), userId);

        // Deletar a pasta em si
        folderRepository.delete(currentFolder);
    }

    public Long countTotalFolder(){ return folderRepository.count();}
}
