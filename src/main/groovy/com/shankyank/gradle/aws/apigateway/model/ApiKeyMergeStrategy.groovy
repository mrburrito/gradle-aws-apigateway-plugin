package com.shankyank.gradle.aws.apigateway.model

import groovy.util.logging.Slf4j

/**
 * Indicates the strategy for updating API Keys when
 * deploying or updating a stage.
 */
@Slf4j
enum ApiKeyMergeStrategy {
    /** Provided API Keys are merged with those already configured on the stage. */
    MERGE,
    /** Only the provided API Keys will be given access to the stage; any configured keys not provided will be cleared. */
    REPLACE

    /**
     * Update the access policy according to this strategy so that all existing keys with
     * names found in `allowedKeyNames` are able to access the API deployed to the provided
     * stage.
     * @param stageId the ID of the stage to which access should be configured
     * @param allowedKeys the names of the keys allowed to access the stage
     * @param keys the keys to configure
     */
    void configureAccessToStage(final String stageId, final Set<String> allowedKeyNames, final List<ApiKey> keys) {
        log.info("Granting access to stage [${stageId}] for keys: ${allowedKeyNames}")
        keys.each {
            if (allowedKeyNames.contains(it.name)) {
                log.debug("Granting access to ${stageId} for key ${it.name} [${it.id}]")
                it.grantAccessToStage(stageId)
            } else if (this == REPLACE) {
                log.debug("Revoking access to ${stageId} for key ${it.name} [${it.id}]")
                it.revokeAccessToStage(stageId)
            }
        }
    }
}
