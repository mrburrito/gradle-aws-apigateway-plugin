package com.shankyank.gradle.aws.apigateway.model

import com.amazonaws.services.apigateway.model.CreateDeploymentRequest
import com.amazonaws.services.apigateway.model.GetStageRequest
import com.amazonaws.services.apigateway.model.GetStageResult
import com.amazonaws.services.apigateway.model.PatchOperation
import com.amazonaws.services.apigateway.model.UpdateStageRequest
import groovy.transform.Canonical
import groovy.transform.ToString
import groovy.util.logging.Slf4j

/**
 * Configuration options for an API Gateway deployment stage.
 */
@Canonical
@ToString(includeNames=true)
@Slf4j
class DeploymentStage implements ApiContainer {
    /** The stage name. */
    String name

    /** The stage description. */
    String stageDescription

    /** The deployment description. */
    String deploymentDescription

    /** The stage variables. */
    Map<String, String> stageVariables

    /** Should metrics publication be enabled? */
    boolean logMetrics

    /** Should logging be enabled? */
    CloudWatchLogLevel logLevel

    /**
     * Deploy the target API to this Stage.
     */
    void deploy() {
        apiGateway.createDeployment(new CreateDeploymentRequest(
                restApiId: api.apiId,
                stageName: name,
                stageDescription: stageDescription,
                description: deploymentDescription
        ))
        update()
    }

    /**
     * Update the settings of the previously deployed Stage
     * to match this configuration.
     */
    void update() {
        List patchOperations = [
                descriptionPatchOp,
                variablesPatchOps,
                metricsPatchOp,
                logLevelPatchOps
        ].flatten()
        log.info("Patching Stage ${name}:\n\t${patchOperations.join('\n\t')}")
        apiGateway.updateStage(new UpdateStageRequest(
                restApiId: api.apiId,
                stageName: name,
                patchOperations: patchOperations
        ))
    }

    protected DeploymentStage getExistingStage() {
        GetStageResult result = apiGateway.getStage(new GetStageRequest(
                restApiId: api.apiId,
                stageName: name
        ))
        def toLogLevel = { str -> str ? CloudWatchLogLevel.valueOf(str) : CloudWatchLogLevel.OFF }
        DeploymentStage stage = new DeploymentStage(
                api: api,
                apiGateway: apiGateway,
                name: result.stageName,
                stageDescription: result.description,
                stageVariables: result.variables,
                logMetrics: result.methodSettings['*/*']?.metricsEnabled ?: false,
                logLevel: toLogLevel(result.methodSettings['*/*']?.loggingLevel)
        )
        log.info("Found deployed stage: ${stage}")
        stage
    }

    protected boolean isDataTraceEnabled() {
        logLevel == CloudWatchLogLevel.INFO
    }

    private PatchOperation getDescriptionPatchOp() {
        new PatchOperation(op: 'replace', path: '/description', value: stageDescription)
    }

    private List getVariablesPatchOps() {
        Set keysToClear = existingStage.stageVariables?.keySet() ?: []
        keysToClear.removeAll(stageVariables.keySet())
        [
                keysToClear.collect { new PatchOperation(op: 'remove', path: "/variables/${it}") },
                stageVariables.collect { k, v -> new PatchOperation(op: 'replace', path: "/variables/${k}", value: v) }
        ].flatten()
    }

    private PatchOperation getMetricsPatchOp() {
        new PatchOperation(op: 'replace', path: '/*/*/metrics/enabled', value: logMetrics)
    }

    private List getLogLevelPatchOps() {
        [
                new PatchOperation(op: 'replace', path: '/*/*/logging/loglevel', value: logLevel),
                new PatchOperation(op: 'replace', path: '/*/*/logging/dataTrace', value: dataTraceEnabled)
        ]
    }
}
