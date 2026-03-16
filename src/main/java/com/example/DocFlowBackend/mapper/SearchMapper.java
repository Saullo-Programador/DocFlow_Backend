package com.example.DocFlowBackend.mapper;

import com.example.DocFlowBackend.dto.SearchResultDTO;
import com.example.DocFlowBackend.entity.FileEntity;
import com.example.DocFlowBackend.entity.Folder;
import com.example.DocFlowBackend.enums.SearchType;

public class SearchMapper {

    public static SearchResultDTO fromFile(FileEntity file){
        return new SearchResultDTO(
                file.getId(),
                file.getName(),
                SearchType.FILE,
                file.getFilePath()
        );
    }

    public static SearchResultDTO fromFolder(Folder folder){
        return new SearchResultDTO(
                folder.getId(),
                folder.getName(),
                SearchType.FOLDER,
                null
        );
    }

}