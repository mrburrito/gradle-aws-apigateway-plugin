package com.shankyank.gradle.aws.apigateway.model

import com.amazonaws.services.apigateway.AmazonApiGateway
import com.amazonaws.services.apigateway.model.CreateModelRequest
import com.amazonaws.services.apigateway.model.CreateResourceRequest
import com.amazonaws.services.apigateway.model.DeleteRestApiRequest
import com.amazonaws.services.apigateway.model.GetModelRequest
import com.amazonaws.services.apigateway.model.GetModelsRequest
import com.amazonaws.services.apigateway.model.GetResourceRequest
import com.amazonaws.services.apigateway.model.GetResourcesRequest
import com.amazonaws.services.apigateway.model.Model
import com.amazonaws.services.apigateway.model.Resource
import com.amazonaws.services.apigateway.model.RestApi
import com.shankyank.gradle.aws.apigateway.specification.ApiSpecification
import com.shankyank.gradle.aws.apigateway.specification.ModelSpecification
import com.shankyank.gradle.aws.apigateway.specification.ResourceSpecification
import groovy.util.logging.Slf4j
import org.gradle.api.GradleException

/**
 * Decorator around API Gateway RestApi model.
 */
@Slf4j('logger')
class Api implements ApiGatewayContainer {
    /** The wrapped RestApi. */
    private final RestApi restApi

    Api(AmazonApiGateway apiGateway, RestApi restApi) {
        this.apiGateway = apiGateway
        this.restApi = restApi
    }

    /**
     * @return the ID of the contained API
     */
    String getApiId() {
        restApi.id
    }

    /**
     * @return the name of the contained API
     */
    String getApiName() {
        restApi.name
    }

    /**
     * @return the description of the contained API
     */
    String getApiDescription() {
        restApi.description
    }

    /**
     * @return the created date of the contained API
     */
    Date getApiCreatedDate() {
        restApi.createdDate.clone()
    }

    /**
     * Refreshes the API, clearing any previously defined models and resources before
     * populating the API based on the provided specification.
     * @param specficiation the API specification
     */
    void refreshApi(final ApiSpecification specification) {
        clearApi()
        populateApi(specification)
    }

    /**
     * Populate the API according to the provided Specification.
     * @param specification the API specification
     */
    void populateApi(final ApiSpecification specification) {
        specification.models.each { getOrCreateModel(it) }
        createResources(specification.rootResource)
    }

    /**
     * Delete this API.
     */
    void delete() {
        info("Deleting API")
        apiGateway.deleteRestApi(new DeleteRestApiRequest(restApiId: apiId))
    }

    /**
     * Clear this API, removing all models and resources.
     */
    void clearApi() {
        deleteResources()
        deleteModels()
    }

    /**
     * Delete all models from this API.
     */
    void deleteModels() {
        models.each { it.delete() }
    }

    /**
     * Delete all resources from this API.
     */
    void deleteResources() {
        topLevelResources.each { it.delete() }
        rootResource?.clearMethods()
    }

    /**
     * @return the models found in this API
     */
    List<ApiModel> getModels() {
        collectPagedResults(new GetModelsRequest(restApiId: apiId), apiGateway.&getModels).collect(this.&wrapModel)
    }

    /**
     * Get the model with the given name.
     * @param name the name of the model
     * @return the model
     */
    ApiModel findModelByName(final String name) {
        debug("Retrieving Model '${name}'")
        ApiModel model = findNullableObject {
            wrapModel(apiGateway.getModel(new GetModelRequest(restApiId: apiId, modelName: name)))
        }
        debug("Model '${name}': ${model ?: 'Not Found'}")
        model
    }

    /**
     * Create a new model.
     * @param model the model to create
     * @return the created model
     */
    ApiModel createModel(final ModelSpecification model) {
        if (logger.debugEnabled) {
            debug("Creating Model '${model.name}' with Schema: ${model.schema}")
        } else {
            info("Creating Model '${model.name}'")
        }
        try {
            wrapModel(apiGateway.createModel(new CreateModelRequest(
                    restApiId: apiId,
                    name: model.name,
                    description: model.description,
                    contentType: model.contentType,
                    schema: model.schema
            )))
        } catch (ex) {
            throw new GradleException(tagLogMessageWithApi("Error Creating Model '${model.name}': ${ex}"), ex)
        }
    }

