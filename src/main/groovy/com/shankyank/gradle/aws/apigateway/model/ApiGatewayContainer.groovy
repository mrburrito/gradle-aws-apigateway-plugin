package com.shankyank.gradle.aws.apigateway.model

import com.amazonaws.services.apigateway.AmazonApiGateway
import com.amazonaws.services.apigateway.model.NotFoundException
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j

/**
 * Base class for decorators around AWS API Gateway services.
 */
@PackageScope
@Slf4j('logger')
trait ApiGatewayContainer {
    /** The AmazonApiGateway client. */
    AmazonApiGateway apiGateway

    /**
     * Executes an API Gateway request that can trigger a NotFoundException,
     * returning an empty Optional instead of throwing the exception.
     * @param query the query to execute
     * @return the query result
     */
    Optional findOptionalObject(final Closure query) {
        try {
            Optional.ofNullable(query())
        } catch (NotFoundException ex) {
            logger.debug("Requested object not found: ${ex.message}")
            Optional.empty()
        }
    }


    /**
     * Executes a pageable call to the API Gateway service, iterating over
     * the pages and returning the items found in a single list.
     *
     * This method assumes `getPage` accepts a single argument of `baseRequest`
     * and returns a result object with the properties `position` and `items`. It also
     * assumes `baseRequest` has `withPosition()` method that configures paging for the
     * request. This is consistent with all API Gateway request/response models.
     *
     * @param baseRequest the base request object with all parameters other than paging details configured
     * @param getPage a Closure, typically a method pointer on an AmazonApiGateway client, that accepts
     *                a request and returns a paged response
     * @return the List of all items returned by the pageable request
     */
    List collectPagedResults(final def baseRequest, final Closure getPage) {
        // can't use default arguments in trait
        collectPagedResults(baseRequest, getPage, Closure.IDENTITY)
    }

    /**
     * Executes a pageable call to the API Gateway service, iterating over
     * the pages and returning the items found in a single list. The optional
     * `filter` closure should accept a single argument, the target item, returning
     * `true` if the item should be included in the list.
     *
     * This method assumes `getPage` accepts a single argument of `baseRequest`
     * and returns a result object with the properties `position` and `items`. It also
     * assumes `baseRequest` has `withPosition()` method that configures paging for the
     * request. This is consistent with all API Gateway request/response models.
     *
     * @param baseRequest the base request object with all parameters other than paging details configured
     * @param getPage a Closure, typically a method pointer on an AmazonApiGateway client, that accepts
     *                a request and returns a paged response
     * @param filter an optional filter, defaulting to the identity function, restricting the values returned
     * @return the List of all items returned by the pageable request
     */
    List collectPagedResults(final def baseRequest, final Closure getPage, final Closure filter) {
        logger.info("Collecting paged results with base request: ${baseRequest}")
        def logPage = { page ->
            if (logger.debugEnabled) {
                logger.debug("Next Page contains ${page?.items?.size() ?: 0} Results:")
                page?.items?.each { logger.debug("\t${it}") }
            }
        }
        def result
        def nextPage = {
            logger.debug("Next page starts at position: ${result?.position}")
            def page = !result || result.position ? getPage(baseRequest.withPosition(result?.position)) : null
            logPage(page)
            page
        }
        List results = []
        while ((result = nextPage())?.items) {
            def filtered = result.items.findAll(filter)
            logger.debug("Found ${filtered.size()} results matching filter")
            results += filtered
        }
        logger.info("Found ${results.size()} Results")
        if (logger.debugEnabled) {
            logger.debug("Collected Results:\n\t${results.join('\n\t')}")
        }
        results
    }
}
