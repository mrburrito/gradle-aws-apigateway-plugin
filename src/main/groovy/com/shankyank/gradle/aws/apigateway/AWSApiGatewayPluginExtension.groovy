package com.shankyank.gradle.aws.apigateway

import com.amazonaws.services.apigateway.AmazonApiGateway
import com.amazonaws.services.apigateway.AmazonApiGatewayClient
import groovy.transform.Memoized
import jp.classmethod.aws.gradle.AwsPluginExtension
import org.gradle.api.Project

/**
 * Configuration for the AWSApiGatewayPlugin.
 */
class AWSApiGatewayPluginExtension {
    /** The containing project. */
    Project project

    /** The AWS profile for configuration and authentication. */
    String profile

    /** The AWS region. */
    String region

    /**
     * Initialize the plugin extension.
     * @param proj the containing project
     */
    AWSApiGatewayPluginExtension(final Project proj) {
        this.project = proj
    }

    /**
     * Constructs an instance of AmazonApiGateway for communication with
     * the API Gateway service.
     * @return the AmazonApiGateway client
     */
    @Memoized
    AmazonApiGateway getApiGateway() {
        AwsPluginExtension aws = project.extensions.getByType(AwsPluginExtension)
        String activeRegion = aws.getActiveRegion(region)
        aws.createClient(AmazonApiGatewayClient, profile).with {
            region = activeRegion
            it
        }
    }
}
