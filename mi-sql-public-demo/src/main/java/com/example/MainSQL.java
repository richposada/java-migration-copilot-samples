package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

/**
 * Azure SQL Database Managed Identity Connection Demo
 * 
 * This application demonstrates how to connect to Azure SQL Database
 * using Managed Identity authentication with the SQL Server JDBC driver.
 */
public class MainSQL {

    private static final Logger LOGGER = Logger.getLogger(MainSQL.class.getName());
    private static final String PROPERTIES_FILE = "application.properties";
    
    // Required property keys
    private static final String AZURE_SQLDB_CONNECTIONSTRING = "AZURE_SQLDB_CONNECTIONSTRING";
    private static final String AZURE_CLIENT_ID = "AZURE_CLIENT_ID";

    public static void main(String[] args) {
        LOGGER.info("Starting Azure SQL Database Managed Identity connection test...");
        
        try {
            Properties properties = loadProperties();
            String connectionString = buildConnectionString(properties);
            testConnection(connectionString);
            
            LOGGER.info("Application completed successfully.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Application failed with error: " + e.getMessage(), e);
            System.exit(1);
        }
    }

    /**
     * Load application properties from the classpath
     */
    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        
        try (InputStream input = MainSQL.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                throw new IOException("Unable to find " + PROPERTIES_FILE + " in classpath");
            }
            
            properties.load(input);
            LOGGER.info("Successfully loaded properties from " + PROPERTIES_FILE);
            
            // Validate required properties
            validateRequiredProperties(properties);
            
            return properties;
        }
    }

    /**
     * Validate that all required properties are present and not empty
     */
    private static void validateRequiredProperties(Properties properties) throws IllegalArgumentException {
        String connectionString = properties.getProperty(AZURE_SQLDB_CONNECTIONSTRING);
        String clientId = properties.getProperty(AZURE_CLIENT_ID);
        
        if (connectionString == null || connectionString.trim().isEmpty()) {
            throw new IllegalArgumentException(AZURE_SQLDB_CONNECTIONSTRING + " is required and cannot be empty");
        }
        
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new IllegalArgumentException(AZURE_CLIENT_ID + " is required and cannot be empty");
        }
        
        // Check for placeholder values
        if (clientId.contains("<your managed identity client id>")) {
            throw new IllegalArgumentException(AZURE_CLIENT_ID + " contains placeholder value. Please update with your actual Managed Identity client ID");
        }
        
        if (connectionString.contains("${AZ_DATABASE_SERVER_NAME}")) {
            throw new IllegalArgumentException(AZURE_SQLDB_CONNECTIONSTRING + " contains placeholder value. Please update with your actual server name");
        }
        
        LOGGER.info("All required properties validated successfully");
    }

    /**
     * Build the complete connection string with Managed Identity authentication
     */
    private static String buildConnectionString(Properties properties) {
        String baseConnectionString = properties.getProperty(AZURE_SQLDB_CONNECTIONSTRING);
        String clientId = properties.getProperty(AZURE_CLIENT_ID);
        
        String connectionString = baseConnectionString + ";msiClientId=" + clientId + ";authentication=ActiveDirectoryMSI";
        
        LOGGER.info("Built connection string for Managed Identity authentication");
        // Log connection string without sensitive information
        String logSafeConnectionString = connectionString.replaceAll("msiClientId=[^;]+", "msiClientId=***");
        LOGGER.info("Connection string: " + logSafeConnectionString);
        
        return connectionString;
    }

    /**
     * Test the database connection and perform a simple query
     */
    private static void testConnection(String connectionString) throws SQLException {
        LOGGER.info("Attempting to connect to Azure SQL Database...");
        
        SQLServerDataSource dataSource = new SQLServerDataSource();
        dataSource.setURL(connectionString);
        
        try (Connection connection = dataSource.getConnection()) {
            LOGGER.info("Successfully connected to Azure SQL Database using Managed Identity!");
            
            // Test the connection with a simple query
            performConnectionTest(connection);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to Azure SQL Database: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Perform a simple test query to validate the connection
     */
    private static void performConnectionTest(Connection connection) throws SQLException {
        LOGGER.info("Performing connection validation test...");
        
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT 1 as test_value, GETDATE() as current_time")) {
            
            if (resultSet.next()) {
                int testValue = resultSet.getInt("test_value");
                String currentTime = resultSet.getString("current_time");
                
                LOGGER.info("Connection test successful - Test value: " + testValue + ", Server time: " + currentTime);
            } else {
                LOGGER.warning("Connection test query returned no results");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Connection test query failed: " + e.getMessage(), e);
            throw e;
        }
    }
}