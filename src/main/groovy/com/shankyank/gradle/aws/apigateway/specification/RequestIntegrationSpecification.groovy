package com.shankyank.gradle.aws.apigateway.specification

import com.amazonaws.services.apigateway.model.IntegrationType
import com.shankyank.gradle.aws.apigateway.model.HttpMethod
import groovy.transform.Canonical

/**
 * An API Gateway request integration defined in the specification.
 */
@Canonical
class RequestIntegrationSpecification {
    /** The integration type. */
    IntegrationType type

    /** The URI. */
    String uri

    /** The credentials. */
    String credentials

    /** The HTTP method. */
    HttpMethod httpMethod

    /** The map of destination to source parameters. */
    Map<String, String> parameters = [:]

    /** The map of content type to transformation template. */
    Map<String, String> templates = [:]

    /** The cache namespace. */
    String cacheNamespace

    /** The cache key parameters. */
    List<String> cacheKeyParameters = []
}
