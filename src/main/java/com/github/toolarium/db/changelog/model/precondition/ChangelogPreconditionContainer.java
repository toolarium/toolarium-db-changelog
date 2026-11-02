/*
 * ChangelogPreconditionContainer.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.precondition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Container holding preconditions with their failure and error actions.
 *
 * @author patrick
 */
public class ChangelogPreconditionContainer {
    private final ChangelogPreconditionFailureAction onFail;
    private final ChangelogPreconditionFailureAction onError;
    private final List<IChangelogPrecondition> checks;


    /**
     * Constructor for ChangelogPreconditionContainer
     *
     * @param onFail action when precondition evaluates to false
     * @param onError action when precondition throws an exception
     * @param checks the precondition checks
     */
    public ChangelogPreconditionContainer(ChangelogPreconditionFailureAction onFail,
                                          ChangelogPreconditionFailureAction onError,
                                          List<IChangelogPrecondition> checks) {
        this.onFail = onFail;
        this.onError = onError;
        this.checks = Collections.unmodifiableList(new ArrayList<>(checks));
    }


    /**
     * Get the action when a precondition fails.
     *
     * @return the onFail action
     */
    public ChangelogPreconditionFailureAction getOnFail() {
        return onFail;
    }


    /**
     * Get the action when a precondition throws an error.
     *
     * @return the onError action
     */
    public ChangelogPreconditionFailureAction getOnError() {
        return onError;
    }


    /**
     * Get the precondition checks.
     *
     * @return the checks
     */
    public List<IChangelogPrecondition> getChecks() {
        return checks;
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(onFail, onError, checks);
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
        ChangelogPreconditionContainer other = (ChangelogPreconditionContainer) obj;
        return java.util.Objects.equals(onFail, other.onFail) && java.util.Objects.equals(onError, other.onError) && java.util.Objects.equals(checks, other.checks);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ChangelogPreconditionContainer[onFail=" + onFail + ", checks=" + checks.size() + "]";
    }
}
