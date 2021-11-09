package com.lydtechconsulting.flywaylambda.services;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class FlywayMigrationService {
    Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    /**
     * Perform a Flyway migrate using the scripts in the given location
     * @param logger use this for logging
     * @param flywayScriptsLocation location to pick up SQL files from - filesystem://* or classpath://*
     */
    public void performMigration(LambdaLogger logger, SecretsManagerClient secretsClient, String flywayScriptsLocation, String secretName) {
        logger.log("performing Flyway migrate");
        
        String dbUrl = System.getenv("FLYWAY_URL");
        Objects.requireNonNull(dbUrl, "FLYWAY_URL env var is required");
        String dbUser = System.getenv("FLYWAY_USER");
        Objects.requireNonNull(dbUrl, "FLYWAY_USER env var is required");

        String dbPassword = getValue(secretsClient, secretName);
        System.out.println("dbPassword = " + dbPassword);
        secretsClient.close();
            
        Flyway flyway = Flyway
                .configure()
                .envVars() //Configures Flyway using FLYWAY_* environment variables.
                .dataSource(dbUrl, dbUser, dbPassword)
                .locations(flywayScriptsLocation)
                .load();

        MigrateResult result = flyway.migrate();

        logger.log("applied " + result.migrations.size() + " migrations to db " + result.database + ". warnings: " + result.warnings.size());
    }

    public String getValue(SecretsManagerClient secretsClient,String secretName) {

        try {
            GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();

            GetSecretValueResponse valueResponse = secretsClient.getSecretValue(valueRequest);
            JsonObject convertedObject = new Gson().fromJson(valueResponse.secretString(), JsonObject.class);
            return convertedObject.get("db_password").getAsString();

        } catch (SecretsManagerException e) {
            throw new RuntimeException("Problem getting secret:  " + secretName, e);
        }
    }
    
}

