package com.example.DocFlowBackend.dto;

public class UploadResponseDTO {

    private String message;
    private String fileName;

    public UploadResponseDTO(String message, String fileName) {
        this.message = message;
        this.fileName = fileName;
    }

    public String getMessage() {
        return message;
    }

    public String getFileName() {
        return fileName;
    }
}