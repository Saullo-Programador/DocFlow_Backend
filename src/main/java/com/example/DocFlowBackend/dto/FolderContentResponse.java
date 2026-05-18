package com.example.DocFlowBackend.dto;

import java.util.List;

public class FolderContentResponse {

    private FolderResponseDTO currentFolder;
    private List<FolderResponseDTO> folders;
    private List<FileResponseDTO> files;

    // Construtor para raiz (sem pasta atual)
    public FolderContentResponse(List<FolderResponseDTO> folders, List<FileResponseDTO> files) {
        this.currentFolder = null;
        this.folders = folders;
        this.files = files;
    }

    // Construtor completo
    public FolderContentResponse(FolderResponseDTO currentFolder, List<FolderResponseDTO> folders, List<FileResponseDTO> files) {
        this.currentFolder = currentFolder;
        this.folders = folders;
        this.files = files;
    }

    // Vazio (pasta não encontrada)
    public FolderContentResponse(FolderResponseDTO currentFolder, List<FolderResponseDTO> folders, List<FileResponseDTO> files, boolean empty) {
        this(currentFolder, folders, files);
    }


    public FolderResponseDTO getCurrentFolder() { return currentFolder; }
    public List<FolderResponseDTO> getFolders() { return folders; }
    public List<FileResponseDTO> getFiles() { return files; }
}