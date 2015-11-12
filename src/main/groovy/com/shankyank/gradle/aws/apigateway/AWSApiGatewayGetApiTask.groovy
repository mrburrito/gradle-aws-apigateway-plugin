package com.shankyank.gradle.aws.apigateway

import com.shankyank.gradle.aws.apigateway.model.Api
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

/**
 * Gets an API by ID.
 */
class AWSApiGatewayGetApiTask extends BaseAWSApiGatewayTask {
    /** The ID of the API. */
    String id

    @TaskAction
    void findApi() {
        if (!id) {
            throw new GradleException("API ID must be provided")
        }
        Api api = apiGateway.getApiById(id)
        println "API[${id}]: ${api ?: 'Not Found'}"
    }
}
