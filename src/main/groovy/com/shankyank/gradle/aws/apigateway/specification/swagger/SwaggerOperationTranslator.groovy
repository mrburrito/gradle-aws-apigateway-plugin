package com.shankyank.gradle.aws.apigateway.specification.swagger

import com.amazonaws.services.apigateway.model.IntegrationType
import com.fasterxml.jackson.databind.ObjectMapper
import com.shankyank.gradle.aws.apigateway.model.HttpMethod
import com.shankyank.gradle.aws.apigateway.model.ParameterLocation
import com.shankyank.gradle.aws.apigateway.specification.MethodSpecification
import com.shankyank.gradle.aws.apigateway.specification.ModelSpecification
import com.shankyank.gradle.aws.apigateway.specification.ParameterSpecification
import com.shankyank.gradle.aws.apigateway.specification.RequestIntegrationSpecification
import com.shankyank.gradle.aws.apigateway.specification.ResponseIntegrationSpecification
import com.shankyank.gradle.aws.apigateway.specification.ResponseSpecification
import groovy.transform.Memoized
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import io.swagger.models.Operation
import io.swagger.models.Swagger
import io.swagger.models.parameters.BodyParameter
import io.swagger.models.parameters.Parameter

/**
 * Assists in the translation of an Operation to a MethodSpecification.
 */
@PackageScope
@Slf4j
class SwaggerOperationTranslator {
    /** ObjectMapper for JSON->Map conversion */
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper()

    /** The authorization extension key. */
    private static final String AUTH_EXTENSION = 'x-amazon-apigateway-auth'

    /** The API Key security definition type. */
    private static final String API_KEY_SECURITY_TYPE = 'apiKey'

    /** The integration extension key. */
    private static final String INTEGRATION_EXTENSION = 'x-amazon-apigateway-integration'

    /** The response integration extensions key. */
    private static final String RESPONSE_INTEGRATION_EXTENSIONS = 'responses'

    /** The default selection pattern marker. */
    private static final String DEFAULT_SELECTION_PATTERN_MARKER = 'default'

    /** The default selection pattern. */
    private static final String DEFAULT_SELECTION_PATTERN = null

    /** The Swagger API specification. */
    final SwaggerApiSpecification api

    /** The HTTP method for this operation. */
    final HttpMethod httpMethod

    /** The operation to translate. */
    final Operation operation

    SwaggerOperationTranslator(final SwaggerApiSpecification api, final HttpMethod httpMethod, final Operation operation) {
        this.api = api
        this.httpMethod = httpMethod
        this.operation = operation
    }

    /**
     * @return a fully constructed MethodSpecification for the configured operation
     */
    MethodSpecification getMethodSpecification() {
        new MethodSpecification(
                httpMethod: httpMethod,
                authorizationType: authorizationType,
                apiKeyRequired: apiKeyRequired,
                bodyModels: requestBodyModelSpecifications,
                parameters: parameterSpecifications,
                responses: responseSpecifications,
                requestIntegration: requestIntegrationSpecification,
                responseIntegrations: responseIntegrationSpecifications
        )
    }

    /**
     * @return the authorization type for this Operation
     */
    private String getAuthorizationType() {
        authorizationExtension['type']?.toUpperCase() ?: MethodSpecification.DEFAULT_AUTHORIZATION_TYPE
    }

    /**
     * @return true if an API Key is required for this Operation
     */
    private boolean isApiKeyRequired() {
        swaggerApiKeySecurityDefinitionName &&
                (operationSecuritySpecifiesApiKey() || swaggerSecuritySpecifiesApiKey())
    }

    /**
     * @return the model specifications for the request body, mapped by content type
     */
    private Map<String, ModelSpecification> getRequestBodyModelSpecifications() {
        bodyParameters.collectEntries { param ->
            [ (api.schemaConverter.contentType): getOrCreateModelSpecificationForParameter(param) ]
        }
    }

    /**
     * @return the parameter specifications for this Operation
     */
    private List<ParameterSpecification> getParameterSpecifications() {
        parameters.collect { param ->
            new ParameterSpecification(
                    location: getParameterLocation(param),
                    name: param.name,
                    required: param.required
            )
        }
    }

    /**
     * @return the ResponseSpecifications for this Operation
     */
    private List<ResponseSpecification> getResponseSpecifications() {
        operation.responses.collect { statusCode, response ->
            new SwaggerResponseTranslator(api, statusCode, response).responseSpecification
        }
    }

    /**
     * @return the request integration specification for this Operation
     */
    private RequestIntegrationSpecification getRequestIntegrationSpecification() {
        log.debug("Building RequestIntegrationSpecification from:\n${integrationExtensions.collect {k,v->"${k}: ${v}"}.join('\n')}")
        def myLog = log
        integrationExtensions?.with {
            def strVal = { key -> myLog.debug("Key: ${key}, it[key]: ${it[key]}, it: ${it}"); it[key] ?: '' }
            def mapVal = { key -> it[key] ?: [:] }
            def listVal = { key -> it[key] ?: [] }
            new RequestIntegrationSpecification(
                    type: toIntegrationType(strVal('type')),
                    uri: strVal('uri'),
                    credentials: strVal('credentials'),
                    httpMethod: toHttpMethod(strVal('httpMethod')),
                    parameters: mapVal('requestParameters'),
                    templates: mapVal('requestTemplates'),
                    cacheNamespace: strVal('cacheNamespace'),
                    cacheKeyParameters: listVal('cacheKeyParameters')
            )
        } ?: null
    }

