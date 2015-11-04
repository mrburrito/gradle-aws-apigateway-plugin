# AWS API Gateway Importer Gradle Plugin
---

    This plugin borrows heavily from Amazon's [API Gateway Importer](https://github.com/awslabs/aws-apigateway-importer)
    command line application. Ideally, if that project is published as a library, the majority of the code in this
    project can be replaced with a dependency.

The API Gateway Importer plugin simplifies the process of deploying a Swagger or RAML defined API
to Amazon's API Gatway service. It creates a new API when necessary, updating an existing API
to conform to the target definition file, using the defined name of the API to check for existence.
