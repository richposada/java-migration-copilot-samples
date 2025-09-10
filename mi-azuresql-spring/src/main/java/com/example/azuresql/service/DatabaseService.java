package com.example.azuresql.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Service
public class DatabaseService {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Test database connection using Azure Managed Identity
     */
    public String testConnection() {
        try (Connection connection = dataSource.getConnection()) {
            return "Successfully connected to Azure SQL Database using Managed Identity!";
        } catch (SQLException e) {
            return "Failed to connect to database: " + e.getMessage();
        }
    }

    /**
     * Get database version using JdbcTemplate
     */
    public String getDatabaseVersion() {
        try {
            return jdbcTemplate.queryForObject("SELECT @@VERSION", String.class);
        } catch (Exception e) {
            return "Failed to get database version: " + e.getMessage();
        }
    }

    /**
     * Test a simple query to validate the connection
     */
    public String testQuery() {
        try {
            String result = jdbcTemplate.queryForObject("SELECT 'Hello from Azure SQL!' as message", String.class);
            return result;
        } catch (Exception e) {
            return "Failed to execute test query: " + e.getMessage();
        }
    }
}