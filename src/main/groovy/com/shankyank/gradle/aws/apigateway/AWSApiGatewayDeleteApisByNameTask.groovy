package com.shankyank.gradle.aws.apigateway

import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

/**
 * This task deletes all APIs with a given name from
 * API Gateway.
 */
class AWSApiGatewayDeleteApisByNameTask extends BaseAWSApiGatewayTask {
    /** The name of the API(s) to delete. */
    String apiName

    @TaskAction
    void deleteApis() {
        if (!apiName?.trim()) {
            throw new GradleException('API Name must be provided')
        }
        apiGateway.findApisByName(apiName).each { api ->
            api.delete()
        }
    }
}
