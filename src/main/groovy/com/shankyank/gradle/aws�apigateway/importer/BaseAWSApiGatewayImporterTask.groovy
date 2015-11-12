package com.shankyank.gradle.aws.apigateway.importer

import com.shankyank.gradle.aws.apigateway.BaseAWSApiGatewayTask
import com.shankyank.gradle.aws.apigateway.model.Api
import com.shankyank.gradle.aws.apigateway.specification.ApiSpecification
import com.shankyank.gradle.aws.apigateway.specification.ApiSpecificationFactory
import com.shankyank.gradle.aws.apigateway.specification.ApiSpecificationFactory.SpecificationType
import groovy.transform.Memoized
import groovy.transform.PackageScope
import org.gradle.api.tasks.InputFile

/**
 * Base class for tasks that import a specification file into API Gateway.
 */
@PackageScope
class BaseAWSApiGatewayImporterTask extends BaseAWSApiGatewayTask {
    /** The specification files */
    @InputFile
    File specificationFile

    /**
     * Creates a new RestApi based on the loaded specification.
     * @return a decorated RestApi
     */
    protected Api createApi() {
        Api api = apiGateway.createApi(specification)
        if (api) {
            logger.quiet("Created API ${api.apiName} with ID ${api.apiId}")
        } else {
            logger.error("API Creation Failed")
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
     * @return the list of supported specifications as a joined string
     */
    @Memoized
    protected final String getSupportedSpecifications() {
        List types = [] + (SpecificationType.values() as List)
        SpecificationType last = types.last()
        types -= last
        "${types.join(', ')}${types.empty ? '' : ' or '}${last}"
    }
}
