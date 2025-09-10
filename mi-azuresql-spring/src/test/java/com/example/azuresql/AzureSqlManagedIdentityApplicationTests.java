package com.example.azuresql;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "AZ_DATABASE_SERVER_NAME=test-server",
    "AZURE_CLIENT_ID=test-client-id",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.cloud.azure.credential.managed-identity-enabled=false"
})
class AzureSqlManagedIdentityApplicationTests {

    @Test
    void contextLoads() {
        // This test ensures the application context loads successfully
        // with mocked properties to avoid actual Azure SQL connection
    }
}