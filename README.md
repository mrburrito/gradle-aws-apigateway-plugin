# AWS API Gateway Importer Gradle Plugin
---

    This plugin is loosely based on Amazon's [API Gateway Importer](https://github.com/awslabs/aws-apigateway-importer)
    command line application. Ideally, if that project is published as a library, this code can be refactored to leverage
    their import process.

The API Gateway Importer plugin simplifies the process of deploying a Swagger defined API
to Amazon's API Gatway service. It creates a new API when necessary, updating an existing API
to conform to the target definition file, using the defined name of the API to check for existence.

This plugin is licensed under the [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt) license.