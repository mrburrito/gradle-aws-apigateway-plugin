package com.shankyank.gradle.aws.apigateway

import com.amazonaws.services.apigateway.model.GetApiKeysRequest
import com.amazonaws.services.apigateway.model.GetStageRequest
import com.amazonaws.services.apigateway.model.GetStageResult
import com.shankyank.gradle.aws.apigateway.model.Api
import com.shankyank.gradle.aws.apigateway.model.ApiKey
import com.shankyank.gradle.aws.apigateway.model.ApiKeyMergeStrategy
import com.shankyank.gradle.aws.apigateway.model.CloudWatchLogLevel
import groovy.transform.Memoized
import groovy.util.logging.Slf4j
import org.gradle.api.tasks.TaskAction

/**
 * Gradle task to authorize a set of API Keys for access to
 * a particular deployment stage. Deployment stages can be
 * mapped before they are created.
 */
@Slf4j
class AWSApiGatewayAuthorizeKeysTask extends BaseAWSApiGatewayTask {
    /** The API name */
    String apiName

    /** The name of the deployment stage. */
    String stageName

    /** The API Key names to authorize */
    Set<String> apiKeyNames = [] as Set

    /** The API Key merge strategy. */
    ApiKeyMergeStrategy mergeStrategy = ApiKeyMergeStrategy.MERGE

    /**
     * Updates API Key access according to the configured merge policy:
     *   MERGE: Any configured apiKeys are retrieved and added to the access list for this stage
     *   REPLACE: All apiKeys are retrieved and only those found in apiKeys maintain access to this stage
     */
    @TaskAction
    void updateApiKeyAccessForStage() {
        log.info("[API: ${apiName}] Configuring access to Stage ${stageId} for API Keys: ${apiKeyNames}")
        List keys = allApiKeys
        log.debug("Found API Keys:\n\t${keys.collect { "${it.class.name}: ${it}"}.join('\n\t')}")
        mergeStrategy.configureAccessToStage(stageId, apiKeyNames, keys)
    }

    /**
     * @return all existing API keys
     */
    protected List<ApiKey> getAllApiKeys() {
        apiGateway.collectPagedResults(new GetApiKeysRequest(), awsApiGateway.&getApiKeys).collect {
            new ApiKey(
                    apiGateway: awsApiGateway,
                    id: it.id,
                    name: it.name,
                    allowedStages: it.stageKeys as Set
            )
        }
    }

    @Memoized
    protected Api getApi() {
        findSingleApiWithName(apiName)
    }

    @Memoized
    protected String getStageId() {
        "${api.apiId}/${stageName}"
    }
}
