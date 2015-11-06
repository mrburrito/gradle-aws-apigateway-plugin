package com.shankyank.gradle.aws.apigateway.model

/**
 * HTTP operations supported by API Gateway
 */
enum HttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    OPTIONS,
    PATCH

    /**
     * @return the AWS httpMethod name
     */
    String getAwsHttpMethod() {
        name().toLowerCase()
    }

    /**
     * Returns the HTTP operation for the specified AWS httpMethod string.
     * @param httpMethod the AWS httpMethod
     * @return the HTTP operation
     */
    static HttpMethod fromAwsHttpMethod(final String httpMethod) {
        HttpMethod.valueOf(httpMethod?.toUpperCase())
    }
}
