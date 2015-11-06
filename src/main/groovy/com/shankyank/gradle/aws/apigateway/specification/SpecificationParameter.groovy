package com.shankyank.gradle.aws.apigateway.specification

import com.shankyank.gradle.aws.apigateway.model.ParameterLocation
import groovy.transform.Immutable

/**
 * A method parameter defined in the specification.
 */
@Immutable
class SpecificationParameter {
    /** The parameter location. */
    final ParameterLocation location

    /** The parameter name. */
    final String name

    /** Is this parameter required? */
    final boolean required

    @Override
    String getAwsRequestParameterName() {
        "method.request.${location.awsName}.${name}"
    }

    @Override
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
