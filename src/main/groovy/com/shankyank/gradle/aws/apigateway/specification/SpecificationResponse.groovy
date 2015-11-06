package com.shankyank.gradle.aws.apigateway.specification

import groovy.transform.Immutable
import groovy.transform.Memoized

/**
 * A method response defined in the specification.
 */
@Immutable
class SpecificationResponse {
    /** The status code. */
    String statusCode

    /** The map of content type to response model. */
    Map<String, SpecificationModel> models = [:]

    /** The header parameters. */
    List<SpecificationParameter> headers = []

    /**
     * @return a map of AWS parameter names to required flag
     */
    @Memoized
    Map<String, Boolean> getAwsResponseHeaders() {
        headers.collectEntries { param ->
            [ (param.awsResponseParameterName): param.required ]
        }
    }
}
