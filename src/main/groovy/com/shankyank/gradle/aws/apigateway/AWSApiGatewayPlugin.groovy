package com.shankyank.gradle.aws.apigateway

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin initialization.
 */
class AWSApiGatewayPlugin implements Plugin<Project> {
    @Override
    void apply(final Project project) {
        project.logger.info("Applying AWSApiGatewayPlugin")
        project.extensions.create(AWSApiGatewayPluginExtension.NAME, AWSApiGatewayPluginExtension, project)
        project.task('listApis', type: AWSApiGatewayListTask)
    }
}
