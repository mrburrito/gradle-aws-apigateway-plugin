package com.shankyank.gradle.aws.apigateway.model

import com.amazonaws.services.apigateway.model.Resource
import com.shankyank.gradle.aws.apigateway.specification.SpecificationResource
import groovy.util.logging.Slf4j

/**
 * Decorator around a Resource.
 */
@Slf4j('logger')
class ApiResource implements ResourceDecorator {
    ApiResource(final RestApiDecorator restApiDecorator, final Resource resource) {
        this.apiGateway = restApiDecorator.apiGateway
        this.restApi = restApiDecorator.restApi
        this.resource = resource
    }

    /**
     * Delete this resource.
     */
    void delete() {
        // todo: implement this method
        logger.debug("Deleting Resource '${resource.path}' in ${apiNameForLog}")
    }

    /**
     * Update this resource to match the specification.
     * @param specification the specification to match
     * @return the updated ApiResource
     */
    ApiResource update(final SpecificationResource specification) {
        // todo: implement this method
        this
    }
}
