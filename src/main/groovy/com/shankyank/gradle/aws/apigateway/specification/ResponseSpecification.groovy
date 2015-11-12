package com.shankyank.gradle.aws.apigateway.specification

import groovy.transform.Canonical
import groovy.transform.Memoized

/**
 * A method response defined in the specification.
 */
@Canonical
class ResponseSpecification {
    /** The status code. */
    String statusCode

    /** The map of content type to response model. */
    Map<String, ModelSpecification> models = [:]

    /** The header parameters. */
    List<ParameterSpecification> headers = []

    /**
     * @return a map of AWS parameter names to required flag
     */
    @Memoized
    Map<String, Boolean> getAwsResponseHeaders() {
        headers?.collectEntries { param ->
            [ (param.awsResponseParameterName): param.required ]
        }
    }
}
