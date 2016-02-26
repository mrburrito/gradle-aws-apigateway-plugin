package com.shankyank.gradle.aws.apigateway

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.AmazonWebServiceRequest
import com.amazonaws.retry.PredefinedRetryPolicies
import com.amazonaws.retry.RetryPolicy.RetryCondition
import groovy.util.logging.Slf4j

/**
 * An implementation of RetryPolicy.RetryCondition that augments
 * a provided RetryCondition, ensuring all Rate Limit errors from
 * API Gateway are retried.
 */
@Slf4j
class RateLimitRetryCondition implements RetryCondition, RateLimitResponder {

    private final RetryCondition retryCondition

    RateLimitRetryCondition(final RetryCondition delegate=PredefinedRetryPolicies.DEFAULT_RETRY_CONDITION) {
        retryCondition = delegate
    }

    @Override
    boolean shouldRetry(AmazonWebServiceRequest originalRequest, AmazonClientException exception, int retriesAttempted) {
        String logPrefix = "${originalRequest} Attempt ${retriesAttempted} (${exception}): "
        boolean wrappedRetry = wrappedConditionIndicatesRetry(originalRequest, exception, retriesAttempted)
        boolean rateLimitRetry = isRateLimitError(exception)
        boolean retry = wrappedRetry || rateLimitRetry
        log.debug("${logPrefix}: Wrapped Retry? ${wrappedRetry}, Rate Limit Retry? ${rateLimitRetry}, Should Retry? ${retry}")
        retry
    }

    /**
     * Determines if the decorated retry policy indicates this request should be retried.
     * @param request the request
     * @param exception the exception
     * @param retriesAttempted the number of retries already attempted
     * @return true if the decorated retry policy indicates this request should be retried
     */
    private boolean wrappedConditionIndicatesRetry(final AmazonWebServiceRequest request, final AmazonClientException exception,
                                                   final int retriesAttempted) {
        retryCondition.shouldRetry(request, exception, retriesAttempted)
    }
}
