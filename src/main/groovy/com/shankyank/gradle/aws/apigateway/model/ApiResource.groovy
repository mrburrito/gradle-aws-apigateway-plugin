package com.shankyank.gradle.aws.apigateway.model

import com.amazonaws.services.apigateway.model.DeleteResourceRequest
import com.amazonaws.services.apigateway.model.Method
import com.amazonaws.services.apigateway.model.PutMethodRequest
import com.amazonaws.services.apigateway.model.PutMethodResult
import com.amazonaws.services.apigateway.model.Resource
import com.shankyank.gradle.aws.apigateway.specification.MethodSpecification
import groovy.util.logging.Slf4j

/**
 * Decorator around a Resource.
 */
@Slf4j('logger')
class ApiResource implements ApiContainer {
    /** The decorated Resource. */
    private final Resource resource

    ApiResource(final Api api, final Resource resource) {
        this.apiGateway = api.apiGateway
        this.api = api
        this.resource = resource
    }

    /**
     * @return the resource ID
     */
    String getResourceId() {
        resource.id
    }

    /**
     * @return the resource path
     */
    String getPath() {
        resource.path
    }

    /**
     * Delete this resource.
     */
    void delete() {
        if (rootResource) {
            clearMethods()
        } else {
            debug("Deleting Resource '${resourceId}: ${path}'")
            apiGateway.deleteResource(new DeleteResourceRequest(
                    restApiId: apiId,
                    resourceId: resourceId
            ))
        }
    }

    /**
     * Clear the methods from this resource.
     */
    void clearMethods() {
        debug("Clearing methods from Resource '${resourceId}: ${path}'")
        methods.values().each { it.delete() }
    }

    /**
     * @return the map of HttpOp to ApiMethod for this resource
     */
    Map<HttpMethod, ApiMethod> getMethods() {
        resource.resourceMethods?.collectEntries { op, method ->
            [ (HttpMethod.fromAwsHttpMethod(op)): wrapMethod(method) ]
        } ?: [:]
    }

    /**
     * Gets the ApiMethod for the specified operation.
     * @param op the operation type
     * @return the method for the specified HTTP operation
     */
    Optional<ApiMethod> getMethodForOp(final HttpMethod op) {
        Optional.ofNullable(wrapMethod(methods[op]))
    }

    /**
     * Create a new method based on the speicfication.
     * @param method the Method specification
     * @return the created ApiMethod
     */
    ApiMethod createMethod(final MethodSpecification method) {
        info("Creating Method '${method.httpMethod} ${path}'")
        wrapMethod(apiGateway.putMethod(new PutMethodRequest(
                restApiId: apiId,
                resourceId: resourceId,
                httpMethod: method.awsHttpMethod,
                apiKeyRequired: method.apiKeyRequired,
                authorizationType: method.authorizationType,
                requestModels: mapRequestModelsByContentType(method),
                requestParameters: mapRequestParametersToRequiredFlag(method)
        )), method)
    }

    /**
     * @return true if this is the root resource
     */
    boolean isRootResource() {
        !path || path == '/'
    }

    private Map<String, Boolean> mapRequestParametersToRequiredFlag(final MethodSpecification method) {
        method.parameters.collectEntries { parameter ->
            [ (parameter.awsRequestParameterName): parameter.required ]
        }
    }

    private Map mapRequestModelsByContentType(final MethodSpecification method) {
        method.bodyModels.collectEntries { contentType, model ->
            [ (contentType): api.getOrCreateModel(model).name ]
        }
    }

    private ApiMethod wrapMethod(final Method method, final MethodSpecification specification=null) {
        method?.with { new ApiMethod(this, it, specification) }
    }

    private ApiMethod wrapMethod(final PutMethodResult method, final MethodSpecification specification=null) {
        wrapMethod(method?.with {
            new Method(
                    httpMethod: httpMethod,
                    apiKeyRequired: apiKeyRequired,
                    authorizationType: authorizationType,
                    requestModels: requestModels,
                    requestParameters: requestParameters
            )
        }, specification)
    }
}
