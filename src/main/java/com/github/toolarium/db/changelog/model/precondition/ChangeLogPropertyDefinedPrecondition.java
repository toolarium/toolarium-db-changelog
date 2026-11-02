/*
 * ChangeLogPropertyDefinedPrecondition.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.precondition;


/**
 * Checks if a changelog property is defined in the parameter resolver.
 *
 * @author patrick
 */
public class ChangeLogPropertyDefinedPrecondition implements IChangelogPrecondition {
    private final String propertyName;


    /**
     * Constructor for ChangeLogPropertyDefinedPrecondition
     *
     * @param propertyName the property name
     */
    public ChangeLogPropertyDefinedPrecondition(String propertyName) {
        this.propertyName = propertyName;
    }


    /**
     * Get the property name.
     *
     * @return the property name
     */
    public String getPropertyName() {
        return propertyName;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.precondition.IChangelogPrecondition#getDescription()
     */
    @Override
    public String getDescription() {
        return "changeLogPropertyDefined: " + propertyName;
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(propertyName);
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
        return java.util.Objects.equals(propertyName, ((ChangeLogPropertyDefinedPrecondition) obj).propertyName);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getDescription();
    }
}
