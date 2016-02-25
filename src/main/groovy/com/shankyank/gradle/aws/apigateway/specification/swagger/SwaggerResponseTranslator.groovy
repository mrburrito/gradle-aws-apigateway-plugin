package com.shankyank.gradle.aws.apigateway.specification.swagger

import com.shankyank.gradle.aws.apigateway.model.ParameterLocation
import com.shankyank.gradle.aws.apigateway.specification.ModelSpecification
import com.shankyank.gradle.aws.apigateway.specification.ParameterSpecification
import com.shankyank.gradle.aws.apigateway.specification.ResponseSpecification
import io.swagger.models.Response

/**
 * Translates an Operation Response to a ResponseSpecification.
 */
class SwaggerResponseTranslator {
    /** The 'default' status code marker. */
    private static final String DEFAULT_STATUS_MARKER = 'default'

    /** The default status code; replaces default marker. */
    private static final String DEFAULT_STATUS_CODE = '200'

    /** The Swagger API specification. */
    final SwaggerApiSpecification api

    /** The status code of this response in the Swagger specification. */
    final String statusCode

    /** The response to translate. */
    final Response response

    SwaggerResponseTranslator(final SwaggerApiSpecification api, final String statusCode, final Response response) {
        this.api = api
        this.statusCode = statusCode
        this.response = response
    }

    /**
     * @return the ResponseSpecification for this Response
     */
    ResponseSpecification getResponseSpecification() {
        new ResponseSpecification(
                statusCode: resolvedStatusCode,
                headers: headerSpecifications,
                models: responseModels
        )
    }

    /**
     * @return the resolved status code for this response
     */
    private String getResolvedStatusCode() {
        switch (statusCode) {
            case DEFAULT_STATUS_MARKER: return DEFAULT_STATUS_CODE
            case ~/\d{3}/: return statusCode
            default: throw new InvalidStatusCode(statusCode)
        }
    }

    /**
     * @return the ParameterSpecifications for the response headers
     */
    private List<ParameterSpecification> getHeaderSpecifications() {
        response.headers?.collect { name, property ->
            new ParameterSpecification(
                    location: ParameterLocation.HEADER,
                    name: name,
                    required: property.required
            )
        }
    }

    /**
     * @return the models for the response body, mapped by content type
     */
    private Map<String, ModelSpecification> getResponseModels() {
        response?.schema?.with {
            ModelSpecification model = api.resolveReferencedModel(it) ?:
                    api.createModelSpecification(it, description: response.description)
            model ? [ (api.schemaConverter.contentType): model ] : [:]
        }
    }

    /**
     * Generated when response status codes are not the default status code
     * placeholder or a three-digit HTTP status code.
     */
    static class InvalidStatusCode extends RuntimeException {
        InvalidStatusCode(final String statusCode) {
            super("Status code [${statusCode}] must be '${DEFAULT_STATUS_MARKER}' or a valid HTTP status code.")
        }
    }
}
