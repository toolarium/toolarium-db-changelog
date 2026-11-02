/*
 * IndexExistsPrecondition.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.precondition;


/**
 * Checks if an index exists.
 *
 * @author patrick
 */
public class IndexExistsPrecondition implements IChangelogPrecondition {
    private final String indexName;


    /**
     * Constructor for IndexExistsPrecondition
     *
     * @param indexName the indexName
     */
    public IndexExistsPrecondition(String indexName) {
        this.indexName = indexName;
    }


    /**
     * Get the indexName.
     *
     * @return the indexName
     */
    public String getIndexName() {
        return indexName;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.precondition.IChangelogPrecondition#getDescription()
     */
    @Override
    public String getDescription() {
        return "indexExists: " + indexName;
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(indexName);
    }


    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        IndexExistsPrecondition other = (IndexExistsPrecondition) obj;
        return java.util.Objects.equals(indexName, other.indexName);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getDescription();
    }
}
