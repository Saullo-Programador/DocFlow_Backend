package com.example.DocFlowBackend.service;

import com.example.DocFlowBackend.entity.Folder;
import com.example.DocFlowBackend.repository.FolderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FolderService {
    private final FolderRepository folderRepository;

    @Value("${docflow.win.path}")
    private String uploadPath;

    public FolderService(FolderRepository folderRepository) {
        this.folderRepository = folderRepository;
    }

    public Folder createFolder(String name, Long parentId, Long userId) {

        try {

            Path folderPath;
            Folder parent = null;

            if (parentId != null) {

                parent = folderRepository.findById(parentId)
                        .orElseThrow(() -> new RuntimeException("Pasta pai não encontrada"));

                folderPath = Paths.get(uploadPath)
                        .resolve(parent.getName())
                        .resolve(name);

            } else {

                folderPath = Paths.get(uploadPath, name);

            }

            Files.createDirectories(folderPath);

            Folder folder = new Folder();
            folder.setName(name);
            folder.setParent(parent);
            folder.setUserId(userId);
            folder.setCreatedAt(LocalDateTime.now());

            return folderRepository.save(folder);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar pasta", e);
        }
    }

    public List<Folder> listFolders(){
        return folderRepository.findAll();
    }

    public List<Folder> getSubFolders(Long parentId){
        return folderRepository.findByParent_Id(parentId);
    }

    public Folder renameFolder(Long id, String newName){
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pasta não encontrada"));

        folder.setName(newName);

        return folderRepository.save(folder);
    }



    public void deleteFolder(Long id){
        folderRepository.deleteById(id);
    }
}
