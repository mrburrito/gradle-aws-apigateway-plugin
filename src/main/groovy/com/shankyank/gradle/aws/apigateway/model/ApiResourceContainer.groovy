package com.shankyank.gradle.aws.apigateway.model

/**
 * Models that contain an ApiResource.
 */
trait ApiResourceContainer extends ApiContainer {
    /** The ApiResource. */
    final ApiResource resource

    /**
     * @return the resource ID
     */
    String getResourceId() {
        resource.resourceId
    }

    /**
     * @return the resource path
     */
    String getResourcePath() {
        resource.path
    }
}
