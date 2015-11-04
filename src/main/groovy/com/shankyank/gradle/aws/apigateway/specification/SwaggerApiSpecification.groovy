package com.shankyank.gradle.aws.apigateway.specification

import com.wordnik.swagger.models.Swagger
import groovy.transform.Canonical

/**
 * ApiSpecification wrapper for Swagger specifications.
 */
@Canonical
class SwaggerApiSpecification implements ApiSpecification<Swagger> {
    /** The parsed specification. */
    Swagger specification

    /** The input file. */
    File specificationFile

    @Override
    String getName() {
        specification.info?.title ?: specificationFile.name
    }

    @Override
    String getDescription() {
        specification.info?.description ?: specificationFile.name
    }

    @Override
    int getModelCount() {
        specification.definitions.size()
    }

    @Override
    int getResourceCount() {
        specification.paths.size()
    }
}
