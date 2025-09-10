package com.example.azuresql.controller;

import com.example.azuresql.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/database")
public class DatabaseController {

    @Autowired
    private DatabaseService databaseService;

    @GetMapping("/test-connection")
    public String testConnection() {
        return databaseService.testConnection();
    }

    @GetMapping("/version")
    public String getDatabaseVersion() {
        return databaseService.getDatabaseVersion();
    }

    @GetMapping("/test-query")
    public String testQuery() {
        return databaseService.testQuery();
    }

    @GetMapping("/health")
    public String health() {
        return "Azure SQL Managed Identity Spring application is running!";
    }
}