package com.lydtechconsulting.flywaylambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lydtechconsulting.flywaylambda.services.FileDownloadService;
import com.lydtechconsulting.flywaylambda.services.FlywayMigrationService;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class FlywayHandler implements RequestHandler<Map<String, String>, String> {
    Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private FlywayMigrationService flywayMigrationService = new FlywayMigrationService();
    private FileDownloadService fileDownloadService = new FileDownloadService();

    @Override
    public String handleRequest(Map<String, String> event, Context context) {
        LambdaLogger logger = context.getLogger();
        logDetails(event, context, logger);

        if (!event.containsKey("bucket_name")) {
            throw new NullPointerException("event must have bucket_name field");
        }
        String bucketName = event.get("bucket_name");
        if (!event.containsKey("secret_name")) {
            throw new NullPointerException("event must have secret_name field");
        }
        String secretName = event.get("secret_name");
        String flywayScriptsLocation = "/tmp/sqlFiles_" + System.currentTimeMillis(); 
        logger.log("bucketName: " + bucketName);
        logger.log("destination: " + flywayScriptsLocation);
        
        String regionString = System.getenv("AWS_REGION");
        requireNonNull(regionString, "AWS_REGION expected to be set");
        Region region = Region.of(regionString);
        
        createDirectory(flywayScriptsLocation);
        final S3Client s3Client = S3Client.builder()
                .region(region) 
                .build();
        fileDownloadService.copy(logger, s3Client, bucketName, flywayScriptsLocation);

        SecretsManagerClient secretsClient = SecretsManagerClient.builder()
                .region(region)
                .build();
        flywayMigrationService.performMigration(logger, secretsClient, "filesystem://" + flywayScriptsLocation, secretName);

        return "200 OK";
    }

    private void createDirectory(String flywayScriptsLocation) {
        try {
            Files.createDirectories(Paths.get(flywayScriptsLocation));
        } catch (IOException e) {
            throw new RuntimeException("problem creating directory", e);
        }
    }

    private void logDetails(Map<String, String> event, Context context, LambdaLogger logger) {
        // log execution details
        logger.log("ENVIRONMENT VARIABLES: " + gson.toJson(System.getenv()));
        logger.log("CONTEXT: " + context.toString());
        // process event
        logger.log("EVENT: " + event.toString());
    }
    
}
