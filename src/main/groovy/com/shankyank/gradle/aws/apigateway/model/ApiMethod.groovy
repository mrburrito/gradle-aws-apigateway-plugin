package com.shankyank.gradle.aws.apigateway.model

import com.amazonaws.services.apigateway.model.DeleteMethodRequest
import com.amazonaws.services.apigateway.model.Method
import com.amazonaws.services.apigateway.model.PutIntegrationRequest
import com.amazonaws.services.apigateway.model.PutIntegrationResponseRequest
import com.amazonaws.services.apigateway.model.PutMethodResponseRequest
import com.shankyank.gradle.aws.apigateway.specification.MethodSpecification
import com.shankyank.gradle.aws.apigateway.specification.RequestIntegrationSpecification
import com.shankyank.gradle.aws.apigateway.specification.ResponseSpecification
import com.shankyank.gradle.aws.apigateway.specification.ResponseIntegrationSpecification
import groovy.transform.Memoized
import groovy.util.logging.Slf4j

/**
 * Decorator around an AWS Method.
 */
@Slf4j('logger')
class ApiMethod implements ApiResourceContainer {
    /** The decorated method. */
    private final Method method

    /**
     * Create a new ApiMethod and initialize its subcomponents based on the
     * provided specification.
     * @param resource the resource that owns this method
     * @param method the API Gateway Method to wrap
     * @param specification the specification for this method
     */
    ApiMethod(final ApiResource resource, final Method method, final MethodSpecification specification=null) {
        this.apiGateway = resource.apiGateway
        this.api = resource.api
        this.resource = resource
        this.method = method
        if (specification) {
            createRequestIntegration(specification.requestIntegration)
            specification.responses.each { response -> createResponse(response) }
            specification.responseIntegrations.each { integration -> createResponseIntegration(integration) }
        }
    }

    /**
     * @return the HTTP method for this Method
     */
    @Memoized
    HttpMethod getOperation() {
        HttpMethod.fromAwsHttpMethod(method.httpMethod)
    }

    /**
     * @return the AWS HTTP method
     */
    String getAwsHttpMethod() {
        operation.awsHttpMethod
    }

    /**
     * @return the name of this Method
     */
    @Memoized
    String getName() {
        "${operation} ${resourcePath}"
    }

    /**
     * Delete this method.
     */
    void delete() {
        debug("Deleting Method '${name}'")
        apiGateway.deleteMethod(new DeleteMethodRequest(
                restApiId: apiId,
                resourceId: resourceId,
                httpMethod: awsHttpMethod
        ))
    }

    /**
     * Create a response for this method according to the specification.
     * @param response the response
     */
    private void createResponse(final ResponseSpecification response) {
        debug("Creating Method Response '${name}' => ${response.statusCode}")
        apiGateway.putMethodResponse(new PutMethodResponseRequest(
                restApiId: apiId,
                resourceId: resourceId,
                httpMethod: awsHttpMethod,
                statusCode: response.statusCode,
                responseParameters: response.awsResponseHeaders,
                responseModels: mapResponseModelsByContentType(response)
        ))
    }

    /**
     * Create the request integration for this method according to the specification.
     * @param integration the request integration
     */
    private void createRequestIntegration(final RequestIntegrationSpecification integration) {
        debug("Creating Request Integration for '${name}'")
        apiGateway.putIntegration(new PutIntegrationRequest(
                restApiId: apiId,
                resourceId: resourceId,
                httpMethod: awsHttpMethod,
                type: integration.type,
                uri: integration.uri,
                credentials: integration.credentials,
                integrationHttpMethod: integration.httpMethod.awsHttpMethod,
                requestParameters: integration.parameters,
                requestTemplates: integration.templates,
                cacheNamespace: integration.cacheNamespace,
                cacheKeyParameters: integration.cacheKeyParameters
        ))
    }

    /**
     * Create a response integration for this method according to the specification.
     * @param integration the response integration
     */
    private void createResponseIntegration(final ResponseIntegrationSpecification integration) {
        debug("Creating Response Integration /${integration.selectionPattern}/ for " +
                "'${name}' => ${integration.statusCode}")
        apiGateway.putIntegrationResponse(new PutIntegrationResponseRequest(
                restApiId: apiId,
                resourceId: resourceId,
                httpMethod: awsHttpMethod,
                statusCode: integration.statusCode,
                selectionPattern: integration.selectionPattern,
                responseParameters: integration.parameters,
                responseTemplates: integration.templates
        ))
    }

    private Map mapResponseModelsByContentType(final ResponseSpecification response) {
        response.models.collectEntries { contentType, model ->
            [ (contentType): api.getOrCreateModel(model).name ]
        }
    }
}
