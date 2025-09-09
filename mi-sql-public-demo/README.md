# Azure SQL Database with Managed Identity - Demo Application

This Java application demonstrates how to connect to Azure SQL Database using Azure Managed Identity authentication instead of traditional username/password authentication.

## Prerequisites

1. **Azure SQL Database**: An Azure SQL Database server and database configured for Managed Identity
2. **Managed Identity**: An Azure User Assigned Managed Identity or System Assigned Managed Identity
3. **Environment Variables**: Properly configured environment variables (see Configuration section)

## Configuration

### Environment Variables

Set the following environment variable before running the application:

```bash
export AZ_DATABASE_SERVER_NAME=your-sql-server-name
```

### Application Properties

The application is pre-configured in `src/main/resources/application.properties` with:

```properties
# Azure SQL Database configuration with Managed Identity
spring.datasource.url=jdbc:sqlserver://${AZ_DATABASE_SERVER_NAME}.database.windows.net:1433;database=demo;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;authentication=ActiveDirectoryMSI

# Azure Managed Identity configuration
spring.cloud.azure.credential.managed-identity-enabled=true
spring.cloud.azure.credential.client-id=<your_managed_identity_client_id>
```

**Important**: Replace `<your_managed_identity_client_id>` with your actual Managed Identity client ID.

## Building and Running

### Build the application:

```bash
mvn clean package
```

### Run the application:

```bash
export AZ_DATABASE_SERVER_NAME=your-sql-server-name
java -jar target/demo-1.0-SNAPSHOT.jar
```

## Features

- **Secure Authentication**: Uses Azure Managed Identity instead of passwords
- **Environment Variable Support**: Dynamically substitutes `${AZ_DATABASE_SERVER_NAME}` from environment
- **Modern Spring Configuration**: Leverages Spring Cloud Azure for seamless integration
- **Error Handling**: Provides clear error messages for missing configuration

## Azure Setup Requirements

1. **Create Azure SQL Database** with Managed Identity authentication enabled
2. **Create Managed Identity** (User Assigned or use System Assigned)
3. **Grant Database Access** to the Managed Identity
4. **Deploy Application** to Azure service that supports Managed Identity (App Service, Container Apps, etc.)

## Documentation

For detailed information about Java application migration with GitHub Copilot, please refer to the [Quickstart: Use GitHub Copilot for app modernization and migration of Java applications](https://learn.microsoft.com/azure/developer/java/migration/migrate-github-copilot-app-modernization-for-java-quickstart-assess-migrate).