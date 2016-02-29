package com.shankyank.gradle.aws.apigateway

import com.shankyank.gradle.aws.apigateway.model.DeploymentStage
import org.gradle.api.tasks.TaskAction

/**
 * Gradle task to deploy an API Gateway to a named Stage, creating the Stage
 * if necessary.
 */
class AWSApiGatewayDeployApiTask extends BaseAWSApiGatewayStageTask {
    /** The deployment description. */
    String deploymentDescription

    AWSApiGatewayDeployApiTask() {
        description = 'Deploys a configured API to the target stage.'
    }

    @TaskAction
    void deployApi() {
        deploymentStage.deploy()
        updateApiKeyAccessForStage()
    }

    @Override
    protected DeploymentStage getDeploymentStage() {
        DeploymentStage stage = super.deploymentStage
        stage.deploymentDescription = deploymentDescription
        stage
    }
}
