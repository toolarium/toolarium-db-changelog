/*
 * DbmsPrecondition.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.precondition;


/**
 * Checks the database type.
 *
 * @author patrick
 */
public class DbmsPrecondition implements IChangelogPrecondition {
    private final String dbms;


    /**
     * Constructor for DbmsPrecondition
     *
     * @param dbms the dbms
     */
    public DbmsPrecondition(String dbms) {
        this.dbms = dbms;
    }


    /**
     * Get the dbms.
     *
     * @return the dbms
     */
    public String getDbms() {
        return dbms;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.precondition.IChangelogPrecondition#getDescription()
     */
    @Override
    public String getDescription() {
        return "dbms: " + dbms;
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(dbms);
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
        DbmsPrecondition other = (DbmsPrecondition) obj;
        return java.util.Objects.equals(dbms, other.dbms);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getDescription();
    }
}
