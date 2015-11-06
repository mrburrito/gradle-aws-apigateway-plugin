package com.shankyank.gradle.aws.apigateway.importer

import com.shankyank.gradle.aws.apigateway.model.Api
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

/**
 * Imports an API specification into API Gateway, updating the specified API.
 */
class AWSApiGatewayImportUpdateTask extends BaseAWSApiGatewayImporterTask {
    /** The AWS ID of the API to update. */
    String apiId

    AWSApiGatewayImportUpdateTask() {
        super()
        description = "Imports a ${supportedSpecifications} specification, updating an existing API Gateway REST API."
    }

    @TaskAction
    void updateApi() {
        Api api = apiGateway.getApiById(apiId)
        if (api) {
            updateApi(api)
        } else {
            throw new GradleException("No API found with ID '${apiId}'")
        }
    }
}