    /**
     * @return the response integration specifications for this Operation
     */
    private List<ResponseIntegrationSpecification> getResponseIntegrationSpecifications() {
        responseIntegrationExtensions?.collect { selectionPattern, response ->
            def mapVal = { key -> response[key] ?: [:] }
            new ResponseIntegrationSpecification(
                    selectionPattern: resolveSelectionPattern(selectionPattern),
                    statusCode: response['statusCode'],
                    parameters: mapVal('responseParameters'),
                    templates: mapVal('responseTemplates')
            )
        } ?: null
    }

    /**
     * @return the Swagger specification
     */
    private Swagger getSwagger() {
        api.specification
    }

    /**
     * @return the defined vendor extensions for this Operation
     */
    private Map getVendorExtensions() {
        def ext = operation?.vendorExtensions
        ext != null ? JSON_MAPPER.convertValue(ext, Map) : [:]
    }

    /**
     * @return the authorization extension for this Operation
     */
    private Map getAuthorizationExtension() {
        vendorExtensions[AUTH_EXTENSION] ?: [:]
    }

    /**
     * @return the specification API Key security definitions
     */
    @Memoized
    private String getSwaggerApiKeySecurityDefinitionName() {
        swagger.securityDefinitions?.find { name, schemeDefinition ->
            schemeDefinition.type == API_KEY_SECURITY_TYPE
        }?.key
    }

    /**
     * @return true if the Operation security defines the need for an API Key
     */
    private boolean operationSecuritySpecifiesApiKey() {
        operation.security.find { it.containsKey(swaggerApiKeySecurityDefinitionName) }
    }

    /**
     * @return true if the Swagger specification defines an API Key requirement for this operation
     */
    private boolean swaggerSecuritySpecifiesApiKey() {
        swagger.securityRequirement?.find { it.name == swaggerApiKeySecurityDefinitionName }
    }

    /**
     * @return the body parameters for the operation
     */
    private List<BodyParameter> getBodyParameters() {
        operation.parameters?.findAll { it instanceof BodyParameter } ?: []
    }

    /**
     * @return the non-body parameters for the operation
     */
    private List<Parameter> getParameters() {
        operation.parameters?.findAll { !(it instanceof BodyParameter) } ?: []
    }

    /**
     * Retrieves the ModelSpecification for the given body parameter, resolving
     * a model reference or creating a new ModelSpecification if an inline
     * schema is provided.
     * @param parameter the body parameter
     * @return the ModelSpecification for the body parameter
     */
    private ModelSpecification getOrCreateModelSpecificationForParameter(final BodyParameter parameter) {
        api.resolveReferencedModel(parameter.schema) ?: createModelSpecification(parameter)
    }

    /**
     * Creates a ModelSpecification for the inline schema found in a body parameter.
     * @param parameter the body parameter
     * @return the generated ModelSpecification
     */
    private ModelSpecification createModelSpecification(final BodyParameter parameter) {
        api.createModelSpecification(parameter.schema, parameter.description)
    }

    /**
     * Get the location of the given Parameter.
     * @param parameter the parameter
     * @return the location of the parameter
     */
    private ParameterLocation getParameterLocation(final Parameter parameter) {
        try {
            ParameterLocation.valueOf(parameter.in?.toUpperCase())
        } catch (ex) {
            throw new UnsupportedParameterLocation(parameter, ex)
        }
    }

    /**
     * @return the integration extensions defined for this Operation
     */
    private Map getIntegrationExtensions() {
        vendorExtensions[INTEGRATION_EXTENSION] ?: [:]
    }

    /**
     * @return the response integration extensions defined for this Operation
     */
    private Map getResponseIntegrationExtensions() {
        integrationExtensions[RESPONSE_INTEGRATION_EXTENSIONS] ?: [:]
    }

    /**
     * Convert a string value to an IntegrationType.
     * @param type the type
     * @return the IntegrationType
     */
    private IntegrationType toIntegrationType(final String type) {
        try {
            IntegrationType.valueOf(type?.toUpperCase())
        } catch (ex) {
            throw new UnknownIntegrationType(type, ex)
        }
    }

    /**
     * Convert a string value to an HttpMethod.
     * @param method the method
     * @return the HttpMethod
     */
    private HttpMethod toHttpMethod(final String method) {
        try {
            HttpMethod.valueOf(method?.toUpperCase())
        } catch (ex) {
            throw new UnknownHttpMethod(method, ex)
        }
    }

    /**
     * Resolve the provided response selection pattern.
     * @param pattern the selection pattern
     * @return the resolved selection pattern
     */
    private String resolveSelectionPattern(final String pattern) {
        pattern == DEFAULT_SELECTION_PATTERN_MARKER ? DEFAULT_SELECTION_PATTERN : pattern
    }

    /**
     * Thrown when an unrecognized parameter location is encountered.
     */
    static class UnsupportedParameterLocation extends RuntimeException {
        UnsupportedParameterLocation(final Parameter param, final Throwable cause=null) {
            super("Unsupported location '${param?.in}' for parameter: ${param?.name}", cause)
        }
    }

    /**
     * Thrown when an unrecognized IntegrationType is encountered.
     */
    static class UnknownIntegrationType extends RuntimeException {
        UnknownIntegrationType(final String typeStr, final Throwable cause=null) {
            super("Unknown IntegrationType '${typeStr}'. Must be one of ${IntegrationType.values()}", cause)
        }
    }

    /**
     * Thrown when an unrecognized HttpMethod is encountered.
     */
    static class UnknownHttpMethod extends RuntimeException {
        UnknownHttpMethod(final String methodStr, final Throwable cause=null) {
            super("Unknown HttpMethod '${methodStr}'. Must be one of ${HttpMethod.values()}", cause)
        }
    }
}
