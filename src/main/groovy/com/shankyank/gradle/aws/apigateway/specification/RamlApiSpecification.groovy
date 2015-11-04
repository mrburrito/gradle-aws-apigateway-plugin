package com.shankyank.gradle.aws.apigateway.specification

import groovy.transform.Canonical
import org.raml.model.Raml
import org.raml.model.Resource

/**
 * ApiSpecification wrapping a RAML specification.
 */
@Canonical
class RamlApiSpecification implements ApiSpecification<Raml> {
    /** The wrapped specification. */
    Raml specification

    /** The specification file. */
    File specificationFile

    @Override
    String getName() {
        specification?.title ?: specificationFile.name
    }

    @Override
    int getModelCount() {
        specification.schemas.size()
    }

    @Override
    int getResourceCount() {
        def toCount = { Resource resource ->
            resource.resources.values().collect(toCount).sum(1)
        }
        specification.resources.values().collect(toCount).sum(0)
    }
}
