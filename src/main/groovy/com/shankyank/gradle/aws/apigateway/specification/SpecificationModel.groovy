package com.shankyank.gradle.aws.apigateway.specification

import groovy.transform.Immutable

/**
 * A model defined by the specification.
 */
@Immutable
class SpecificationModel {
    /** The name of the model. */
    String name

    /** The description of the model. */
    String description

    /** The model schema. */
    String schema

    /** The model content type. */
    String contentType
}
