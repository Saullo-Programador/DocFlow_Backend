package com.example.DocFlowBackend.controller;

import com.example.DocFlowBackend.dto.GlobalSearchResponse;
import com.example.DocFlowBackend.dto.SearchResultDTO;
import com.example.DocFlowBackend.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    //Somente Arquivos
    @GetMapping("/files")
    public ResponseEntity<List<SearchResultDTO>> searchFiles(@RequestParam String query){

        if(query == null || query.isBlank()){
            throw new IllegalArgumentException("Query inválida");
        }

        return ResponseEntity.ok(searchService.searchFiles(query));
    }

    //Melhoria futura Nome da Rota
    //Arquivos dentro da pasta
    @GetMapping("/files-in-folder")
    public ResponseEntity<List<SearchResultDTO>> searchFilesInFolder(
            @RequestParam Long folderId,
            @RequestParam String query
    ){
        return ResponseEntity.ok(
                searchService.searchFilesInFolder(folderId, query)
        );
    }

    //Somente Pastas
    @GetMapping("/folders")
    public ResponseEntity<List<SearchResultDTO>> searchFolders(@RequestParam String query){
        return ResponseEntity.ok(searchService.searchFolders(query));
    }

    //Global (Pastas e arquivos)
    @GetMapping("/global")
    public ResponseEntity<GlobalSearchResponse> searchGlobal(
            @RequestParam String query) {

        return ResponseEntity.ok(
                searchService.searchGlobal(query)
        );
    }
}
