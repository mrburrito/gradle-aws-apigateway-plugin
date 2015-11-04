package com.shankyank.gradle.aws.apigateway.model

import com.amazonaws.services.apigateway.model.CreateModelRequest
import com.amazonaws.services.apigateway.model.GetModelRequest
import com.amazonaws.services.apigateway.model.GetModelsRequest
import com.amazonaws.services.apigateway.model.GetResourcesRequest
import com.amazonaws.services.apigateway.model.Model
import com.shankyank.gradle.aws.apigateway.specification.SpecificationModel
import groovy.util.logging.Slf4j
import org.gradle.api.GradleException

/**
 * Decorator around API Gateway RestApi model.
 */
@Slf4j('logger')
class Api implements RestApiDecorator {
    /**
     * @return the models found in this API
     */
    List<ApiModel> getModels() {
        collectPagedResults(new GetModelsRequest().withRestApiId(apiId), apiGateway.&getModels).collect {
            new ApiModel(this, it)
        }
    }

    /**
     * Get the model with the given name.
     * @param name the name of the model
     * @return the model
     */
    Optional<ApiModel> findModelByName(final String name) {
        logger.debug("Retrieving Model '${name}' in ${apiNameForLog}")
        findOptionalObject {
            toApiModel(apiGateway.getModel(new GetModelRequest().withRestApiId(apiId).withModelName(name)))
        }
    }

    /**
     * Create a new model.
     * @param model the model to create
     * @return the created model
     */
    ApiModel createModel(final SpecificationModel model) {
        logger.debug("Creating Model '${model.name}' in ${apiNameForLog}")
        try {
            toApiModel(apiGateway.createModel(new CreateModelRequest().
                    withRestApiId(apiId).
                    withName(model.name).
                    withDescription(model.description).
                    withContentType(model.contentType).
                    withSchema(model.schema)))
        } catch (ex) {
            throw new GradleException("Error Creating Model '${model.name}' in ${apiNameForLog}: ${ex}", ex)
        }
    }

    /**
     * @return the Resources in this API
     */
    List<ApiResource> getResources() {
        collectPagedResults(new GetResourcesRequest().withRestApiId(apiId), apiGateway.&getResources).collect {
            new ApiResource(this, it)
        }
    }

    /**
     * @return a map of Resource path to Resource
     */
    Map<String, ApiResource> getResourceMap() {
        resources.collectEntries { [ (it.resourcePath): it ] }
    }

    /**
     * Find the resource with the given path.
     * @param path the path to the resource
     * @return the requested resource
     */
    Optional<ApiResource> findResourceByPath(final String path) {
        Optional.ofNullable(resourceMap[path])
    }

    private ApiModel toApiModel(final def modelResult) {
        modelResult?.with {
            new ApiModel(this, new Model().
                    withId(id).
                    withName(name).
                    withDescription(description).
                    withContentType(contentType).
                    withSchema(schema))
        }
    }
}
