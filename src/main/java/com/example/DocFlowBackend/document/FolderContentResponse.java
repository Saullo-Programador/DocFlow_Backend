package com.example.DocFlowBackend.document;

import com.example.DocFlowBackend.dto.FileResponseDTO;

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