    /**
     * Gets the requested model, creating it from the specification if it
     * does not already exist.
     * @param model the model specification
     * @return the model
     */
    ApiModel getOrCreateModel(ModelSpecification model) {
        findModelByName(model.name) ?: createModel(model)
    }

    /**
     * @return the root resource in this API
     */
    ApiResource getRootResource() {
        findResourceByPath('/')
    }

    /**
     * @return the Resources in this API
     */
    List<ApiResource> getResources() {
        collectPagedResults(new GetResourcesRequest(restApiId: apiId),
                apiGateway.&getResources).collect(this.&wrapResource)
    }

    /**
     * @return the Resources in the API directly below the root resource
     */
    List<ApiResource> getTopLevelResources() {
        collectPagedResults(new GetResourcesRequest(restApiId: apiId), apiGateway.&getResources) {
            it.path == "/${it.pathPart}"
        }.collect(this.&wrapResource)
    }

    /**
     * @return a map of Resource path to Resource
     */
    Map<String, ApiResource> getResourceMap() {
        resources.collectEntries { [ (it.path): it ] }
    }

    /**
     * Find the resource with the given path.
     * @param path the path to the resource
     * @return the requested resource
     */
    ApiResource findResourceByPath(final String path) {
        resourceMap[path]
    }

    /**
     * Create all resources in the tree rooted at the provided resource.
     * @param rootResource the root of the resource tree to create
     */
    void createResources(final ResourceSpecification rootResource) {
        Map<String, ApiResource> resourceCache = resourceMap
        rootResource.flattenedResourceTree.each { resource ->
            if (!resourceCache[resource.path]) {
                ApiResource created = createResource(resource, resourceCache[resource.parentPath])
                resourceCache[created.path] = created
            }
        }
    }

    /**
     * Generate a log message that identifies this API.
     * @param message the log message
     * @return the message, formatted to include an identifier for this API
     */
    String tagLogMessageWithApi(final String message) {
        "${this}: ${message}"
    }

    @Override
    String toString() {
        "RestApi[${apiId}: ${apiName}]"
    }

    /**
     * Log a debug message.
     * @param message the message
     * @param error the error
     */
    private void debug(final String message, final Throwable error=null) {
        logger.debug(tagLogMessageWithApi(message), error)
    }

    /**
     * Log an info message.
     * @param message the message
     * @param error the error
     */
    private void info(final String message, final Throwable error=null) {
        logger.info(tagLogMessageWithApi(message), error)
    }

    /**
     * Create a resource as a child of the provided parent.
     * @param specification the Resource specification
     * @param parent the parent resource
     * @return the created resource
     */
    private ApiResource createResource(final ResourceSpecification specification, final ApiResource parent) {
        info("Creating Resource '${specification.path}' with parent ${parent?.resourceId}")
        try {
            ApiResource resource = wrapResource(apiGateway.createResource(new CreateResourceRequest(
                    restApiId: apiId,
                    parentId: parent?.resourceId,
                    pathPart: specification.name
            )))
            specification.operations.values().each { resource.createMethod(it) }
            resource
        } catch (ex) {
            throw new GradleException(tagLogMessageWithApi("Error Creating Resource '${specification.path}'"), ex)
        }
    }

    private ApiModel wrapModel(final Model model) {
        model?.with { new ApiModel(this, it) }
    }

    private ApiModel wrapModel(final def modelResult) {
        wrapModel(modelResult?.with {
            new Model(
                    id: id,
                    name: name,
                    description: description,
                    contentType: contentType,
                    schema: schema
            )
        })
    }

    private ApiResource wrapResource(final Resource resource) {
        resource?.with { new ApiResource(this, it) }
    }

    private ApiResource wrapResource(final def resourceResult) {
        wrapResource(resourceResult?.with {
            new Resource(
                    id: id,
                    parentId: parentId,
                    path: path,
                    pathPart: pathPart,
                    resourceMethods: resourceMethods
            )
        })
    }
}
