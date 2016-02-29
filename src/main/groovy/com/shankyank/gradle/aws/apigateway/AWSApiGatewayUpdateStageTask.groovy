package com.shankyank.gradle.aws.apigateway

import org.gradle.api.tasks.TaskAction

/**
 * Gradle task to update a deployed API Stage.
 */
class AWSApiGatewayUpdateStageTask extends BaseAWSApiGatewayStageTask {
    AWSApiGatewayUpdateStageTask() {
        description = 'Updates a previously deployed API stage.'
    }

    @TaskAction
    void updateStage() {
        deploymentStage.update()
    }
}
