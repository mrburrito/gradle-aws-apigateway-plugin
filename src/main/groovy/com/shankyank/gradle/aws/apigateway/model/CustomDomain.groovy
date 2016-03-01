package com.shankyank.gradle.aws.apigateway.model

import com.amazonaws.services.apigateway.model.CreateBasePathMappingRequest
import com.amazonaws.services.apigateway.model.GetBasePathMappingRequest
import com.amazonaws.services.apigateway.model.PatchOperation
import com.amazonaws.services.apigateway.model.UpdateBasePathMappingRequest
import groovy.transform.Canonical
import groovy.util.logging.Slf4j

/**
 * A custom domain name registered in API Gateway.
 */
@Canonical
@Slf4j('logger')
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
        basePathMappingExists(basePath) ? updateExistingMapping(basePath, apiId, stageName) : createMapping(basePath, apiId, stageName)
    }

    private boolean basePathMappingExists(final String basePath) {
        findNullableObject {
            apiGateway.getBasePathMapping(new GetBasePathMappingRequest(
                    domainName: domainName,
                    basePath: basePath
            ))
        }
    }

    private void updateExistingMapping(final String basePath, final String apiId, final String stageName) {
        List patchOps = [
                new PatchOperation(op: 'replace', path: '/restapiId', value: apiId),
                new PatchOperation(op: 'replace', path: '/stage', value: stageName)
        ]
        apiGateway.updateBasePathMapping(new UpdateBasePathMappingRequest(
                domainName: domainName,
                basePath: basePath,
                patchOperations: patchOps
        ))
    }

    private void createMapping(final String basePath, final String apiId, final String stageName) {
        apiGateway.createBasePathMapping(new CreateBasePathMappingRequest(
                domainName: domainName,
                basePath: basePath,
                restApiId: apiId,
                stage: stageName
        ))
    }
}
