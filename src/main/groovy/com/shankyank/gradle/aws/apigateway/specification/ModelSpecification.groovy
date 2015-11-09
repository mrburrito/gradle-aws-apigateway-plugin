package com.shankyank.gradle.aws.apigateway.specification

import groovy.transform.Canonical

/**
 * A model defined by the specification.
 */
@Canonical
class ModelSpecification {
    /** The name of the model. */
    String name

    /** The description of the model. */
    String description

    /** The model schema. */
    String schema

    /** The model content type. */
    String contentType
}
