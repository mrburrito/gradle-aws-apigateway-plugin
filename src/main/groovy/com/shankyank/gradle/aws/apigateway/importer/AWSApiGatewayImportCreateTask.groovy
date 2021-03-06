package com.shankyank.gradle.aws.apigateway.importer

import org.gradle.api.tasks.TaskAction

/**
 * Imports an API specification into API Gateway, forcing creation of
 * a new API every time the task is run.
 */
class AWSApiGatewayImportCreateTask extends BaseAWSApiGatewayImporterTask {
    AWSApiGatewayImportCreateTask() {
        super()
        description = "Imports a ${supportedSpecifications} specification into a new API Gateway REST API."
    }

    @TaskAction
    void importNewApi() {
        createApi()
    }
}
