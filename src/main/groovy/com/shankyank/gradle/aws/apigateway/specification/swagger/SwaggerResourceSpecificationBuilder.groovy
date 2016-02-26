package com.shankyank.gradle.aws.apigateway.specification.swagger

import com.shankyank.gradle.aws.apigateway.model.HttpMethod
import com.shankyank.gradle.aws.apigateway.specification.MethodSpecification
import com.shankyank.gradle.aws.apigateway.specification.ResourceSpecification
import groovy.transform.Memoized
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import io.swagger.models.Operation
import io.swagger.models.Path
import io.swagger.models.Swagger
import java.util.regex.Matcher

/**
 * Builds a resource specification tree from a Swagger specifciation.
 */
@PackageScope
@Slf4j
class SwaggerResourceSpecificationBuilder {
    /** The API Gateway Stage Vendor Extension. */
    static final String API_GATEWAY_STAGE_EXTENSION = 'x-amazon-apigateway-stage'
    /** The resource path separator. */
    static final String RESOURCE_PATH_SEPARATOR = '/'

    /** The Swagger API specification. */
    final SwaggerApiSpecification api

    /** The root of the resource tree. */
    final ResourceSpecification rootResource

    SwaggerResourceSpecificationBuilder(final SwaggerApiSpecification api) {
        this.api = api
        rootResource = new ResourceSpecification(name: '')
        populateResourceTree()
    }

    /**
     * @return the Swagger specification
     */
    private Swagger getSwagger() {
        api.specification
    }

    /**
     * Parses the resources defined in the Swagger specification, translating
     * them into ResourceSpecifications below the defined root resource.
     */
    private void populateResourceTree() {
        swagger.paths.each { pathName, path ->
            String fullPathName = getFullResourcePath(pathName)
            rootResource.ensureAllResourcesOnPathExist(fullPathName)
            addMethodsToResource(rootResource[fullPathName], path)
        }
    }

    /**
     * @return the portion of the base path representing the API Gateway stage, if it is configured
     */
    @Memoized
    private String getApiGatewayStage() {
        swagger.vendorExtensions?."${API_GATEWAY_STAGE_EXTENSION}"?.trim() ?: ''
    }

    /**
     * @return the API base path, with the API Gateway Stage stripped
     */
    @Memoized
    private String getBasePath() {
        String path = swagger.basePath
        if (apiGatewayStage) {
            Matcher m = path =~ /^\/?${apiGatewayStage}(\/.*)?$/
            if (m.matches()) {
                path = m[0][1] ?: ''
            } else {
                log.info("basePath '${swagger.basePath}' does not begin with ${API_GATEWAY_STAGE_EXTENSION} '${apiGatewayStage}'")
            }
        }
        path
    }

    /**
     * Get the full path to the given resource.
     * @param resourcePath the path to the target resource
     * @return the full path, including basePath, to the resource
     */
    private String getFullResourcePath(final String resourcePath) {
        cleanPathSeparators("/${basePath}/${resourcePath}")
    }

    /**
     * Removes duplicate path separators and all separators at the end of the path.
     * @param path the input path
     * @return the cleaned path
     */
    private String cleanPathSeparators(final String path) {
        path.replaceAll(/\/+/, '/').replaceAll(/\/$/, '')
    }

    /**
     * Get the parts of a path as a list of path components.
     * @param path the path
     * @return the list of path parts
     */
    private List<String> getPathParts(final String path) {
        path?.split(RESOURCE_PATH_SEPARATOR) ?: []
    }

    /**
     * Constructs a partial, relative path to the requested path component.
     * @param pathParts the path parts
     * @param targetIndex the index of the target path part
     * @return the path to the target resource
     */
    private String buildPartialPath(final List pathParts, final int targetIndex) {
        pathParts[0..<targetIndex].join(RESOURCE_PATH_SEPARATOR)
    }

    /**
     * Indicates the existence of the resource at the given path.
     * @param path the path to the target resource, relative to the root resource
     * @return true if the resource exists
     */
    private boolean resourceDoesNotExist(final String path) {
        ResourceSpecification spec = rootResource[path]
        if (spec) {
            log.debug("Found Resource[{$spec.path}] for path '${path}'")
            false
        } else {
            log.debug("Resource not found at path '${path}'")
            true
        }
    }

    /**
     * Creates an empty ResourceSpecification within the parent at the given path.
     * @param parentPath the path to the parent resource
     * @param name the name of the new resource
     */
    private void createEmptyResource(final String parentPath, final String name) {
        rootResource[parentPath].addChild(new ResourceSpecification(name: name))
    }

    /**
     * Translates the Operations defined for a Swagger path to MethodSpecifications
     * on the target ResourceSpecification.
     * @param resource the resource specification
     * @param path the Swagger path
     */
    private void addMethodsToResource(final ResourceSpecification resource, final Path path) {
        HttpMethod.values().each { httpMethod ->
            getOperationForMethod(path, httpMethod)?.with {
                resource[httpMethod] = createMethodSpecificationFromOperation(httpMethod, it)
            }
        }
    }

    /**
     * Get the Operation on the Swagger Path for the requested HTTP method.
     * @param path the Swagger path
     * @param httpMethod the HTTP method
     * @return the Operation for the specified method
     */
    private Operation getOperationForMethod(final Path path, final HttpMethod httpMethod) {
        path?."${httpMethod.toString().toLowerCase()}"
    }

    /**
     * Translates a Swagger Operation to a MethodSpecification.
     * @param httpMethod the HTTP method of the operation
     * @param operation the Swagger operation
     * @return the translated MethodSpecification
     */
    private MethodSpecification createMethodSpecificationFromOperation(final HttpMethod httpMethod, final Operation operation) {
        new SwaggerOperationTranslator(api, httpMethod, operation).methodSpecification
    }
}
