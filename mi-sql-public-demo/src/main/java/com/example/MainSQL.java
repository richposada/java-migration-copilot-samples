package com.example;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import java.sql.*;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

public class MainSQL {


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
        String clientId = properties.getProperty("AZURE_CLIENT_ID");
        
        // Resolve environment variable placeholders
        if (connString != null) {
            String serverName = System.getenv("AZ_DATABASE_SERVER_NAME");
            if (serverName != null) {
                connString = connString.replace("${AZ_DATABASE_SERVER_NAME}", serverName);
            }
        }
        
        if (clientId != null) {
            String envClientId = System.getenv("AZURE_CLIENT_ID");
            if (envClientId != null) {
                clientId = clientId.replace("${AZURE_CLIENT_ID}", envClientId);
            }
        }
        
        // Add client ID for managed identity if specified and not placeholder
        if (clientId != null && !clientId.contains("${") && !clientId.equals("<your managed identity client id>")) {
            connString = connString + ";msiClientId=" + clientId;
        }
        System.out.print(connString);
        
        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setURL(connString);
        try (Connection connection = ds.getConnection()) {
            System.out.println("Connected successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
}