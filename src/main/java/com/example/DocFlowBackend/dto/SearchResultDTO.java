package com.example.DocFlowBackend.dto;


import com.example.DocFlowBackend.enums.SearchType;

public class SearchResultDTO {

    private Long id;
    private String name;
    private SearchType type;
    private String path;

    public SearchResultDTO(Long id, String name, SearchType type, String path) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.path = path;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SearchType getType() {
        return type;
    }

    public void setType(SearchType type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
