package com.example.DocFlowBackend.dto;

import java.util.List;

public class GlobalSearchResponse {

    private List<SearchResultDTO> files;
    private List<SearchResultDTO> folders;

    public GlobalSearchResponse(List<SearchResultDTO> files, List<SearchResultDTO> folders) {
        this.files = files;
        this.folders = folders;
    }

    public List<SearchResultDTO> getFiles() {
        return files;
    }

    public void setFiles(List<SearchResultDTO> files) {
        this.files = files;
    }

    public List<SearchResultDTO> getFolders() {
        return folders;
    }

    public void setFolders(List<SearchResultDTO> folders) {
        this.folders = folders;
    }
}
