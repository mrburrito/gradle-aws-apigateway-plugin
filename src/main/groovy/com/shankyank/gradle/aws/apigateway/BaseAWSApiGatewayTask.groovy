package com.shankyank.gradle.aws.apigateway

import com.shankyank.gradle.aws.apigateway.model.Api
import com.shankyank.gradle.aws.apigateway.model.ApiGateway
import groovy.transform.Memoized
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException

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

    /**
     * Finds a single Api with the provided name, returning null
     * if it does not exist and generating an error if more than
     * one Api is found with the requested name.
     * @param name the name of the Api
     * @return the requested Api, if only one Api has the specified name or null if the Api does not exist
     * @throws GradleException if more than one Api is found with the provided name
     */
    protected final Api findSingleApiWithName(final String name) {
        List<Api> namedApis = apiGateway.findApisByName(name)
        Api api = null
        switch (namedApis.size()) {
            case 0:
                logger.info("No APIs found with name '${name}'.")
                break
            case 1:
                api = namedApis[0]
                logger.info("Found API ${api} with name '${name}'")
                break
            default:
                logger.error("Found ${namedApis.size()} APIs with name '${name}:\n\t${namedApis.join('\n\t')}")
                throw new GradleException("Found ${namedApis.size()} APIs with name '${name}'.")
        }
        api
    }
}
