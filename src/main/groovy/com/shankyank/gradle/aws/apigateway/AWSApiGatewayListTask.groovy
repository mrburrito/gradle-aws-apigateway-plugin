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
        List apis = apiGateway.restApis
        logger.quiet("Found ${apis.size()} APIs:")
        apis.each { api -> logger.quiet("\t${api.apiId}: ${api.apiName}\n\t\t${api.apiDescription}\n\t\tCreated: ${api.apiCreatedDate}") }
    }
}
