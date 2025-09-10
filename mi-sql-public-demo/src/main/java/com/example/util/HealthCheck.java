package com.example.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

/**
 * Health Check utility for Azure SQL Database Managed Identity connection
 * 
 * This utility provides health check functionality that can be used for:
 * - Deployment validation
 * - Monitoring and alerting
 * - Troubleshooting connectivity issues
 */
public class HealthCheck {

    private static final Logger LOGGER = Logger.getLogger(HealthCheck.class.getName());
    
    public static class HealthResult {
        private final boolean healthy;
        private final String message;
        private final long responseTimeMs;
        
        public HealthResult(boolean healthy, String message, long responseTimeMs) {
            this.healthy = healthy;
            this.message = message;
            this.responseTimeMs = responseTimeMs;
        }
        
        public boolean isHealthy() { return healthy; }
        public String getMessage() { return message; }
        public long getResponseTimeMs() { return responseTimeMs; }
        
        @Override
        public String toString() {
            return String.format("HealthResult{healthy=%s, responseTime=%dms, message='%s'}", 
                                healthy, responseTimeMs, message);
        }
    }

    /**
     * Perform a health check on the Azure SQL Database connection
     * 
     * @return HealthResult containing the health status and details
     */
    public static HealthResult checkDatabaseHealth() {
        long startTime = System.currentTimeMillis();
        
        try {
            Properties properties = loadProperties();
            String connectionString = buildConnectionString(properties);
            
            SQLServerDataSource dataSource = createDataSource(connectionString);
            try (Connection connection = dataSource.getConnection()) {
                
                // Perform a simple health check query
                boolean querySuccess = executeHealthCheckQuery(connection);
                long responseTime = System.currentTimeMillis() - startTime;
                
                if (querySuccess) {
                    return new HealthResult(true, "Database connection healthy", responseTime);
                } else {
                    return new HealthResult(false, "Health check query failed", responseTime);
                }
                
            }
        } catch (IOException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            LOGGER.log(Level.WARNING, "Configuration error during health check", e);
            return new HealthResult(false, "Configuration error: " + e.getMessage(), responseTime);
            
        } catch (SQLException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            LOGGER.log(Level.WARNING, "Database connection error during health check", e);
            return new HealthResult(false, "Database connection error: " + e.getMessage(), responseTime);
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            LOGGER.log(Level.SEVERE, "Unexpected error during health check", e);
            return new HealthResult(false, "Unexpected error: " + e.getMessage(), responseTime);
        }
    }

    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = HealthCheck.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IOException("Unable to find application.properties in classpath");
            }
            properties.load(input);
            return properties;
        }
    }

    private static String buildConnectionString(Properties properties) {
        String baseConnectionString = properties.getProperty("AZURE_SQLDB_CONNECTIONSTRING");
        String clientId = properties.getProperty("AZURE_CLIENT_ID");
        
        if (baseConnectionString == null || clientId == null) {
            throw new IllegalArgumentException("Required connection properties not found");
        }
        
        return baseConnectionString + ";msiClientId=" + clientId + ";authentication=ActiveDirectoryMSI";
    }

    private static SQLServerDataSource createDataSource(String connectionString) {
        SQLServerDataSource dataSource = new SQLServerDataSource();
        dataSource.setURL(connectionString);
        return dataSource;
    }

    private static boolean executeHealthCheckQuery(Connection connection) {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT 1 as health_check")) {
            
            return resultSet.next() && resultSet.getInt("health_check") == 1;
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Health check query execution failed", e);
            return false;
        }
    }

    /**
     * Command-line interface for health check
     */
    public static void main(String[] args) {
        LOGGER.info("Starting Azure SQL Database health check...");
        
        HealthResult result = checkDatabaseHealth();
        
        if (result.isHealthy()) {
            LOGGER.info("Health check PASSED: " + result.getMessage() + 
                       " (Response time: " + result.getResponseTimeMs() + "ms)");
            System.exit(0);
        } else {
            LOGGER.severe("Health check FAILED: " + result.getMessage() + 
                         " (Response time: " + result.getResponseTimeMs() + "ms)");
            System.exit(1);
        }
    }
}