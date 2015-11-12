package com.shankyank.gradle.aws.apigateway.specification.swagger

import com.shankyank.gradle.aws.apigateway.specification.ApiSpecification
import com.shankyank.gradle.aws.apigateway.specification.ModelSpecification
import com.shankyank.gradle.aws.apigateway.specification.ResourceSpecification
import com.shankyank.gradle.aws.apigateway.specification.SchemaConverter
import com.wordnik.swagger.models.Swagger
import groovy.transform.Memoized
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j

/**
 * ApiSpecification wrapper for Swagger specifications.
 */
@Slf4j
class SwaggerApiSpecification implements ApiSpecification<Swagger> {
    /** The parsed specification. */
    final Swagger specification

    /** The input file. */
    final File specificationFile

    SwaggerApiSpecification(Swagger specification, File specificationFile) {
        this.specification = specification
        this.specificationFile = specificationFile
    }

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

    @Memoized
    Collection<ModelSpecification> getModels() {
        specification.definitions.collect { name, model ->
            createModelSpecification(model, name: name, description:  model.description)
        }
    }

    @Memoized
    ResourceSpecification getRootResource() {
        new SwaggerResourceSpecificationBuilder(this).rootResource
    }

    /**
     * Creates a ModelSpecification for the provided model, generating a name from
     * the description if no name is provided.
     * @param name the name of the model
     * @param description the description of the model
     * @param model the model
     */
    @PackageScope
    ModelSpecification createModelSpecification(final Object model, final String name=null, final String description=null) {
        log.debug("Creating Model Specification [${name}] -- '${description}': ${model}")
        String modelName = name ?: generateModelName(description)
        new ModelSpecification(
                name: modelName,
                description: description,
                schema: schemaConverter.generateSchemaForModel(modelName, model),
                contentType: schemaConverter.contentType
        )
    }

    /**
     * Creates a ModelSpecification for the provided model, generating a name from
     * the description if no name is provided.
     * @param name the name of the model
     * @param description the description of the model
     * @param model the model
     */
    @PackageScope
    ModelSpecification createModelSpecification(final Map<String, String> args, final Object model) {
        log.debug("Creating Model Specification for [ Name: ${args?.name}, Description: ${args?.description} ]: ${model}")
        createModelSpecification(model, args?.get('name'), args?.get('description'))
    }

    /**
     * Resolves the schema for a referenced model.
     * @param reference the model reference
     * @return the resolved schema
     */
    @PackageScope
    ModelSpecification resolveReferencedModel(final Object reference) {
        getReferenceTarget(reference)?.with { modelsByName[it] }
    }

    /**
     * @return the SchemaConverter
     */
    @Memoized
    @PackageScope
    SchemaConverter getSchemaConverter() {
        log.debug("Creating SchemaConverter for ${specification.definitions}")
        new SwaggerJsonSchemaConverter(specification.definitions)
    }

    /**
     * @return the defined Models in this specification, mapped by name
     */
    @Memoized
    private Map<String, ModelSpecification> getModelsByName() {
        models.collectEntries { model -> [ (model.name): model ] }
    }

    /**
     * Generates a model name from a provided description, creating a random name
     * if the description is empty.
     * @param description the description
     * @return the generated name
     */
    private String generateModelName(final String description=null) {
        sanitizeModelName(description) ?: "Model${UUID.randomUUID().toString()[0..<8]}"
    }

    /**
     * Cleans a generated model name, ensuring it contains only alpha-numeric characters.
     * @param name the name to clean
     * @return the cleaned name
     */
    private String sanitizeModelName(final String name) {
        name?.trim()?.replaceAll(/\W/, '')
    }

    /**
     * Gets the value of the target reference.
     * @param reference the reference
     * @return the reference target
     */
    private String getReferenceTarget(final Object reference) {
        reference?.hasProperty('simpleRef')?.getProperty(reference)
    }
}
