package com.example.DocFlowBackend.document;

public class DocumentResponse {
    private String name;
    private String path;
    private String downloadUrl;

    public DocumentResponse(String name, String path, String downloadUrl){
        this.name = name;
        this.path = path;
        this.downloadUrl = downloadUrl;
    }

    public String getName(){ return name; }
    public String getPath() { return path; }
    public String getDownloadUrl() { return downloadUrl; }
}
