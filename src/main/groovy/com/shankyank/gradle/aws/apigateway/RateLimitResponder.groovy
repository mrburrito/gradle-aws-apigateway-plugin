package com.shankyank.gradle.aws.apigateway

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException

/**
 * This trait can be assumed by classes that need to respond
 * when API Gateway returns a rate limit error.
 */
trait RateLimitResponder {
    /** The rate limit exceeded error code */
    static final int RATE_LIMIT_ERROR_CODE = 429

    /**
     * Determines if the provided exception is a Rate Limit Exceeded error.
     * @param exception the exception
     * @return true if the exception indicates the API Gateway rate limit was exceeded
     */
    boolean isRateLimitError(final AmazonClientException exception) {
        exception instanceof AmazonServiceException && exception.statusCode == RATE_LIMIT_ERROR_CODE
    }
}
