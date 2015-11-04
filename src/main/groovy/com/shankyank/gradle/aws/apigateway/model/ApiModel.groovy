package com.shankyank.gradle.aws.apigateway.model

import com.amazonaws.services.apigateway.model.DeleteModelRequest
import com.amazonaws.services.apigateway.model.Model
import com.shankyank.gradle.aws.apigateway.specification.SpecificationModel
import groovy.util.logging.Slf4j

/**
 * Decorator around a Model
 */
@Slf4j('logger')
class ApiModel implements RestApiDecorator {
    /** The decorated model. */
    final Model model

    ApiModel(final RestApiDecorator parent, final Model model) {
        this.apiGateway = parent.apiGateway
        this.restApi = parent.restApi
        this.model = model
    }

    /**
     * Delete this model.
     */
    void delete() {
        logger.debug("Removing Model '${model.name}' from ${apiNameForLog}")
        apiGateway.deleteModel(new DeleteModelRequest().
                withRestApiId(apiId).
                withModelName(model.name))
    }

    /**
     * Update this model to match the new specification.
     * @param specification the model specification
     * @return the updated model
     */
    ApiModel update(final SpecificationModel specification) {
        this
    }
}
