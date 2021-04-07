package io.ovalview.flyway;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;

// needs the following env vars:
// FLYWAY_URL
// FLYWAY_USER
// FLYWAY_PASSWORD
// FLYWAY_SCHEMAS
// FLYWAY_DEFAULT_SCHEMA

public class FlywayMigrator {
    public void performMigration(String location) {
        System.out.println("performing Flyway migrate");
        Flyway flyway = Flyway.configure()
                .envVars()
                .locations("filesystem://" + location)
                .load();

        MigrateResult result = flyway.migrate();

        System.out.println("applied " + result.migrations.size() + " migrations to db " +result.database + ". warnings: " + result.warnings.size() );
    }
    
    public static void main(String[] args) {
        // need the following env vars set up
        /*
FLYWAY_URL=jdbc:postgresql://localhost/ovalview
FLYWAY_USER=postgres
FLYWAY_PASSWORD=password
FLYWAY_SCHEMAS=ovalview
FLYWAY_DEFAULT_SCHEMA=ovalview
         */
        new FlywayMigrator().performMigration("/Users/james/dev/ovalview/ovalview-functions/flyway/sql");
    }
}
