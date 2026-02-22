package com.example.DocFlowBackend.document;

import java.util.List;

public class FolderContentResponse {

    private List<String> folders;
    private List<DocumentResponse> files;

    public FolderContentResponse(
            List<String> folders,
            List<DocumentResponse> files) {
        this.folders = folders;
        this.files = files;
    }

    public List<String> getFolders() { return folders; }
    public List<DocumentResponse> getFiles() { return files; }
}
