package com.shankyank.gradle.aws.apigateway.specification
/**
 * A container for a REST API specification declared in a
 * format such as Swagger or RAML.
 */
interface ApiSpecification<T> {
    /**
     * @return the name of the API
     */
    String getName()

    /**
     * @return a description of the API
     */
    String getDescription()

    /**
     * @return the models defined by this specification
     */
    Collection<SpecificationModel> getModels()

    /**
     * @return the representation of the API
     */
    T getSpecification()

    /**
     * @return the number of models found in the specification
     */
    int getModelCount()

    /**
     * @return the number of resources found in the specification
     */
    int getResourceCount()

    /**
     * @return the root resource defined by this specification
     */
    SpecificationResource getRootResource()
}
