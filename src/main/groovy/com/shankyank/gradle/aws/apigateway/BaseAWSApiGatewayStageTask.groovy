package com.shankyank.gradle.aws.apigateway

import com.amazonaws.services.apigateway.model.GetApiKeysRequest
import com.shankyank.gradle.aws.apigateway.model.Api
import com.shankyank.gradle.aws.apigateway.model.ApiKey
import com.shankyank.gradle.aws.apigateway.model.ApiKeyMergeStrategy
import com.shankyank.gradle.aws.apigateway.model.CloudWatchLogLevel
import com.shankyank.gradle.aws.apigateway.model.DeploymentStage
import groovy.transform.Memoized
import groovy.util.logging.Slf4j
import org.gradle.api.GradleException

/**
 * Base task for operations on an API Deployment Stage.
 */
@Slf4j
abstract class BaseAWSApiGatewayStageTask extends BaseAWSApiGatewayTask {
    /** The name of the API to deploy. */
    String apiName

    /** The stage to deploy to. */
    String stageName

    /** The stage description. */
    String stageDescription

    /** The stage variables */
    Map<String, String> stageVariables

    /** Should metrics be logged to CloudWatch? */
    boolean logMetrics = true

    /** The logging level for CloudWatch */
    CloudWatchLogLevel logLevel = CloudWatchLogLevel.ERROR

    /**
     * @return the configured Api
     */
    @Memoized
    protected Api getApi() {
        Api api = findSingleApiWithName(apiName)
        if (!api) {
            throw new GradleException("No API found with name '${apiName}'.")
        }
        api
    }

    /**
     * @return the configured deployment stage
     */
    protected DeploymentStage getDeploymentStage() {
        new DeploymentStage(
                api: api,
                apiGateway: api.apiGateway,
                name: stageName,
                stageDescription: stageDescription,
                stageVariables: stageVariables,
                logMetrics: logMetrics,
                logLevel: logLevel
        )
    }
}
