package com.example.DocFlowBackend.service;

import com.example.DocFlowBackend.dto.GlobalSearchResponse;
import com.example.DocFlowBackend.dto.SearchResultDTO;
import com.example.DocFlowBackend.entity.FileEntity;
import com.example.DocFlowBackend.entity.Folder;
import com.example.DocFlowBackend.mapper.SearchMapper;
import com.example.DocFlowBackend.repository.FileRepository;
import com.example.DocFlowBackend.repository.FolderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

    private final FileRepository documentRepository;
    private final FolderRepository folderRepository;

    public SearchService(FileRepository documentRepository, FolderRepository folderRepository) {
        this.documentRepository = documentRepository;
        this.folderRepository = folderRepository;
    }

    // 🔵 Buscar só arquivos
    public List<SearchResultDTO> searchFiles(String query) {

        List<FileEntity> documents =
                documentRepository.findByNameContainingIgnoreCase(query);

        return documents.stream()
                .map(SearchMapper::fromFile)
                .toList();
    }

    // 🟡 Buscar só pastas
    public List<SearchResultDTO> searchFolders(String query) {

        List<Folder> folders =
                folderRepository.findByNameContainingIgnoreCase(query);

        return folders.stream()
                .map(SearchMapper::fromFolder)
                .toList();
    }

    public List<SearchResultDTO> searchFilesInFolder(
            Long folderId,
            String query
    ){

        List<FileEntity> files =
                documentRepository
                        .findByFolderIdAndNameContainingIgnoreCase(folderId, query);

        return files.stream()
                .map(SearchMapper::fromFile)
                .toList();
    }

    public GlobalSearchResponse searchGlobal(String query){

        List<SearchResultDTO> files = searchFiles(query);
        List<SearchResultDTO> folders = searchFolders(query);

        return new GlobalSearchResponse(files, folders);
    }
}