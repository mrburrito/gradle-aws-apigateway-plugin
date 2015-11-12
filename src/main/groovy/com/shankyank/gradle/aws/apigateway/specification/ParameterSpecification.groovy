package com.shankyank.gradle.aws.apigateway.specification

import com.shankyank.gradle.aws.apigateway.model.ParameterLocation
import groovy.transform.Canonical

/**
 * A method parameter defined in the specification.
 */
@Canonical
class ParameterSpecification {
    /** The parameter location. */
    ParameterLocation location

    /** The parameter name. */
    String name

    /** Is this parameter required? */
    boolean required

    /**
     * @return the parameter name as it appears in an AWS request integration
     */
    String getAwsRequestParameterName() {
        "method.request.${location.awsName}.${name}"
    }

    /**
     * @return the parameter name as it appears in an AWS response integration
     */
    String getAwsResponseParameterName() {
        if (location != ParameterLocation.HEADER) {
            throw new UnsupportedParameterLocation(
                    "Response parameter '${location}: ${name}' must be a HEADER parameter.")
        }
        "method.response.${location.awsName}.${name}"
    }

    static class UnsupportedParameterLocation extends RuntimeException {
        UnsupportedParameterLocation(final String message) {
            super(message)
        }
    }
}
