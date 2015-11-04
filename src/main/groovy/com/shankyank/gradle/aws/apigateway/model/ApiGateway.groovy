package com.shankyank.gradle.aws.apigateway.model

import com.amazonaws.services.apigateway.model.GetRestApiRequest
import com.amazonaws.services.apigateway.model.GetRestApisRequest
import com.amazonaws.services.apigateway.model.RestApi
import groovy.util.logging.Slf4j

/**
 * Decorator for AmazonApiGateway that simplifies access to AWS resources.
 */
@Slf4j('logger')
class ApiGateway implements ApiGatewayDecorator {
    /**
     * @return the APIs registered in this gateway
     */
    List<Api> getRestApis() {
        collectPagedResults(new GetRestApisRequest(), apiGateway.&getRestApis).collect(this.&toApi)
    }

    /**
     * Find all APIs with a given name.
     * @param the name of the API
     * @return the set of APIs with the provided name
     */
    List<Api> findApisByName(final String name) {
        collectPagedResults(new GetRestApisRequest(), apiGateway.&getRestApis) {
            it.name == name
        }.collect(this.&toApi)
    }

    /**
     * Get the API with the given ID.
     * @param apiId the ID of the API
     * @return the target API
     */
    Optional<Api> getApiById(final String apiId) {
        logger.debug("Retrieving RestApi ${apiId}")
        findOptionalObject {
            apiGateway.getRestApi(new GetRestApiRequest().withRestApiId(apiId))?.with {
                toApi(new RestApi().
                        withId(id).
                        withName(name).
                        withDescription(description).
                        withCreatedDate(createdDate))
            }
        }
    }

    /**
     * Converts an ApiGateway result object to a decorated RestApi.
     * @param result the results from an ApiGateway representing a RestApi
     * @return a RestApi object
     */
    private Api toApi(final RestApi api) {
        new Api(apiGateway: apiGateway, restApi: api)
    }
}
