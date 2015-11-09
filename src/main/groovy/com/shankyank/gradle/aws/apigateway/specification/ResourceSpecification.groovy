package com.shankyank.gradle.aws.apigateway.specification

import com.shankyank.gradle.aws.apigateway.model.HttpMethod
import groovy.transform.Canonical
import groovy.transform.Memoized

/**
 * A resource defined by the specification. Each resource is defined
 * by a path and represents a single path element. Resources may or
 * may not have operations and child resources associated with them.
 */
@Canonical
class ResourceSpecification implements Comparable<ResourceSpecification> {
    /** The parent of this resource. */
    ResourceSpecification parent

    /** The name of this resource. */
    String name

    /** The operations on this resource. */
    Map<HttpMethod, MethodSpecification> operations = new EnumMap(HttpMethod)

    /** The children of this resource. */
    private Map<String, ResourceSpecification> children = [:]

    /**
     * @return the children of this resource
     */
    List<ResourceSpecification> getChildren() {
        [] + children.values()
    }

    /**
     * Add a child resource.
     * @param resource the child resource
     */
    void addChild(final ResourceSpecification resource) {
        resource.parent = this
        children[resource.name] = resource
    }

    /**
     * Add a child resource using the << operator.
     * @param resource the child resource
     * @return this
     */
    ResourceSpecification leftShift(final ResourceSpecification resource) {
        addChild(resource)
        this
    }

    /**
     * Get the child resource at the specified path below this resource. Paths
     * should be specified as '/'-separated relative paths to this resource and
     * leading '/' characters will be stripped before evaluation. A null or empty
     * path can be used to refer to this resource.
     * @param path the path to the child resource
     * @return the resource at the given path
     */
    ResourceSpecification findResourceAtPath(final String path) {
        findDescendantResource(getRelativePathParts(path))
    }

    /**
     * Get the child resource at the specified path below this resource using the
     * [] operator. Paths should be specified as '/'-separated relative paths to
     * this resource and leading '/' characters will be stripped before evaluation.
     * A null or empty path can be used to refer to this resource.
     * @param path the path to the child resource
     * @return the resource at the given path
     */
    ResourceSpecification getAt(final String path) {
        findResourceAtPath(path)
    }

    /**
     * Configure a method for this resource.
     * @param httpMethod the HTTP method
     * @param method the method
     */
    void putMethod(final HttpMethod httpMethod, final MethodSpecification method) {
        operations[httpMethod] = method
    }

    /**
     * Configure a method for this resource using the [] operator.
     * @param httpMethod the HTTP method
     * @param method the method
     */
    void putAt(final HttpMethod httpMethod, final MethodSpecification method) {
        putMethod(httpMethod, method)
    }

    /**
     * Get the method for the specified HTTP method using the [] operator.
     * @param httpMethod the HTTP method
     * @return the method specification
     */
    MethodSpecification getAt(final HttpMethod httpMethod) {
        operations[httpMethod]
    }

    /**
     * @return the path to this resource's parent, separated by '/'
     */
    @Memoized
    String getParentPath() {
        parent?.path ?: ''
    }

    /**
     * @return the full path to this resource, separated by '/'
     */
    @Memoized
    String getPath() {
        "${parentPath}/${name}"
    }

    /**
     * @return a depth-first search of the resource tree rooted at this resource
     */
    @Memoized
    SortedSet<ResourceSpecification> getFlattenedResourceTree() {
        new TreeSet(this) + children?.collectMany { it.flattenedResourceTree }
    }

    @Override
    int compareTo(ResourceSpecification other) {
        parent <=> other.parent ?: name <=> other.name
    }

    /**
     * Cleans a path, removing all leading and trailing '/' characters
     * and splits it into components so descendants can be recursively
     * identified.
     * @param path the path to clean
     * @return the names of the descendants of this resource in the order
     *         they appear in the input path
     */
    private List<String> getRelativePathParts(final String path) {
        (path =~ /^\/*(.*)\/*$/).with {
            if (it) {
                it[0][1].replaceAll(/\/+/, '/').split('/') as List
            } else {
                []
            }
        }
    }

    /**
     * Recurses the descendant tree until no child with a given name can be found
     * or the path is exhausted, returning the final identified resource.
     * @param pathParts the relative path parts to this resource
     * @return the descendant at the requested path
     */
    private ResourceSpecification findDescendantResource(final List<String> pathParts) {
        pathParts ? children[pathParts.head()]?.findDescendantResource(pathParts.tail()) : this
    }
}
