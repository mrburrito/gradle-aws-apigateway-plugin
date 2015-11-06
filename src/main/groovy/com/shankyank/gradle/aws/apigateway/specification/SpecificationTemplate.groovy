package com.shankyank.gradle.aws.apigateway.specification

import groovy.transform.Immutable

/**
 * Created by gshankman on 11/5/15.
 */
@Immutable
class SpecificationTemplate {
    /** The content type. */
    String contentType

    /** The template. */
    String template
}
