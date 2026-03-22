package com.example.DocFlowBackend.dto;

import java.util.List;

public class FolderContentResponse {

    private List<String> folders;
    private List<FileResponseDTO> files;

    public FolderContentResponse(
            List<String> folders,
            List<FileResponseDTO> files) {
        this.folders = folders;
        this.files = files;
    }

    public List<String> getFolders() { return folders; }
    public List<FileResponseDTO> getFiles() { return files; }
}
