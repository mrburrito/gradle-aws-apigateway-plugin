package com.shankyank.gradle.aws.apigateway.specification

import com.shankyank.gradle.aws.apigateway.model.HttpMethod
import groovy.transform.Immutable

/**
 * A resource method defined by the specification.
 */
@Immutable
class SpecificationMethod {
    /** The default authorization type. */
    static final String DEFAULT_AUTHORIZATION_TYPE = 'NONE'

    /** The operation type. */
    HttpMethod operation

    /** The authorization type. */
    String authorizationType = DEFAULT_AUTHORIZATION_TYPE

    /** Is an API key required for this operation? */
    boolean apiKeyRequired

    /** The map of content type to request body model. */
    Map<String, SpecificationModel> bodyModels = [:]

    /** The non-BODY method parameters. */
    List<SpecificationParameter> parameters = []

    /** The request integration. */
    SpecificationRequestIntegration requestIntegration

    /** The method responses. */
    List<SpecificationResponse> responses = []

    /** The response integrations. */
    List<SpecificationRequestIntegration> responseIntegrations = []

    /**
     * @return the AWS HttpMethod string
     */
    String getAwsHttpMethod() {
        operation.awsHttpMethod
    }
}
