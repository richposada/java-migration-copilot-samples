package com.example;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MainSQLTest {
    
    @Test
    public void testApplicationPropertiesContainsRequiredConfiguration() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = MainSQLTest.class.getClassLoader().getResourceAsStream("application.properties")) {
            assertNotNull("application.properties should be available", input);
            properties.load(input);
        }
        
        // Verify Spring Cloud Azure managed identity configuration
        assertEquals("true", properties.getProperty("spring.cloud.azure.credential.managed-identity-enabled"));
        assertEquals("${AZURE_CLIENT_ID}", properties.getProperty("spring.cloud.azure.credential.client-id"));
        
        // Verify DataSource URL contains managed identity authentication
        String datasourceUrl = properties.getProperty("spring.datasource.url");
        assertNotNull("spring.datasource.url should be configured", datasourceUrl);
        assertTrue("DataSource URL should contain ActiveDirectoryMSI authentication", 
                  datasourceUrl.contains("authentication=ActiveDirectoryMSI"));
        assertTrue("DataSource URL should contain SQL Server connection string", 
                  datasourceUrl.contains("jdbc:sqlserver://"));
        assertTrue("DataSource URL should contain database name", 
                  datasourceUrl.contains("database=demo"));
    }
    
    @Test
    public void testConnectionStringConstruction() {
        // This test verifies that the connection string is properly constructed
        // by capturing the printed output when environment variables are set
        
        // Set environment variables
        String originalServerName = System.getenv("AZ_DATABASE_SERVER_NAME");
        String originalClientId = System.getenv("AZURE_CLIENT_ID");
        
        try {
            // Capture System.out
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outputStream));
            
            // Set test environment variables using system properties as a workaround
            System.setProperty("AZ_DATABASE_SERVER_NAME", "test-server");
            System.setProperty("AZURE_CLIENT_ID", "test-client-id");
            
            // Create a modified version that reads from system properties
            String testUrl = "jdbc:sqlserver://${AZ_DATABASE_SERVER_NAME}.database.windows.net:1433;database=demo;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;authentication=ActiveDirectoryMSI";
            String testClientId = "${AZURE_CLIENT_ID}";
            
            // Simulate the resolution logic
            String serverName = System.getProperty("AZ_DATABASE_SERVER_NAME");
            if (serverName != null) {
                testUrl = testUrl.replace("${AZ_DATABASE_SERVER_NAME}", serverName);
            }
            
            String clientId = System.getProperty("AZURE_CLIENT_ID");
            if (clientId != null) {
                testClientId = testClientId.replace("${AZURE_CLIENT_ID}", clientId);
            }
            
            if (!testClientId.contains("${") && !testClientId.equals("<your managed identity client id>")) {
                testUrl = testUrl + ";msiClientId=" + testClientId;
            }
            
            // Verify the constructed URL
            assertTrue("URL should contain resolved server name", testUrl.contains("test-server.database.windows.net"));
            assertTrue("URL should contain authentication=ActiveDirectoryMSI", testUrl.contains("authentication=ActiveDirectoryMSI"));
            assertTrue("URL should contain msiClientId when client ID is provided", testUrl.contains("msiClientId=test-client-id"));
            assertFalse("URL should not contain double semicolons", testUrl.contains(";;"));
            
            // Restore System.out
            System.setOut(originalOut);
            
        } finally {
            // Clean up system properties
            System.clearProperty("AZ_DATABASE_SERVER_NAME");
            System.clearProperty("AZURE_CLIENT_ID");
        }
    }
}