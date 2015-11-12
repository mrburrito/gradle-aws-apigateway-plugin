package com.shankyank.gradle.aws.apigateway.specification.swagger

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.github.fge.jsonschema.core.exceptions.ProcessingException
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.shankyank.gradle.aws.apigateway.specification.SchemaConverter
import com.wordnik.swagger.util.Json
import groovy.transform.Memoized
import groovy.util.logging.Slf4j

/**
 * Translates Swagger models to flattened JsonSchema with all references included
 * as inline schema definitions.
 */
@Slf4j
class SwaggerJsonSchemaConverter implements SchemaConverter {
    /** The schema content type. */
    static final String CONTENT_TYPE = 'application/json'

    /** The Reference node key. */
    private static final String REFERENCE = '$ref'

    /** The inline schema root node. */
    private static final String SCHEMA_ROOT = 'definitions'

    /** The known models for dependency resolution. */
    private Map<String, ResolvedSchema> models

    /** The object mapper. */
    private ObjectMapper mapper

    SwaggerJsonSchemaConverter(Map<String, Object> models) {
        mapper = Json.mapper()
        this.models = models?.collectEntries { name, model ->
            if (log.debugEnabled) {
                log.debug("Creating ResolvedSchema for ${name} [ ${toPrettyJsonString(model)} ]")
            }
            [ (name): new ResolvedSchema(this, name, model) ]
        } ?: [:]
    }

    @Override
    String getContentType() {
        CONTENT_TYPE
    }

    @Override
    String getSchemaForNamedModel(final String name) {
        models[name]?.jsonSchema
    }

    @Override
    String generateSchemaForModel(final String name, final Object model) {
        new ResolvedSchema(this, name, model).jsonSchema
    }

    /**
     * Serialize an object as a JSON String.
     * @param object the serialization target
     * @return the serialized JSON
     */
    private String toJsonString(final Object object) {
        mapper.writeValueAsString(object)
    }

    /**
     * Serialize an object as a pretty-printed JSON String
     * for human readability.
     * @param object the serialization target
     * @return the serialized JSON
     */
    private String toPrettyJsonString(final Object object) {
        mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object)
    }

    /**
     * Reformat the serialized JSON string as a pretty-printed JSON string.
     * @param json the serialized JSON
     * @return pretty printed JSON
     */
    private String toPrettyJsonString(final String json) {
        mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readTree(json))
    }

    /**
     * Deserialize the input JSON string to a JsonNode.
     * @param json the serialized JSON
     * @return a JsonNode
     */
    private JsonNode toJsonNode(final String json) {
        mapper.readTree(json)
    }

    /**
     * Identity conversion for JsonNodes.
     * @param jsonNode the JsonNode
     * @return the input node
     */
    private JsonNode toJsonNode(final JsonNode jsonNode) {
        jsonNode
    }

    /**
     * Convert the input object to a JsonNode.
     * @param object the object to convert
     * @return a JsonNode representing the object
     */
    private JsonNode toJsonNode(final Object object) {
        mapper.valueToTree(object)
    }

    /**
     * Exception thrown when the value of a ${REFERENCE} node is
     * not a legal reference.
     */
    static class BadReference extends RuntimeException {
        BadReference(final String ref) {
            super("{ \"${REFERENCE}\": \"${ref}\" }")
        }
    }

    /**
     * Exception thrown when a generated JsonSchema is invalid.
     */
    static class InvalidSchema extends RuntimeException {
        InvalidSchema(final String message=null, final Throwable cause=null) {
            super(message, cause)
        }
    }

    private static class ResolvedSchema {
        /** The owner of this schema. */
        private final SwaggerJsonSchemaConverter schemaConverter

        /** The name of this schema. */
        final String name

        /** The root of the schema definition. */
        final ObjectNode jsonModel

        /** The names of the dependent schema for this schema. */
        final Set<String> dependencies

        ResolvedSchema(final SwaggerJsonSchemaConverter schemaConverter, final String name, final Object model) {
            this.schemaConverter = schemaConverter
            this.name = name
            jsonModel = schemaConverter.toJsonNode(model)
            Set deps = [] as Set
            findReferences(jsonModel).each { ref, parent ->
                String schemaName = getReferenceSchemaName(ref)
                deps << schemaName
                parent.set(REFERENCE, new TextNode(getInlineSchemaReference(schemaName)))
            }
            dependencies = deps.asImmutable()
        }

        /**
         * Generate a flattened JsonSchema, resolving all dependencies of this schema
         * and including them as inline schema definitions.
         * @return the flattened JsonSchema
         */
        @Memoized
        String getJsonSchema() {
            ObjectNode schemaRootNode = new ObjectNode(JsonNodeFactory.instance)
            resolvedDependencies.each { name, schema ->
                schemaRootNode.set(name, schema.jsonModel)
            }

            JsonNode flatSchema = jsonModel.deepCopy()
            if (resolvedDependencies) {
                flatSchema.set(SCHEMA_ROOT, schemaRootNode)
            }
            validateJsonSchema(flatSchema)
            if (log.debugEnabled) {
                log.debug("Generated JsonSchema for Model ${name}: ${schemaConverter.toPrettyJsonString(flatSchema)}")
            } else {
                log.info("Generated JsonSchema for Model ${name}: ${schemaConverter.toJsonString(flatSchema)}")
            }
            schemaConverter.toJsonString(flatSchema)
        }

        @Memoized
        private Map<String, ResolvedSchema> getResolvedDependencies() {
            dependencies.collectEntries { name ->
                schemaConverter.models[name]?.with { [ (name): it ] + it.resolvedDependencies }
            }
        }

        private Map<JsonNode, JsonNode> findReferences(final JsonNode node) {
            Map refs = [:]
            JsonNode ref = node.path(REFERENCE)
            if (!ref.missingNode) {
                refs[ref] = node
            }
            refs + node.collectEntries(this.&findReferences)
        }

        private String getReferenceSchemaName(final JsonNode refNode) {
            (refNode.textValue() =~ /.*\/([^\/]+)$/).with {
                if (it) {
                    it[0][1]
                } else {
                    throw new BadReference(refNode)
                }
            }
        }

        private String getInlineSchemaReference(final String schemaName) {
            "#/${SCHEMA_ROOT}/${schemaName}"
        }

        private void validateJsonSchema(final JsonNode root) {
            try {
                JsonSchemaFactory factory = JsonSchemaFactory.byDefault()
                ProcessingReport report = factory.getSyntaxValidator().validateSchema(root)
                if (!report.success) {
                    throw new InvalidSchema("Invalid JsonSchema for ${name}: ${report.collect{it.message}.join('; ')}")
                }
            } catch (ProcessingException ex) {
                throw new InvalidSchema("Unable to build JsonSchema for ${name}", ex)
            }
        }
    }
}
