package com.example;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.sql.*;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

public class MainSQL {

    /**
     * Simple placeholder resolver for ${VAR_NAME} and ${VAR_NAME:default} patterns
     */
    private static String resolvePlaceholders(String input) {
        if (input == null) return null;
        
        Pattern pattern = Pattern.compile("\\$\\{([^}:]+)(?::([^}]*))?\\}");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String varName = matcher.group(1);
            String defaultValue = matcher.group(2);
            String envValue = System.getenv(varName);
            
            if (envValue != null) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(envValue));
            } else if (defaultValue != null) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(defaultValue));
            } else {
                // Leave placeholder unresolved
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public static void main(String[] args) {
        
        Properties properties = new Properties();
        try (InputStream input = MainSQL.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find application.properties");
                return;
            }
            // Load the properties file
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        String connString = properties.getProperty("spring.datasource.url");
        
        if (connString == null) {
            System.out.println("ERROR: spring.datasource.url property not found in application.properties");
            return;
        }
        
        // Resolve environment variable placeholders
        connString = resolvePlaceholders(connString);
        
        // Check for unresolved placeholders
        if (connString.contains("${AZ_DATABASE_SERVER_NAME}")) {
            System.out.println("ERROR: AZ_DATABASE_SERVER_NAME environment variable is not set");
            System.out.println("Please set the AZ_DATABASE_SERVER_NAME environment variable to your Azure SQL server name");
            return;
        }
        
        String clientId = properties.getProperty("spring.cloud.azure.credential.client-id");
        if (clientId != null) {
            clientId = resolvePlaceholders(clientId);
            if (clientId.contains("${AZURE_CLIENT_ID}")) {
                System.out.println("WARNING: AZURE_CLIENT_ID environment variable is not set");
                System.out.println("This is required for user-assigned managed identity, but optional for system-assigned managed identity");
            }
        }
        
        System.out.println("Connection string: " + connString);
        
        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setURL(connString);
        try (Connection connection = ds.getConnection()) {
            System.out.println("Connected successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
}