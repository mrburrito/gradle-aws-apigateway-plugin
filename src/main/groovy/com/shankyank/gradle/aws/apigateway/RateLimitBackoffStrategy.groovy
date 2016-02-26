package com.shankyank.gradle.aws.apigateway

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.AmazonWebServiceRequest
import com.amazonaws.retry.RetryPolicy.BackoffStrategy
import com.amazonaws.retry.RetryUtils
import groovy.util.logging.Slf4j

/**
 * Modification of Amazon's default backoff strategy (PredefinedRetryPolicies.DEFAULT_BACKOFF_STRATEGY)
 * implementation to ensure 429 errors trigger exponential delays.
 */
@Slf4j
class RateLimitBackoffStrategy implements BackoffStrategy, RateLimitResponder {
    /** Base sleep time (milliseconds) for general exceptions. **/
    private static final int SCALE_FACTOR = 300

    /** Base sleep time (milliseconds) for throttling exceptions. **/
    private static final int THROTTLING_SCALE_FACTOR = 500

    private static final int THROTTLING_SCALE_FACTOR_RANDOM_RANGE = THROTTLING_SCALE_FACTOR / 4

    /** Maximum exponential back-off time before retrying a request */
    private static final int MAX_BACKOFF_IN_MILLISECONDS = 20 * 1000

    /**
     * Maximum number of retries before the max backoff will be hit. This is
     * calculated via log_2(MAX_BACKOFF_IN_MILLISECONDS / SCALE_FACTOR)
     * based on the code below.
     */
    private static final int MAX_RETRIES_BEFORE_MAX_BACKOFF = 6

    /** For generating a random scale factor **/
    private final Random random = new Random();

    /** {@inheritDoc} */
    @Override
    public final long delayBeforeNextRetry(final AmazonWebServiceRequest originalRequest, final AmazonClientException exception,
                                           final int retriesAttempted) {
        String logPrefix = "${originalRequest} Attempt ${retriesAttempted} (${exception}):"
        long delay
        switch (retriesAttempted) {
            case { it < 0 }:
                log.debug("${logPrefix} Retry Count < 0")
                delay = 0
                break
            case { it > MAX_RETRIES_BEFORE_MAX_BACKOFF }:
                log.debug("${logPrefix} Retry Count > ${MAX_RETRIES_BEFORE_MAX_BACKOFF}, Using max backoff delay.")
                delay = MAX_BACKOFF_IN_MILLISECONDS
                break
            default:
                int scaleFactor = includeJitterInBackoff(exception) ? jitterScaleFactor : SCALE_FACTOR
                delay = Math.min((1L << retriesAttempted) * scaleFactor, MAX_BACKOFF_IN_MILLISECONDS)
                break
        }
        log.debug("${logPrefix} Delay: ${delay} ms")
        delay
    }

    /**
     * @return a randomized scale factor to introduce jitter to the backoff delay
     */
    private int getJitterScaleFactor() {
        THROTTLING_SCALE_FACTOR + random.nextInt(THROTTLING_SCALE_FACTOR_RANDOM_RANGE)
    }

    /**
     * Indicates whether the delay scaling should include random jitter.
     * @param exception the error
     * @return true if jitter should be applied to the scale factor
     */
    private boolean includeJitterInBackoff(final AmazonClientException exception) {
        isRateLimitError(exception) || (exception instanceof AmazonServiceException && RetryUtils.isThrottlingException(exception))
    }
}
