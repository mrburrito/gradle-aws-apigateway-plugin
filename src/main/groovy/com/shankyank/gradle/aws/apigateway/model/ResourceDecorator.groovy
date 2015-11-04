package com.shankyank.gradle.aws.apigateway.model

import com.amazonaws.services.apigateway.model.Resource

/**
 * Decorator around an API Gateway Resource.
 */
trait ResourceDecorator extends RestApiDecorator {
    /** The decorated resource. */
    final Resource resource

    /**
     * @return the resource ID
     */
    String getResourceId() {
        resource.id
    }

    /**
     * @return the resource path
     */
    String getResourcePath() {
        resource.path
    }
}
