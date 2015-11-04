package com.shankyank.gradle.aws.apigateway.specification

import groovy.transform.Immutable

/**
 * A resource method defined by the specification.
 */
@Immutable
class SpecificationMethod {
    /**
     * The operation types for resource methods.
     */
    static enum Operation {
        GET,
        POST,
        PUT,
        DELETE,
        OPTIONS,
        PATCH
    }

    /** The operation type. */
    Operation operation

    /** The authorization type. */
    String authorizationType

    /** Is an API key required for this operation? */
    boolean apiKeyRequired

    /** The method parameters. */
    List<SpecificationParameter> parameters = []

    /** The method integrations. */
    List<SpecificationIntegration> requestIntegrations= []

    /** The method responses. */
    List<SpecificationResponse> responses = []
}
