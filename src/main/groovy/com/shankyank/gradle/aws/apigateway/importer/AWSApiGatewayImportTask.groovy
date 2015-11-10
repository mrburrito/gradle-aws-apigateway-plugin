package com.shankyank.gradle.aws.apigateway.importer

import com.amazonaws.services.apigateway.model.RestApi
import com.shankyank.gradle.aws.apigateway.model.Api
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

/**
 * Imports an API specification, creating and/or updating an API Gateway
 * RestApi.
 */
class AWSApiGatewayImportTask extends BaseAWSApiGatewayImporterTask {
    AWSApiGatewayImportTask() {
        description = "Imports a ${supportedSpecifications} specification into AWS API Gateway, \
creating the API if it does not exist."
    }

    @TaskAction
    void importApi() {
        Set<Api> namedApis = apiGateway.findApisByName(specification.name)
        switch (namedApis.size()) {
            case 0:
                logger.info("No API named '${specification.name}' found, creating.")
                createApi()
                break
            case 1:
                logger.info("Updating ${namedApis[0]}")
                updateApi(namedApis[0])
                break
            default:
                logger.info("Found ${namedApis.size()} APIs named '${specification.name}: ${namedApis}")
                throw new GradleException("Found ${namedApis.size()} APIs named '${specification.name}'. \
Use AWSApiGatewayImportCreateTask or AWSApiGatewayImportUpdateTask instead.")
        }
    }
}
