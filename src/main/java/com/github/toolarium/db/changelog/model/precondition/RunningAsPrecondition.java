/*
 * RunningAsPrecondition.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.precondition;


/**
 * Checks the database user.
 *
 * @author patrick
 */
public class RunningAsPrecondition implements IChangelogPrecondition {
    private final String username;


    /**
     * Constructor for RunningAsPrecondition
     *
     * @param username the username
     */
    public RunningAsPrecondition(String username) {
        this.username = username;
    }


    /**
     * Get the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.precondition.IChangelogPrecondition#getDescription()
     */
    @Override
    public String getDescription() {
        return "runningAs: " + username;
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(username);
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
        RunningAsPrecondition other = (RunningAsPrecondition) obj;
        return java.util.Objects.equals(username, other.username);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getDescription();
    }
}
