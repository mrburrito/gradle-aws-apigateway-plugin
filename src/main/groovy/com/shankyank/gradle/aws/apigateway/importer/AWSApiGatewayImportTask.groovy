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
        Api api = findSingleApiWithName(specification.name)
        if (api) {
            logger.info("Updating API ${api}")
            updateApi(api)
        } else {
            logger.info("Creating API ${specification.name}")
            createApi()
        }
    }
}
