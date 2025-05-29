package com.example.controller;

import com.example.service.LLMService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class QueryController {

    private final LLMService llmService;

    public QueryController(LLMService llmService) {
        this.llmService = llmService;
    }

    @PostMapping("/api/query")
    public ResponseEntity<?> handleQuery(@RequestBody Map<String, String> body) throws Exception {
        String userInput = body.get("query");
        String sql = llmService.generateSQL(userInput);
        List<Map<String, Object>> result = llmService.executeSQL(sql);
        return ResponseEntity.ok(result);
    }
    
   
    @GetMapping("/api/query")
    public ResponseEntity<?> getData(@RequestBody Map<String, String> body) throws Exception {
        String userInput = body.get("query");
        String sql = llmService.generateSQL(userInput);
        List<Map<String, Object>> result = llmService.executeSQL(sql);
        System.out.println(result.size());
        return ResponseEntity.ok(result);
    }
}