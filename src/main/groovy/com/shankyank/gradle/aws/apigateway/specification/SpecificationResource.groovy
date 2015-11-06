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
class SpecificationResource implements Comparable<SpecificationResource> {
    /** The parent of this resource. */
    SpecificationResource parent

    /** The name of this resource. */
    String name

    /** The operations on this resource. */
    Map<HttpMethod, SpecificationMethod> operations

    /** The children of this resource. */
    List<SpecificationResource> children = []

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
    SortedSet<SpecificationResource> getFlattenedResourceTree() {
        new TreeSet(this) + children?.collectMany { it.flattenedResourceTree }
    }

    @Override
    int compareTo(SpecificationResource other) {
        parent <=> other.parent ?: name <=> other.name
    }
}
