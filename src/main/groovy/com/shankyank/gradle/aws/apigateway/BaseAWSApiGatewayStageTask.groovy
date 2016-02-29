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

    /** The list of API Keys, by name, valid for this Stage */
    Set<String> apiKeys = [] as Set

    /** The API Key merge strategy. */
    ApiKeyMergeStrategy apiKeyMergeStrategy = ApiKeyMergeStrategy.MERGE

    /** The stage variables */
    Map<String, String> stageVariables

    /** Should metrics be logged to CloudWatch? */
    boolean logMetrics = true

    /** The logging level for CloudWatch */
    CloudWatchLogLevel logLevel = CloudWatchLogLevel.ERROR

    /**
     * Updates API Key access according to the configured merge policy:
     *   MERGE: Any configured apiKeys are retrieved and added to the access list for this stage
     *   REPLACE: All apiKeys are retrieved and only those found in apiKeys maintain access to this stage
     */
    protected void updateApiKeyAccessForStage() {
        log.info("Found API Keys:\n\t${allApiKeys.collect { "${it.class.name}: ${it}"}.join('\n\t')}")
        apiKeyMergeStrategy.configureAccessToStage(deploymentStage, apiKeys, allApiKeys)
    }

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

    /**
     * @return all existing API keys
     */
    protected List<ApiKey> getAllApiKeys() {
        apiGateway.collectPagedResults(new GetApiKeysRequest(), apiGateway.apiGateway.&getApiKeys).collect {
            new ApiKey(
                    apiGateway: api.apiGateway,
                    id: it.id,
                    name: it.name,
                    allowedStages: it.stageKeys as Set
            )
        }
    }
}
