package com.lydtechconsulting.flywaylambda.services;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static com.github.stefanbirkner.systemlambda.SystemLambda.*;

class FlywayMigrationServiceTest {

    public static final String DATABASE_URL = "jdbc:hsqldb:mem:myunittests";
    public static final String DATABASE_USERNAME = "test";
    public static final String DATABASE_PASSWORD = "pass";

    @Test
    public void should_migrate_successfully() throws Exception {
        //Call migration using HSQL db properties and loading a sql script that will create a Person table
        withEnvironmentVariable("FLYWAY_URL", DATABASE_URL)
                .and("FLYWAY_USER", DATABASE_USERNAME)
                .and("FLYWAY_PASSWORD", DATABASE_PASSWORD)
                .execute(() -> new FlywayMigrationService().performMigration(mock(LambdaLogger.class), "classpath:/sql/"));

        //Prove that the Person table now exists
        JdbcTemplate jdbcTemplate = new JdbcTemplate(new DriverManagerDataSource(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD));
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) from PERSON", Integer.class);
        assertEquals(0, count);
    }
}
