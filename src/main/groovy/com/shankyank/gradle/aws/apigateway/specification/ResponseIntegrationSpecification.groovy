package com.shankyank.gradle.aws.apigateway.specification

import groovy.transform.Canonical

/**
 * An API Gateway response integration defined in the specification.
 */
@Canonical
class ResponseIntegrationSpecification {
    /** The selection pattern. */
    String selectionPattern

    /** The status code. */
    String statusCode

    /** The map of destination to source parameters. */
    Map<String, String> parameters = [:]

    /** The map of content type to transformation template. */
    Map<String, String> templates = [:]
}
