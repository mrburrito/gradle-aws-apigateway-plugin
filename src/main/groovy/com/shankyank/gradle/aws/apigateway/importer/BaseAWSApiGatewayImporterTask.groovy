package com.shankyank.gradle.aws.apigateway.importer

import com.shankyank.gradle.aws.apigateway.AWSApiGatewayPluginExtension
import com.shankyank.gradle.aws.apigateway.model.Api
import com.shankyank.gradle.aws.apigateway.model.ApiGateway
import com.shankyank.gradle.aws.apigateway.specification.ApiSpecification
import com.shankyank.gradle.aws.apigateway.specification.ApiSpecificationFactory
import com.shankyank.gradle.aws.apigateway.specification.ApiSpecificationFactory.SpecificationType
import groovy.transform.Memoized
import groovy.transform.PackageScope
import org.gradle.api.GradleException
import org.gradle.api.internal.ConventionTask

/**
 * Base class for tasks that import a specification file into API Gateway.
 */
@PackageScope
class BaseAWSApiGatewayImporterTask extends ConventionTask {
    /** The specification file */
    File specificationFile

    protected BaseAWSApiGatewayImporterTask() {
        group = 'aws: apigateway'
    }

    /**
     * Creates a new RestApi based on the loaded specification.
     * @return a decorated RestApi
     */
    protected Api createApi() {
        apiGateway.createApi(specification)
    }

    /**
     * Updates an existing RestApi to match the loaded specification.
     * @param api the api to update
     */
    protected void updateApi(final String apiId) {
        Api api = apiGateway.getApiById(apiId)
        if (api) {
            updateApi(api)
        } else {
            throw new GradleException("No API found with ID ${apiId}")
        }
    }

    /**
     * Updates an existing Api to match the loaded specification.
     * @param api the api to update
     */
    protected void updateApi(final Api api) {
        logger.debug("Updating ${api}")
        api.refreshApi(specification)
    }

    /**
     * Parses the specification from the provided file.
     * @return the parsed specification
     */
    @Memoized
    protected final ApiSpecification getSpecification() {
        ApiSpecificationFactory.instance.createApiSpecification(specificationFile)
    }

    /**
     * @return the defined plugin extension
     */
    @Memoized
    protected final AWSApiGatewayPluginExtension getPluginExtension() {
        extensions.findByType(AWSApiGatewayPluginExtension)
    }

    /**
     * @return an AmazonApiGateway client
     */
    @Memoized
    protected final ApiGateway getApiGateway() {
        new ApiGateway(pluginExtension.apiGateway)
    }

    /**
     * @return the list of supported specifications as a joined string
     */
    @Memoized
    protected final String getSupportedSpecifications() {
        List types = [] + SpecificationType.values()
        SpecificationType last = types.last()
        types -= last
        "${types.join(', ')}${types.empty ? '' : ' or '}${last}"
    }
}
