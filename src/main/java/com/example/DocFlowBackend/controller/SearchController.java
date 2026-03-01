package com.example.DocFlowBackend.controller;

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
    @Autowired
    private SearchService searchService;

    //Somente Arquivos
    @GetMapping("/files")
    public ResponseEntity<List<SearchResultDTO>> searchFiles(@RequestParam String query){
        return ResponseEntity.ok(searchService.searchFiles(query));
    }

    //Somente Pastas
    @GetMapping("/folders")
    public ResponseEntity<List<SearchResultDTO>> searchFolders(@RequestParam String query){
        return ResponseEntity.ok(searchService.searchFolders(query));
    }

    //Global (Pastas e arquivos)
    @GetMapping("/global")
    public ResponseEntity<List<SearchResultDTO>> searchGlobal(@RequestParam String query){
        return ResponseEntity.ok(searchService.globalSearch(query));
    }
}
