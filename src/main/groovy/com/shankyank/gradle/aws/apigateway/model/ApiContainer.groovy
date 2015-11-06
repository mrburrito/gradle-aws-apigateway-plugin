package com.shankyank.gradle.aws.apigateway.model

import groovy.transform.PackageScope
import groovy.util.logging.Slf4j

/**
 * Decorators that contain an Api.
 */
@PackageScope
@Slf4j('logger')
trait ApiContainer extends ApiGatewayContainer {
    /** The Api. */
    final Api api

    /**
     * @return the API id
     */
    String getApiId() {
        api.apiId
    }

    /**
     * @return the API name
     */
    String getApiName() {
        api.apiName
    }

    /**
     * Log a trace message for the contained API.
     * @param message the message
     */
    @PackageScope
    void trace(final CharSequence message) {
        // can't use default parameters in trait
        trace(message, null)
    }
    
    /**
     * Log a trace message for the contained API.
     * @param message the message
     * @param error the error
     */
    @PackageScope
    void trace(final CharSequence message, final Throwable error) {
        logger.trace(api.tagLogMessageWithApi(message), error)
    }

    /**
     * Log a debug message for the contained API.
     * @param message the message
     */
    @PackageScope
    void debug(final CharSequence message) {
        // can't use default parameters in trait
        debug(message, null)
    }

    /**
     * Log a debug message for the contained API.
     * @param message the message
     * @param error the error
     */
    @PackageScope
    void debug(final CharSequence message, final Throwable error) {
        logger.debug(api.tagLogMessageWithApi(message), error)
    }

    /**
     * Log a info message for the contained API.
     * @param message the message
     */
    @PackageScope
    void info(final CharSequence message) {
        // can't use default parameters in trait
        info(message, null)
    }

    /**
     * Log a info message for the contained API.
     * @param message the message
     * @param error the error
     */
    @PackageScope
    void info(final CharSequence message, final Throwable error) {
        logger.info(api.tagLogMessageWithApi(message), error)
    }

    /**
     * Log a warn message for the contained API.
     * @param message the message
     */
    @PackageScope
    void warn(final CharSequence message) {
        // can't use default parameters in trait
        warn(message, null)
    }

    /**
     * Log a warn message for the contained API.
     * @param message the message
     * @param error the error
     */
    @PackageScope
    void warn(final CharSequence message, final Throwable error) {
        logger.warn(api.tagLogMessageWithApi(message), error)
    }

    /**
     * Log a error message for the contained API.
     * @param message the message
     */
    @PackageScope
    void error(final CharSequence message) {
        // can't use default parameters in trait
        error(message, null)
    }

    /**
     * Log a error message for the contained API.
     * @param message the message
     * @param error the error
     */
    @PackageScope
    void error(final CharSequence message, final Throwable error) {
        logger.error(api.tagLogMessageWithApi(message), error)
    }
}
