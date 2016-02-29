package com.shankyank.gradle.aws.apigateway.model

import com.amazonaws.services.apigateway.model.CreateBasePathMappingRequest
import groovy.transform.Canonical

/**
 * A custom domain name registered in API Gateway.
 */
@Canonical
class CustomDomain implements ApiGatewayContainer {
    /** The custom domain name. */
    String domainName

    /**
     * Map a base path from this domain name to the target stage.
     * @param basePath the base path, null or empty to map the root domain
     * @param apiId the ID of the target RestApi
     * @param stageName the name of the target stage
     */
    void mapBasePathToStage(final String basePath, final String apiId, final String stageName) {
        apiGateway.createBasePathMapping(new CreateBasePathMappingRequest(
                domainName: domainName,
                basePath: basePath,
                restApiId: apiId,
                stage: stageName
        ))
    }
}
