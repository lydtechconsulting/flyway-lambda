package com.lydtechconsulting.flywaylambda.services;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;

import static java.util.Objects.requireNonNull;

public class FlywayMigrationService {
    /**
     * Perform a Flyway migrate using the scripts in the given location
     * @param logger use this for logging
     * @param flywayScriptsLocation location to pick up SQL files from - filesystem://* or classpath://*
     */
    public void performMigration(LambdaLogger logger, String flywayScriptsLocation) {
        logger.log("performing Flyway migrate");
        Flyway flyway = Flyway
                .configure()
                .envVars() //Configures Flyway using FLYWAY_* environment variables.
                .locations(flywayScriptsLocation)
                .load();

        MigrateResult result = flyway.migrate();

        logger.log("applied " + result.migrations.size() + " migrations to db " + result.database + ". warnings: " + result.warnings.size());
    }

    /**
     * Handy for testing local migrations.
     * <p>
     * need the following env vars set, e.g.
     * <p>
     * FLYWAY_URL=jdbc:postgresql://localhost/ovalview
     * FLYWAY_USER=postgres
     * FLYWAY_PASSWORD=password
     * FLYWAY_SCHEMAS=ovalview
     * FLYWAY_DEFAULT_SCHEMA=ovalview
     * 
     * Ensure sql files are in /tmp/sql
     *
     * @param args not required
     */
    public static void main(String[] args) {

        requireEnvVar("FLYWAY_URL");
        requireEnvVar("FLYWAY_USER");
        requireEnvVar("FLYWAY_PASSWORD");
        requireEnvVar("FLYWAY_SCHEMAS");
        requireEnvVar("FLYWAY_DEFAULT_SCHEMA");
        new FlywayMigrationService().performMigration(new LambdaLogger() {
            @Override
            public void log(String message) {
                System.out.println("message = " + message);
            }

            @Override
            public void log(byte[] message) {
                System.out.println("message = " + new String(message));
            }
        }, "filesystem:///tmp/sql/");
    }

    private static void requireEnvVar(String name) {
        requireNonNull(System.getenv().get(name), name + " env variable is required");
    }
}

