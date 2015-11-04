package com.shankyank.gradle.aws.apigateway.importer

import com.amazonaws.services.apigateway.model.RestApi
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

/**
 * Imports a Swagger or RAML specification, creating and/or updating an API Gateway
 * RestApi.
 */
class AWSApiGatewayImportTask extends BaseAWSApiGatewayImporterTask {
    AWSApiGatewayImportTask() {
        description = "Imports a ${supportedSpecifications} specification into an API Gateway REST API."
    }

    @TaskAction
    void importApi() {
        Set<RestApi> namedApis = getNamedRestApis(specification.name)
        switch (namedApis.size()) {
            case 0:
                logger.info("No API named '${specification.name}' found, creating.")
                createApi(specification)
                break
            case 1:
                RestApi api = namedApis[0]
                logger.info("Found API [${api.id}] named '${specification.name}', updating.")
                updateApi(api, specification)
                break
            default:
                throw new GradleException("Found ${namedApis.size()} APIs named '${specification.name}. " +
                        "Use AWSApiGatewayImportCreateTask or AWSApiGatewayImportUpdateTask instead.")
        }
    }

    void createApi() {
    }

    void updateApi(final RestApi api) {
    }
}
