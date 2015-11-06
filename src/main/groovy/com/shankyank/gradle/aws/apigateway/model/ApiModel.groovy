package com.shankyank.gradle.aws.apigateway.model

import com.amazonaws.services.apigateway.model.DeleteModelRequest
import com.amazonaws.services.apigateway.model.Model
import groovy.util.logging.Slf4j

/**
 * Decorator around a Model
 */
@Slf4j('logger')
class ApiModel implements ApiContainer {
    /** The decorated model. */
    private final Model model

    ApiModel(final Api api, final Model model) {
        this.apiGateway = api.apiGateway
        this.api = api
        this.model = model
    }

    /**
     * @return the model name
     */
    String getName() {
        model.name
    }

    /**
     * Delete this model.
     */
    void delete() {
        debug("Removing Model '${model.name}'")
        apiGateway.deleteModel(new DeleteModelRequest().
                withRestApiId(apiId).
                withModelName(model.name))
    }
}
