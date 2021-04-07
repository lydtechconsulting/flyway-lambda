### Lambda Flyway Migrator

A Java project designed to be executed as an AWS Lambda, designed to perform a 'Flyway migrate' on a specified database.

Inspired by https://github.com/dhet/flyway-awslambda which appears to use an old AWS API meaning for example that the region eu-west-2 is not useable.

### Instructions
* Run `mvn clean install` on this project to build a 'jar with dependencies'
* Create an AWS lambda (Java 11) using the generated Jar and the handler class `io.ovalview.flyway.handler.Handler' note the following:
  * the timeout will need increasing from the default 15 seconds
  * the lambda will need read access to the s3 bucket that will contain SQL files
* Ensure the SQL files are uploaded to the s3 bucket (in root)
* Execute the lambda with the payload: `{"bucket_name": "the_bu ket_na"}`

### Future improvements
This project has been developed for a specific use case. The following are suggested improvements that could be made to improve its usefulness
- Allow other options for db creds apart from env vars. e.g. flyway config file / aws secret manager entries
- Allow Flyway config file to be used to control options (many are currently hardcoded)

