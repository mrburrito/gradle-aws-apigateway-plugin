package com.shankyank.gradle.aws.apigateway.importer

import com.amazonaws.services.apigateway.AmazonApiGateway
import com.amazonaws.services.apigateway.model.CreateModelRequest
import com.amazonaws.services.apigateway.model.CreateResourceRequest
import com.amazonaws.services.apigateway.model.CreateRestApiRequest
import com.amazonaws.services.apigateway.model.CreateRestApiResult
import com.amazonaws.services.apigateway.model.DeleteModelRequest
import com.amazonaws.services.apigateway.model.GetModelsRequest
import com.amazonaws.services.apigateway.model.GetResourcesRequest
import com.amazonaws.services.apigateway.model.GetRestApiRequest
import com.amazonaws.services.apigateway.model.GetRestApiResult
import com.amazonaws.services.apigateway.model.GetRestApisRequest
import com.amazonaws.services.apigateway.model.Model
import com.amazonaws.services.apigateway.model.Resource
import com.amazonaws.services.apigateway.model.RestApi
import com.shankyank.gradle.aws.apigateway.AWSApiGatewayPluginExtension
import com.shankyank.gradle.aws.apigateway.specification.ApiSpecification
import com.shankyank.gradle.aws.apigateway.specification.ApiSpecificationFactory
import com.shankyank.gradle.aws.apigateway.specification.ApiSpecificationFactory.SpecificationType
import com.shankyank.gradle.aws.apigateway.specification.SpecificationMethod.Operation
import com.shankyank.gradle.aws.apigateway.specification.SpecificationMethod
import com.shankyank.gradle.aws.apigateway.specification.SpecificationModel
import com.shankyank.gradle.aws.apigateway.specification.SpecificationResource
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

    /**
     * The API Gateway configuration. Required if specification file does not
     * support API Gateway extensions.
     */
    File apiGatewayConfigFile

    protected BaseAWSApiGatewayImporterTask() {
        group = 'aws: apigateway'
    }

    /**
     * Creates a new RestApi based on the loaded specification.
     * @return the created RestApi
     */
    protected RestApi createApi() {
        logger.debug("Creating RestApi named: ${specification.name}")

        CreateRestApiResult result = apiGateway.createRestApi(new CreateRestApiRequest().
                withName(specification.name).
                withDescription(specification.description))
        logger.debug("Created RestApi[${specification.name}]: ${result}")
        toRestApi(result)
    }

    /**
     * Updates an existing RestApi to match the loaded specification.
     * @param api the api to update
     */
    protected void updateApi(final RestApi api) {
        logger.debug("Updating RestApi[${api.id}: '${api.name}]'")
        RestApiHelper apiHelper = new RestApiHelper(api)
        apiHelper.deleteModels()
        apiHelper.createModels()
        apiHelper.createResources()
    }

    /**
     * Parses the specification from the provided file.
     * @return the parsed specification
     */
    @Memoized
    protected final ApiSpecification getSpecification() {
        ApiSpecificationFactory.createApiSpecification(specificationFile)
    }

    /**
     * @return the defined plugin extension
     */
    protected final AWSApiGatewayPluginExtension getPluginExtension() {
        extensions.findByType(AWSApiGatewayPluginExtension)
    }

    /**
     * @return an AmazonApiGateway client
     */
    protected final AmazonApiGateway getApiGateway() {
        pluginExtension.apiGateway
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

    /**
     * Gets the RestApi with the given ID.
     * @param apiId the API id
     * @return the RestApi for that ID
     */
    @Memoized
    protected RestApi getRestApiByID(final String apiId) {
        logger.debug("Retrieving RestApi ${apiId}")
        GetRestApiResult result = apiGateway.getRestApi(new GetRestApiRequest().withRestApiId(apiId))
        logger.debug("Found RestApi[${apiId}]: ${result}")
        toRestApi(result)
    }

    /**
     * Finds all existing RestApis with the given name.
     * @param name the name of the API
     * @return all existing RestApis with the given name
     */
    @Memoized
    protected List<RestApi> getNamedRestApis(final String name) {
        collectPagedResults(new GetRestApisRequest(), apiGateway.&getRestApis) { it.name == name }
    }

    /**
     * Converts an ApiGateway result object to a RestApi.
     * @param result the results from an ApiGateway call returning a single RestApi
     * @return a RestApi object
     */
    private RestApi toRestApi(final Object result) {
        result?.with {
            new RestApi().
                    withId(id).
                    withName(name).
                    withDescription(description).
                    withCreatedDate(createdDate)
        }
    }

    /**
     * Executes a pageable call to the API Gateway service, iterating over
     * the pages and returning the items found in a single list. The optional
     * `filter` closure should accept a single argument, the target item, returning
     * `true` if the item should be included in the list.
     *
     * This method assumes `getPage` accepts a single argument of `baseRequest`
     * and returns a result object with the properties `position` and `items`. It also
     * assumes `baseRequest` has `withPosition()` method that configures paging for the
     * request. This is consistent with all API Gateway request/response models.
     *
     * @param baseRequest the base request object with all parameters other than paging details configured
     * @param getPage a Closure, typically a method pointer on an AmazonApiGateway client, that accepts
     *                a request and returns a paged response
     * @param filter an optional filter, defaulting to the identity function, restricting the values returned
     * @return the List of all items returned by the pageable request
     */
    private List collectPagedResults(final def baseRequest, final Closure getPage,
                                     final Closure filter=Closure.IDENTITY) {
        def result = getPage(baseRequest)
        def nextPage = {
            result.position ? getPage(baseRequest.withPosition(result.position)) : null
        }
        List results = []
        while ((result = nextPage())?.items) {
            results += result.items.findAll(filter)
        }
        results
    }

    /**
     * Wraps a RestApi and provides methods for altering its contents.
     */
    private class RestApiHelper {
        /** The API */
        final RestApi api

        RestApiHelper(RestApi restApi) {
            this.api = restApi
        }

        /**
         * Deletes all models found in the API.
         */
        void deleteModels() {
            models.each { model ->
                logger.debug("Removing Model '${model.name}' from ${apiLogDescription}")
                apiGateway.deleteModel(new DeleteModelRequest().withRestApiId(api.id).withModelName(model.name))
            }
        }

        /**
         * @return the Models in the API
         */
        List<Model> getModels() {
            collectPagedResults(new GetModelsRequest().withRestApiId(api.id), apiGateway.&getModels)
        }

        /**
         * Creates the models found in the loaded Specification.
         */
        void createModels() {
            specification.models.each(this.&createModel)
        }

        /**
         * @return the Resources in the API
         */
        List<Resource> getResources() {
            collectPagedResults(new GetResourcesRequest().withRestApiId(api.id), apiGateway.&getResources)
        }

        /**
         * @return the Resources in the API, mapped by path
         */
        Map<String, Resource> getResourceMap() {
            resources.collectEntries { resource -> [ (resource.path): resource ] }
        }

        /**
         * Creates the resources and operations found in the loaded Specification.
         */
        void createResources() {
            Map<String, Resource> existingResources = resourceMap
            specification.rootResource.flattenedResourceTree.each { specResource ->
                if (existingResources[specResource.name]) {
                    updateResource(existingResources[specResource.name], specResource)
                } else {
                    createResource(existingResources[specResource.parentPath], specResource).with { created ->
                        existingResources[created.path] = created
                        createMethods(created, specResource.operations)
                    }
                }
            }
        }

        /**
         * Creates the methods for a particular resource.
         * @param resource the target resource
         * @param operations the operations to create
         */
        void createMethods(final Resource resource, final Map<Operation, SpecificationMethod> operations) {
        }

        /**
         * Creates a model in the wrapped RestApi.
         * @param model the model to create
         * @return the created Model
         */
        private Model createModel(final SpecificationModel model) {
            String logMessage = "Creating Model '${model.name}' in ${apiLogDescription}"
            logger.debug(logMessage)
            try {
                apiGateway.createModel(new CreateModelRequest().
                        withRestApiId(api.id).
                        withName(model.name).
                        withDescription(model.description).
                        withContentType(model.contentType).
                        withSchema(model.schema)
                ).with {
                    new Model().
                            withId(id).
                            withName(name).
                            withDescription(description).
                            withContentType(contentType).
                            withSchema(schema)
                }
            } catch (Exception ex) {
                throw new GradleException("Error ${logMessage}: ${ex}", ex)
            }
        }

        /**
         * Creates a resource in the wrapped RestApi.
         * @param parent the parent Resource
         * @param resource the resource to create
         * @return the created Resource
         */
        private Resource createResource(final Resource parent, final SpecificationResource resource) {
            String logMessage = "Creating Resource '${parent.path}/${resource.name} in ${apiLogDescription}"
            logger.debug(logMessage)
            try {
                apiGateway.createResource(new CreateResourceRequest().
                        withRestApiId(api.id).
                        withParentId(parent?.id).
                        withPathPart(resource.name)
                ).with {
                    new Resource().
                            withId(id).
                            withParentId(parentId).
                            withPath(path).
                            withPathPart(pathPart)
                }
            } catch (Exception ex) {
                throw new GradleException("Error ${logMessage}: ${ex}", ex)
            }
        }

        /**
         * Updates a resource
         * @param resource
         * @param importResource
         */
        private void updateResource(final Resource resource, final SpecificationResource importResource) {

        }

        @Memoized
        private String getApiLogDescription() {
            "RestApi[${api.id}: ${api.name}]"
        }
    }
}
