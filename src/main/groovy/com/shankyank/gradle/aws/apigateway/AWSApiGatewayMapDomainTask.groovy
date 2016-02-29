package com.shankyank.gradle.aws.apigateway

import com.amazonaws.services.apigateway.model.GetDomainNameRequest
import com.shankyank.gradle.aws.apigateway.model.Api
import com.shankyank.gradle.aws.apigateway.model.CustomDomain
import org.gradle.api.tasks.TaskAction

/**
 * Gradle task to map a previously created custom domain to a
 * deployed API Gateway API/stage.
 */
class AWSApiGatewayMapDomainTask extends BaseAWSApiGatewayTask {
    /** The custom domain name. */
    String domainName

    /** The base path to map; may be null or empty to map the domain name directly. */
    String basePath

    /** The name of the target API. */
    String apiName

    /** The name of the target stage. */
    String stageName

    /**
     * Maps ${domainName}/${basePath} to ${API}/${stage}.
     */
    @TaskAction
    void mapDomainName() {
        customDomain.mapBasePathToStage(basePath, targetApi.apiId, stageName)
    }

    /**
     * @return the target API
     */
    protected Api getTargetApi() {
        findSingleApiWithName(apiName)
    }

    /**
     * @return the custom domain registered in API Gateway
     */
    protected CustomDomain getCustomDomain() {
        awsApiGateway.getDomainName(new GetDomainNameRequest(domainName: domainName)).with {
            new CustomDomain(
                    apiGateway: awsApiGateway,
                    domainName: it.domainName
            )
        }
    }
}
