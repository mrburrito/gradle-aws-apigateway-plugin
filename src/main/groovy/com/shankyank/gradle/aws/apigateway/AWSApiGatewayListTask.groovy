package com.shankyank.gradle.aws.apigateway

import org.gradle.api.tasks.TaskAction

/**
 * Lists all APIs known to API Gateway.
 */
class AWSApiGatewayListTask extends BaseAWSApiGatewayTask {
    AWSApiGatewayListTask() {
        description = 'List all APIs found in API Gateway'
    }

    @TaskAction
    void listApis() {
        apiGateway.restApis.with {
            println "Found ${size()} APIs:"
            it.each { api -> println "\t${api.apiId}: ${api.apiName}\n\t\t${api.apiDescription}\n\t\tCreated: ${api.apiCreatedDate}" }
        }
    }
}
