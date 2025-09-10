# Azure SQL with Managed Identity - Spring Boot Demo

This project demonstrates how to connect to Azure SQL Database using Azure Managed Identity in a Spring Boot application.

## Features

- Spring Boot application with Azure SQL Database connectivity
- Azure Managed Identity authentication (no passwords required)
- REST API endpoints for testing database connectivity
- Proper Spring Cloud Azure integration

## Configuration

The application is configured to use Azure Managed Identity for authentication with Azure SQL Database.

### Required Environment Variables

- `AZ_DATABASE_SERVER_NAME`: Your Azure SQL Server name (without .database.windows.net)
- `AZURE_CLIENT_ID`: The client ID of your User-Assigned Managed Identity

### Application Properties

The `application.properties` file contains:

```properties
# Azure SQL Database Configuration with Managed Identity
spring.datasource.url=jdbc:sqlserver://${AZ_DATABASE_SERVER_NAME}.database.windows.net:1433;database=demo;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;authentication=ActiveDirectoryMSI

# Enable Azure managed identity for Spring Cloud Azure
spring.cloud.azure.credential.managed-identity-enabled=true
spring.cloud.azure.credential.client-id=${AZURE_CLIENT_ID}
```

## Dependencies

Key dependencies used in this project:

- `spring-boot-starter-web`: Web layer and REST controllers
- `spring-boot-starter-jdbc`: JDBC support
- `spring-cloud-azure-starter`: Azure integration for Spring Boot
- `mssql-jdbc`: Microsoft SQL Server JDBC driver

## API Endpoints

Once running, the application exposes the following endpoints:

- `GET /api/database/health`: Check if the application is running
- `GET /api/database/test-connection`: Test database connectivity
- `GET /api/database/version`: Get database version information  
- `GET /api/database/test-query`: Execute a simple test query

## Building and Running

```bash
# Build the project
mvn clean compile

# Run the application (requires Azure environment with configured managed identity)
mvn spring-boot:run
```

## Prerequisites

1. Azure SQL Database instance
2. User-Assigned Managed Identity configured in Azure
3. Proper access permissions granted to the managed identity on the SQL database
4. Application running in an Azure environment that supports managed identity (e.g., Azure App Service, Azure VM, Azure Container Instances)

## Azure Setup

1. Create an Azure SQL Database
2. Create a User-Assigned Managed Identity  
3. Grant the managed identity access to the SQL database
4. Configure the application environment variables
5. Deploy to an Azure service that supports managed identity

For more information about setting up managed identity with Azure SQL, refer to the [Azure documentation](https://docs.microsoft.com/en-us/azure/active-directory/managed-identities-azure-resources/overview).