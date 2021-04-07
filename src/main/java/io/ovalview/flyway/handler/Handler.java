package io.ovalview.flyway.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.ovalview.flyway.FlywayMigrator;
import io.ovalview.flyway.S3FileLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class Handler implements RequestHandler<Map<String,String>, String> {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    private FlywayMigrator flywayMigrator = new FlywayMigrator();
    private S3FileLoader s3FileLoader = new S3FileLoader();
    
    @Override
    public String handleRequest(Map<String,String> event, Context context)
    {
        LambdaLogger logger = context.getLogger();
        // log execution details
        logger.log("ENVIRONMENT VARIABLES: " + gson.toJson(System.getenv()));
        logger.log("CONTEXT: " + gson.toJson(context));
        // process event
        String eventString = gson.toJson(event);
        logger.log("EVENT: " + eventString);
        logger.log("EVENT TYPE: " + event.getClass().toString());

        String destination = "/tmp/sqlFiles_" + System.currentTimeMillis(); //todo correct name & clear if necessary
        JsonObject jsonObject = gson.fromJson( eventString, JsonObject.class);
        String bucketName = jsonObject.get("bucket_name").getAsString();
        System.out.println("destination = " + destination);
        System.out.println("bucketName = " + bucketName);
        try {
            Files.createDirectories(Paths.get(destination));
        } catch (IOException e) {
            throw new RuntimeException("problem creating directory", e);
        }
        s3FileLoader.copy(bucketName, destination);
        flywayMigrator.performMigration(destination);
        
        return "200 OK";
    }

    public void setFlywayMigrator(FlywayMigrator flywayMigrator) {
        this.flywayMigrator = flywayMigrator;
    }

    public void setS3FileLoader(S3FileLoader s3FileLoader) {
        this.s3FileLoader = s3FileLoader;
    }
}
