package com.shankyank.gradle.aws.apigateway.model

import com.amazonaws.services.apigateway.model.RestApi
import groovy.transform.Memoized

/**
 * Objects that contain an immutable RestApi.
 */
trait RestApiDecorator extends ApiGatewayDecorator {
    /** The RestApi. */
    final RestApi restApi

    /**
     * @return the ID of the contained API
     */
    String getApiId() {
        restApi.id
    }

    /**
     * @return the name of the contained API
     */
    String getApiName() {
        restApi.name
    }

    /**
     * @return a logging identifier for the RestApi
     */
    @Memoized
    String getApiNameForLog() {
        "RestApi[${apiId}: ${apiName}"
    }
}
