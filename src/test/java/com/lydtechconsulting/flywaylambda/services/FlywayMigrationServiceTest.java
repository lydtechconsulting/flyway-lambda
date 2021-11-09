package com.lydtechconsulting.flywaylambda.services;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FlywayMigrationServiceTest {

    public static final String DATABASE_URL = "jdbc:hsqldb:mem:myunittests";
    public static final String DATABASE_USERNAME = "test";
    public static final String DATABASE_PASSWORD = "pass";

    @Test
    public void should_migrate_successfully() throws Exception {
        //Call migration using HSQL db properties and loading a sql script that will create a Person table
        SecretsManagerClient mockSecretsManagerClient = mock(SecretsManagerClient.class);
        when(mockSecretsManagerClient.getSecretValue(Mockito.any(GetSecretValueRequest.class))).thenReturn(GetSecretValueResponse
                .builder()
                .secretString("{\"db_password\": \"" + DATABASE_PASSWORD + "\"}")
                .build());
        withEnvironmentVariable("FLYWAY_URL", DATABASE_URL)
                .and("FLYWAY_USER", DATABASE_USERNAME)
                .execute(() -> {
                    new FlywayMigrationService().performMigration(mock(LambdaLogger.class), mockSecretsManagerClient, "classpath:/sql/", "mySecretName");
                });

        //Prove that the Person table now exists
        JdbcTemplate jdbcTemplate = new JdbcTemplate(new DriverManagerDataSource(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD));
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) from PERSON", Integer.class);
        assertEquals(0, count);
    }
}
