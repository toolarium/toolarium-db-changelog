/*
 * ChangelogParameterResolverFactoryTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.resolver;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;


/**
 * Tests for ChangelogParameterResolverFactory.
 *
 * @author patrick
 */
public class ChangelogParameterResolverFactoryTest {

    /**
     * Test singleton and instance creation.
     */
    @Test
    void testFactory() {
        assertSame(ChangelogParameterResolverFactory.getInstance(), ChangelogParameterResolverFactory.getInstance());
        assertNotNull(ChangelogParameterResolverFactory.getInstance().getParameterResolver());
    }
}
