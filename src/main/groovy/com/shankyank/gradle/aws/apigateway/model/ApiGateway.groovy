package com.shankyank.gradle.aws.apigateway.model

import com.amazonaws.services.apigateway.AmazonApiGateway
import com.amazonaws.services.apigateway.model.CreateRestApiRequest
import com.amazonaws.services.apigateway.model.CreateRestApiResult
import com.amazonaws.services.apigateway.model.GetRestApiRequest
import com.amazonaws.services.apigateway.model.GetRestApisRequest
import com.amazonaws.services.apigateway.model.RestApi
import com.shankyank.gradle.aws.apigateway.specification.ApiSpecification
import groovy.util.logging.Slf4j

/**
 * Decorator for AmazonApiGateway that simplifies access to AWS resources.
 */
@Slf4j('logger')
class ApiGateway implements ApiGatewayContainer {
    ApiGateway(AmazonApiGateway apiGateway) {
        this.apiGateway = apiGateway
    }

    /**
     * @return the APIs registered in this gateway
     */
    List<Api> getRestApis() {
        collectPagedResults(new GetRestApisRequest(), apiGateway.&getRestApis).collect(this.&wrapApi)
    }

    /**
     * Find all APIs with a given name.
     * @param the name of the API
     * @return the set of APIs with the provided name
     */
    List<Api> findApisByName(final String name) {
        collectPagedResults(new GetRestApisRequest(), apiGateway.&getRestApis) {
            it.name == name
        }.collect(this.&wrapApi)
    }

    /**
     * Get the API with the given ID.
     * @param apiId the ID of the API
     * @return the target API
     */
    Optional<Api> getApiById(final String apiId) {
        logger.debug("Retrieving RestApi ${apiId}")
        findOptionalObject {
            wrapApi(apiGateway.getRestApi(new GetRestApiRequest(restApiId: apiId)))
        }
    }

    /**
     * Create an API with the given name and description.
     * @param name the name of the API
     * @param description the API description
     * @return the created API
     */
    Api createApi(final String name, final String description) {
        logger.debug("Creating RestApi: '${name}'")
        Api api = wrapApi(apiGateway.createRestApi(new CreateRestApiRequest(
                name: name,
                description: description
        )))
        logger.debug("Created ${api}")
        api
    }

    /**
     * Create and populate a new API based on the provided specification.
     * @param specification the specification
     * @return the created API
     */
    Api createApi(final ApiSpecification specification) {
        Api api = createApi(specification.name, specification.description)
        api.refreshApi(specification)
        api
    }

    private Api wrapApi(final RestApi api) {
        api?.with { new Api(apiGateway, it) }
    }

    private Api wrapApi(final def apiResult) {
        wrapApi(apiResult?.with {
            new RestApi(
                    id: id,
                    name: name,
                    description: description,
                    createdDate: createdDate
            )
        })
    }
}
