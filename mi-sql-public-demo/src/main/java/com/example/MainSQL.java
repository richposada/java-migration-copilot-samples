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
        
        if (connString == null) {
            System.out.println("spring.datasource.url property not found in application.properties");
            return;
        }
        
        // Substitute environment variables in connection string
        connString = substituteEnvironmentVariables(connString);
        
        System.out.println("Connection string: " + connString);
        
        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setURL(connString);
        try (Connection connection = ds.getConnection()) {
            System.out.println("Connected successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Substitutes environment variables in the format ${VARIABLE_NAME} with their actual values.
     * @param input the string containing environment variable placeholders
     * @return the string with environment variables substituted
     */
    private static String substituteEnvironmentVariables(String input) {
        if (input == null) {
            return null;
        }
        
        String result = input;
        // Pattern to match ${VARIABLE_NAME}
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\$\\{([^}]+)\\}");
        java.util.regex.Matcher matcher = pattern.matcher(input);
        
        while (matcher.find()) {
            String envVarName = matcher.group(1);
            String envVarValue = System.getenv(envVarName);
            
            if (envVarValue != null) {
                result = result.replace(matcher.group(0), envVarValue);
            } else {
                System.err.println("Warning: Environment variable " + envVarName + " is not set");
            }
        }
        
        return result;
    }

    
}