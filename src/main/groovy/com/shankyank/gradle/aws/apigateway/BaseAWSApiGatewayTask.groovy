package com.shankyank.gradle.aws.apigateway

import com.shankyank.gradle.aws.apigateway.model.ApiGateway
import groovy.transform.Memoized
import org.gradle.api.DefaultTask

/**
 * Base plugin for all API Gateway plugins.
 */
abstract class BaseAWSApiGatewayTask extends DefaultTask {
    protected BaseAWSApiGatewayTask() {
        group = 'AWS: API Gateway'
    }

    /**
     * @return the defined plugin extension
     */
    @Memoized
    protected final AWSApiGatewayPluginExtension getPluginExtension() {
        project.extensions.findByType(AWSApiGatewayPluginExtension)
    }

    /**
     * @return an AmazonApiGateway client
     */
    @Memoized
    protected final ApiGateway getApiGateway() {
        new ApiGateway(pluginExtension.apiGateway)
    }
}
