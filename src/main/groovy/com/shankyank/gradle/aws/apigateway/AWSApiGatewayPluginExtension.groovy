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
import com.amazonaws.retry.RetryPolicy
import com.amazonaws.retry.RetryPolicy.BackoffStrategy
import com.amazonaws.retry.RetryPolicy.RetryCondition
import com.amazonaws.services.apigateway.AmazonApiGateway
import com.amazonaws.services.apigateway.AmazonApiGatewayClient
import groovy.transform.Memoized
import groovy.util.logging.Slf4j
import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * Configuration for the AWSApiGatewayPlugin.
 */
@Slf4j
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

    /** The retry condition. */
    private static final RetryCondition RETRY_CONDITION = new RateLimitRetryCondition()

    /** The backoff strategy. */
    private static final BackoffStrategy BACKOFF_STRATEGY = new RateLimitBackoffStrategy()

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

    /** The maximum number of retries, defaults to 8 (total retry delay of ~51s) */
    int maxRetries = 8

    /**
     * Initialize the plugin extension.
     * @param proj the containing project
     */
    AWSApiGatewayPluginExtension(final Project proj) {
        this.project = proj
        log.info("Created AWSApiGatewayPluginExtension in ${proj}")
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
        ClientConfiguration config = new ClientConfiguration(retryPolicy: retryPolicy, maxErrorRetry: maxRetries)
        if (proxyDefined) {
            config.proxyHost = proxyHost
            config.proxyPort = proxyPort
        }
        project.logger.info("API Gateway Client Configuration: ${config}")
        config
    }

    protected boolean isProxyDefined() {
        proxyHost?.trim() && proxyPort > 0
    }

    protected RetryPolicy getRetryPolicy() {
        new RetryPolicy(RETRY_CONDITION, BACKOFF_STRATEGY, maxRetries, true)
    }

    protected boolean isProfileConfigured() {
        profile?.trim()
    }

    protected boolean isNonDefaultProfile() {
        profile?.trim() != DEFAULT_PROFILE
    }
}
