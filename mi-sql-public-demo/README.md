# Azure SQL Database with Managed Identity Demo

This project demonstrates how to connect to Azure SQL Database using **Azure Managed Identity** authentication with Java and the SQL Server JDBC driver. The implementation showcases a secure, credential-free way to connect to Azure SQL Database from Java applications.

## Overview

Azure Managed Identity provides an automatically managed identity in Microsoft Entra ID for applications to use when connecting to resources that support Microsoft Entra authentication. This eliminates the need to manage credentials in your application code.

## Prerequisites

Before running this application, ensure you have:

1. **Azure Subscription** with appropriate permissions
2. **Azure SQL Database** server and database created
3. **User-Assigned Managed Identity** created and configured
4. **Java 17+** installed
5. **Maven 3.6+** installed

## Azure Setup

### 1. Create Azure SQL Database

```bash
# Create resource group
az group create --name myResourceGroup --location eastus

# Create Azure SQL Server
az sql server create \
    --name myserver \
    --resource-group myResourceGroup \
    --location eastus \
    --admin-user sqladmin \
    --admin-password YourPassword123!

# Create database
az sql db create \
    --resource-group myResourceGroup \
    --server myserver \
    --name demo \
    --service-objective Basic
```

### 2. Create User-Assigned Managed Identity

```bash
# Create managed identity
az identity create \
    --resource-group myResourceGroup \
    --name myManagedIdentity

# Get the client ID (you'll need this for configuration)
az identity show \
    --resource-group myResourceGroup \
    --name myManagedIdentity \
    --query clientId \
    --output tsv
```

### 3. Configure SQL Database for Managed Identity

```bash
# Set Microsoft Entra admin for SQL server
az sql server ad-admin create \
    --resource-group myResourceGroup \
    --server-name myserver \
    --display-name myManagedIdentity \
    --object-id $(az identity show --resource-group myResourceGroup --name myManagedIdentity --query principalId --output tsv)
```

### 4. Create Database User for Managed Identity

Connect to your Azure SQL Database using SQL Server Management Studio, Azure Data Studio, or sqlcmd, and run:

```sql
-- Create user for managed identity
CREATE USER [myManagedIdentity] FROM EXTERNAL PROVIDER;

-- Grant necessary permissions
ALTER ROLE db_datareader ADD MEMBER [myManagedIdentity];
ALTER ROLE db_datawriter ADD MEMBER [myManagedIdentity];
ALTER ROLE db_ddladmin ADD MEMBER [myManagedIdentity];
```

## Configuration

### 1. Update application.properties

Edit `src/main/resources/application.properties` and replace the placeholders:

```properties
# Replace with your actual Azure SQL Server name
AZURE_SQLDB_CONNECTIONSTRING=jdbc:sqlserver://myserver.database.windows.net:1433;database=demo;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;

# Replace with your managed identity client ID
AZURE_CLIENT_ID=12345678-1234-1234-1234-123456789012
```

### 2. Environment Variables (Alternative)

Instead of modifying the properties file, you can set environment variables:

```bash
export AZ_DATABASE_SERVER_NAME=myserver
export AZURE_CLIENT_ID=12345678-1234-1234-1234-123456789012
```

## Building and Running

### Local Development

```bash
# Clone the repository
git clone <repository-url>
cd mi-sql-public-demo

# Compile the application
mvn clean compile

# Package the application
mvn package

# Run the application (requires Azure environment with managed identity)
java -jar target/demo-1.0-SNAPSHOT.jar
```

### Running on Azure

This application is designed to run in Azure environments that support Managed Identity:

- **Azure Virtual Machines** with assigned managed identity
- **Azure Container Instances** with managed identity
- **Azure App Service** with managed identity enabled
- **Azure Container Apps** with managed identity
- **Azure Kubernetes Service (AKS)** with pod identity

## Features

- **Secure Authentication**: Uses Azure Managed Identity for credential-free database access
- **Connection Validation**: Validates database connectivity with test queries
- **Error Handling**: Comprehensive error handling and logging
- **Property Validation**: Validates required configuration properties
- **Logging**: Detailed logging for troubleshooting and monitoring
- **Health Check Utility**: Built-in health check for monitoring and deployment validation

## Running the Application

### Main Application

```bash
# Run the main application
java -jar target/demo-1.0-SNAPSHOT.jar

# Or run the main class directly
java -cp target/classes com.example.MainSQL
```

### Health Check Utility

The application includes a health check utility that can be used for monitoring and deployment validation:

```bash
# Run health check
java -cp target/classes com.example.util.HealthCheck

# Health check will exit with code 0 for success, 1 for failure
# Perfect for monitoring scripts and deployment pipelines
```

The health check provides:
- Connection validation with response time measurement
- Detailed error reporting and logging
- Exit codes for automation and monitoring systems
- Lightweight operation suitable for frequent monitoring

## Troubleshooting

### Common Issues

1. **Authentication Failed**
   - Verify the managed identity has been assigned to your Azure resource
   - Ensure the managed identity has proper permissions on the SQL database
   - Check that the client ID is correct in configuration

2. **Connection Timeout**
   - Verify network connectivity to Azure SQL Database
   - Check firewall rules on the SQL server
   - Ensure the connection string is correct

3. **Property Validation Errors**
   - Replace placeholder values in `application.properties`
   - Verify environment variables are set correctly
   - Check for typos in server names and client IDs

### Logging

The application provides detailed logging to help with troubleshooting:

- **INFO**: Normal operation and success messages
- **WARNING**: Non-fatal issues and fallback operations  
- **SEVERE**: Critical errors and failures

### Testing Connection

The application performs these validation steps:

1. Loads and validates configuration properties
2. Constructs the connection string with managed identity parameters
3. Establishes connection to Azure SQL Database
4. Executes test query to verify connectivity
5. Reports connection status and server information

## Security Considerations

- **No Credentials**: The application doesn't store or handle database passwords
- **Encrypted Connection**: All database connections use SSL/TLS encryption
- **Identity Validation**: Managed identity provides secure, token-based authentication
- **Minimal Permissions**: Grant only the minimum required database permissions

## Dependencies

- **Spring Cloud Azure**: Provides Azure integration and managed identity support
- **SQL Server JDBC Driver**: Microsoft's official JDBC driver for SQL Server
- **Java Logging API**: Standard Java logging framework

## Documentation

For more information about Java application migration with GitHub Copilot, see:
- [Quickstart: Use GitHub Copilot for app modernization and migration of Java applications](https://learn.microsoft.com/azure/developer/java/migration/migrate-github-copilot-app-modernization-for-java-quickstart-assess-migrate)

For Azure Managed Identity documentation:
- [What are managed identities for Azure resources?](https://docs.microsoft.com/en-us/azure/active-directory/managed-identities-azure-resources/overview)
- [Use managed identities to connect Azure SQL Database](https://docs.microsoft.com/en-us/azure/app-service/app-service-web-tutorial-connect-msi)

## License

This project is licensed under the MIT License - see the LICENSE file for details.