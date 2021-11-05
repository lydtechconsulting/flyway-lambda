### Lambda Flyway Migrator

A Java  project designed to be executed as an AWS Lambda.Pperforms a 'Flyway migrate' on a specified database.

### Instructions
* Run `mvn clean install` on this project to build a 'jar with dependencies'
* Create an AWS lambda (Java 11) using the generated Jar and the handler class `com.lydtechconsulting.flywaylambda.FlywayHandler' note the following:
  * the timeout will need increasing from the default 15 seconds
  * the lambda will need read access to the s3 bucket that will contain SQL files
* Ensure the SQL files are uploaded to the s3 bucket (in root)
* Execute the lambda with the payload: `{"bucket_name": "the_bucket_name"}`

### Future improvements
This project has been developed for a specific use case. The following are suggested improvements that could be made to improve its usefulness
- Allow other options for db creds apart from env vars. e.g. flyway config file / aws secret manager entries
- Allow Flyway config file to be used to control options (many are currently hardcoded)
- Currently only includes the Postgres JDBC driver. Others to be added

