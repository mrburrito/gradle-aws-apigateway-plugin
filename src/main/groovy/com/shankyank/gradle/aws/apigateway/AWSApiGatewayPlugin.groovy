package com.shankyank.gradle.aws.apigateway

import jp.classmethod.aws.gradle.AwsPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin initialization.
 */
class AWSApiGatewayPlugin implements Plugin<Project> {
    @Override
    void apply(final Project project) {
        project.pluginManager.apply(AwsPlugin)
        project.extensions.create('apigateway', AWSApiGatewayPluginExtension, project)
    }
}
