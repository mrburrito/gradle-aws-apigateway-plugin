package com.shankyank.gradle.aws.apigateway.model

/**
 * A parameter location.
 */
enum ParameterLocation {
    PATH('path'),
    QUERY('querystring'),
    HEADER('header')

    /** The name of this location in an API Gateway integration path. */
    final String awsName

    private ParameterLocation(final String awsName) {
        this.awsName = awsName
    }
}
