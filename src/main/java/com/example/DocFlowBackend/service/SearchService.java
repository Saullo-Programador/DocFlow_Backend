package com.example.DocFlowBackend.service;

import com.example.DocFlowBackend.dto.SearchResultDTO;
import com.example.DocFlowBackend.entity.Document;
import com.example.DocFlowBackend.entity.Folder;
import com.example.DocFlowBackend.repository.DocumentRepository;
import com.example.DocFlowBackend.repository.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private FolderRepository folderRepository;

    // 🔵 Buscar só arquivos
    public List<SearchResultDTO> searchFiles(String query) {

        List<Document> documents =
                documentRepository.findByNameContainingIgnoreCase(query);

        return documents.stream()
                .map(doc -> new SearchResultDTO(
                        doc.getId(),
                        doc.getName(),
                        "FILE"
                ))
                .toList();
    }

    // 🟡 Buscar só pastas
    public List<SearchResultDTO> searchFolders(String query) {

        List<Folder> folders =
                folderRepository.findByNameContainingIgnoreCase(query);

        return folders.stream()
                .map(folder -> new SearchResultDTO(
                        folder.getId(),
                        folder.getName(),
                        "FOLDER"
                ))
                .toList();
    }

    // 🟢 Busca global
    public List<SearchResultDTO> globalSearch(String query) {

        List<SearchResultDTO> results = new ArrayList<>();

        results.addAll(searchFiles(query));
        results.addAll(searchFolders(query));

        return results;
    }
}