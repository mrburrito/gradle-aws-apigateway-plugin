package com.shankyank.gradle.aws.apigateway.specification

import com.shankyank.gradle.aws.apigateway.model.HttpMethod
import groovy.transform.Immutable
import groovy.transform.Memoized

/**
 * A resource defined by the specification. Each resource is defined
 * by a path and represents a single path element. Resources may or
 * may not have operations and child resources associated with them.
 */
@Immutable
class ResourceSpecification implements Comparable<ResourceSpecification> {
    /** The parent of this resource. */
    ResourceSpecification parent

    /** The name of this resource. */
    String name

    /** The operations on this resource. */
    Map<HttpMethod, MethodSpecification> operations

    /** The children of this resource. */
    List<ResourceSpecification> children = []

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
}
