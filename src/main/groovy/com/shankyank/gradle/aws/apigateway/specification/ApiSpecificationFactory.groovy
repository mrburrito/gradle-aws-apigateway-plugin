package com.shankyank.gradle.aws.apigateway.specification

import com.shankyank.gradle.aws.apigateway.specification.swagger.SwaggerApiSpecification
import groovy.util.logging.Slf4j
import io.swagger.parser.SwaggerParser
import org.gradle.api.GradleException

/**
 * This factory builds ApiSpecification wrappers based on the
 * contents of a specification file.
 */
@Singleton
@Slf4j
class ApiSpecificationFactory {
    @Slf4j
    static enum SpecificationType {
        SWAGGER {
            @Override
            protected Object parseSpecification(final File specFile) {
                new SwaggerParser().read(specFile.absolutePath)
            }

            @Override
            protected ApiSpecification createSpecification(final Object spec, final File specFile) {
                new SwaggerApiSpecification(spec, specFile)
            }

            @Override
            String toString() {
                'Swagger'
            }
        }

        /**
         * Parse the specification file.
         * @param specFile the specification file
         * @return a parsed specification
         */
        protected abstract Object parseSpecification(final File specFile)

        /**
         * Create an ApiSpecification wrapper for this type of specification.
         * @param spec the specification to wrap
         * @param specFile the specification file
         * @return an appropriate ApiSpecification
         */
        protected abstract ApiSpecification createSpecification(final Object specification, final File specFile)

        /**
         * Parse the specification file, returning a wrapped ApiSpecification.
         * @param specFile the specification to parse
         * @return the wrapped specification
         */
        ApiSpecification parseSpecificationFile(final File specFile) {
            log.info("Reading ${this} specification from ${specFile.absolutePath}")
            def spec = parseSpecification(specFile)
            if (spec) {
                createSpecification(spec, specFile).with {
                    if (log.debugEnabled) {
                        log.debug("Parsed ${this} specification '${name}' with ${resourceCount} resources " +
                                "and ${modelCount} models")
                    }
                    it
                }
            } else {
                throw new GradleException("Unable to parse ${this} specification from ${specFile.absolutePath}")
            }
        }

        /**
         * Determine the specification type of the provided file.
         * @param file the file
         * @return the identified SpecificationType
         */
        static SpecificationType forSpecificationFile(final File specFile) {
            SWAGGER
        }
    }

    /**
     * Create an ApiSpecification by reading from the provided specification file.
     * @param specFile the file containing the specification
     * @return the ApiSpecification loaded from the specFile
     */
    ApiSpecification createApiSpecification(final File specFile) {
        SpecificationType.forSpecificationFile(specFile).parseSpecificationFile(specFile)
    }
}
