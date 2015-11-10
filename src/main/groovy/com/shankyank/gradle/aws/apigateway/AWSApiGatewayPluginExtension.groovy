package com.shankyank.gradle.aws.apigateway

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.AWSCredentialsProviderChain
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.auth.InstanceProfileCredentialsProvider
import com.amazonaws.auth.SystemPropertiesCredentialsProvider
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Region
import com.amazonaws.regions.RegionUtils
import com.amazonaws.regions.Regions
import com.amazonaws.services.apigateway.AmazonApiGateway
import com.amazonaws.services.apigateway.AmazonApiGatewayClient
import groovy.transform.Memoized
import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * Configuration for the AWSApiGatewayPlugin.
 */
class AWSApiGatewayPluginExtension {
    /** The name of the extension. */
    static final String NAME = 'apigateway'

    /** The default profile. */
    static final String DEFAULT_PROFILE = 'default'

    /** The empty credentials provider. */
    protected static final AWSCredentialsProvider EMPTY_PROVIDER = [
            'refresh': {},
            'getCredentials': { null }
    ] as AWSCredentialsProvider

    /** The containing project. */
    Project project

    /** The AWS profile for configuration and authentication. */
    String profile = DEFAULT_PROFILE

    /** The AWS region. */
    String region = Regions.US_EAST_1.name

    /** The proxy host. */
    String proxyHost

    /** The proxy port. */
    int proxyPort = -1

    /**
     * Initialize the plugin extension.
     * @param proj the containing project
     */
    AWSApiGatewayPluginExtension(final Project proj) {
        this.project = proj
        proj.logger.info("Created AWSApiGatewayPluginExtension in ${proj}")
    }

    /**
     * Constructs an instance of AmazonApiGateway for communication with
     * the API Gateway service.
     * @return the AmazonApiGateway client
     */
    @Memoized
    AmazonApiGateway getApiGateway() {
        new AmazonApiGatewayClient(credentialsProvider, clientConfiguration).withRegion(activeRegion)
    }

    protected AWSCredentialsProvider getCredentialsProvider() {
        new AWSCredentialsProviderChain(
                new EnvironmentVariableCredentialsProvider(),
                new SystemPropertiesCredentialsProvider(),
                profileConfigured ? new ProfileCredentialsProvider(profile) : EMPTY_PROVIDER,
                nonDefaultProfile ? new ProfileCredentialsProvider(DEFAULT_PROFILE) : EMPTY_PROVIDER,
                new InstanceProfileCredentialsProvider()
        )
    }

    protected Region getActiveRegion() {
        if (!region) {
            throw new GradleException("Region must be provided")
        }
        RegionUtils.getRegion(region)
    }

    protected ClientConfiguration getClientConfiguration() {
        proxyDefined ? new ClientConfiguration(proxyHost: proxyHost, proxyPort: proxyPort) : new ClientConfiguration()
    }

    protected boolean isProxyDefined() {
        proxyHost?.trim() && proxyPort > 0
    }

    protected boolean isProfileConfigured() {
        profile?.trim()
    }

    protected boolean isNonDefaultProfile() {
        profile?.trim() != DEFAULT_PROFILE
    }
}
