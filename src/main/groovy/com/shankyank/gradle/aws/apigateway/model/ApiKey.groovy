package com.shankyank.gradle.aws.apigateway.model

import com.amazonaws.services.apigateway.model.PatchOperation
import com.amazonaws.services.apigateway.model.UpdateApiKeyRequest
import groovy.transform.Canonical
import groovy.transform.ToString
import groovy.util.logging.Slf4j

/**
 * Represents an API Key providing access to a deployed stage.
 */
@Canonical
@ToString(includeNames=true)
@Slf4j
class ApiKey implements ApiGatewayContainer {
    /** The key ID */
    String id

    /** The name of the key */
    String name

    /** The API stages this key provides access to. */
    Set allowedStages = [] as Set

    /**
     * Grants access to the target stage.
     * @param stage the stage
     */
    void grantAccessToStage(final DeploymentStage stage) {
        if (allowedStages.add(stage.awsId)) {
            log.debug("Granting access to ${stage.awsId} for key ${name}")
            updateAccessPolicy([
                    new PatchOperation(op: 'add', path: '/stages', value: stage.awsId)
            ])
        }
    }

    /**
     * Removes access to the target stage, if it was provided.
     * @param stage the stage
     */
    void revokeAccessToStage(final DeploymentStage stage) {
        if (allowedStages.remove(stage.awsId)) {
            log.debug("Revoking access to ${stage.awsId} for key ${name}")
            updateAccessPolicy([
                    new PatchOperation(op: 'remove', path: '/stages', value: stage.awsId)
            ])
        }
    }

    /**
     * Updates this key so it provides access to the stages
     * currently in `allowedStages`.
     * @param patches the patch operations
     */
    private void updateAccessPolicy(final List patches) {
        log.info("API Key ${name} - Applying patches\n\t${patches.join('\n\t')}")
        apiGateway.updateApiKey(new UpdateApiKeyRequest(
                apiKey: id,
                patchOperations: patches
        ))
    }
}
