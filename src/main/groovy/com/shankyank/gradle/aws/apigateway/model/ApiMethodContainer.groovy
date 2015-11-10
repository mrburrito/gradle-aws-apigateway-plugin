package com.shankyank.gradle.aws.apigateway.model

/**
 * Models that contain an ApiMethod.
 */
trait ApiMethodContainer extends ApiResourceContainer {
    /** The ApiMethod. */
    ApiMethod method

    /**
     * @return the method operation
     */
    HttpMethod getHttpOperation() {
        method.operation
    }

    /**
     * @return the AWS http method
     */
    String getAwsHttpMethod() {
        httpOperation.awsHttpMethod
    }
}
