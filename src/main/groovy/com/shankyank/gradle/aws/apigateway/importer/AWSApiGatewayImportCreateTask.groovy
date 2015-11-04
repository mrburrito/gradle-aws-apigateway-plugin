package com.shankyank.gradle.aws.apigateway.importer

import com.amazonaws.services.apigateway.model.RestApi
import org.gradle.api.tasks.TaskAction

/**
 * Imports an API specification into API Gateway, forcing creation of
 * a new API every time the task is run.
 */
class AWSApiGatewayImportCreateTask extends BaseAWSApiGatewayImporterTask {
    @TaskAction
    void importNewApi() {
        RestApi api = createApi()
    }
}
