### Lambda Flyway Migrator

A Java project designed to be executed as an AWS Lambda. Performs a 'Flyway migrate' on a specified database.

### Instructions
See https://www.lydtechconsulting.com/blog-flyway-serverless.html

### Future improvements
This project has been developed for a specific use case. The following are suggested improvements that could be made to improve its usefulness
- Allow other options for db creds apart from env vars. e.g. flyway config file / aws secret manager entries
- Allow Flyway config file to be used to control options (many are currently hardcoded)
- Currently only includes the Postgres JDBC driver. Others to be added

