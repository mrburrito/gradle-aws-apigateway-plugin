package com.shankyank.gradle.aws.apigateway

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
        apiGateway.getApiById(id).with { opt ->
            println "API[${id}]: ${opt ? opt.get() : 'Not Found'}"
        }
    }
}
