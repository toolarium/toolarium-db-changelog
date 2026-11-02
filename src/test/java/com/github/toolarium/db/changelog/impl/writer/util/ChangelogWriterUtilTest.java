/*
 * ChangelogWriterUtilTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.writer.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.model.ChangelogChangeSet;
import com.github.toolarium.db.changelog.model.ChangelogChangeSetIdentity;
import com.github.toolarium.db.changelog.model.IChangelogEntry;
import com.github.toolarium.db.changelog.model.precondition.ChangelogPreconditionContainer;
import com.github.toolarium.db.changelog.model.precondition.ChangelogPreconditionFailureAction;
import com.github.toolarium.db.changelog.model.precondition.DbmsPrecondition;
import com.github.toolarium.db.changelog.model.precondition.IChangelogPrecondition;
import com.github.toolarium.db.changelog.model.sql.SqlChangelogEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;


/**
 * Tests for ChangelogWriterUtil.
 *
 * @author patrick
 */
public class ChangelogWriterUtilTest {

    /**
     * Test singleton instance.
     */
    @Test
    void testSingleton() {
        ChangelogWriterUtil instance1 = ChangelogWriterUtil.getInstance();
        ChangelogWriterUtil instance2 = ChangelogWriterUtil.getInstance();
        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }


    /**
     * Test changeset with basic attributes.
     */
    @SuppressWarnings("unchecked")
    @Test
    void testToChangeSetMapBasic() {
        ChangelogChangeSetIdentity identity = new ChangelogChangeSetIdentity("cs-1", "author", "file.db.yaml");
        ChangelogChangeSet cs = new ChangelogChangeSet(identity,
                Collections.singletonList(new SqlChangelogEntry("SELECT 1")));

        Map<String, Object> map = ChangelogWriterUtil.getInstance().toChangeSetMap(cs);

        assertEquals("cs-1", map.get("id"));
        assertEquals("author", map.get("author"));
        assertNotNull(map.get("changes"));

        List<Object> changes = (List<Object>) map.get("changes");
        assertEquals(1, changes.size());

        // Default attributes should not be present
        assertFalse(map.containsKey("runAlways"));
        assertFalse(map.containsKey("runOnChange"));
        assertFalse(map.containsKey("failOnError"));
        assertFalse(map.containsKey("runInTransaction"));
        assertFalse(map.containsKey("comment"));
        assertFalse(map.containsKey("rollback"));
    }


    /**
     * Test changeset with all attributes set.
     */
    @Test
    void testToChangeSetMapWithAttributes() {
        ChangelogChangeSetIdentity identity = new ChangelogChangeSetIdentity("cs-2", "author", "file.db.yaml");
        List<IChangelogEntry> entries = Collections.singletonList(new SqlChangelogEntry("SELECT 1"));
        List<IChangelogEntry> rollback = Collections.singletonList(new SqlChangelogEntry("SELECT 0"));
        ChangelogChangeSet cs = new ChangelogChangeSet(identity, entries, rollback, "My comment",
                true, true, false, false,
                Arrays.asList("dev", "test"), Arrays.asList("v1.0"), null);

        Map<String, Object> map = ChangelogWriterUtil.getInstance().toChangeSetMap(cs);

        assertEquals("cs-2", map.get("id"));
        assertEquals("My comment", map.get("comment"));
        assertEquals(Boolean.TRUE, map.get("runAlways"));
        assertEquals(Boolean.TRUE, map.get("runOnChange"));
        assertEquals(Boolean.FALSE, map.get("failOnError"));
        assertEquals(Boolean.FALSE, map.get("runInTransaction"));
        assertTrue(map.containsKey("context"));
        assertTrue(map.containsKey("labels"));
        assertTrue(map.containsKey("rollback"));
    }


    /**
     * Test changeset with preconditions.
     */
    @SuppressWarnings("unchecked")
    @Test
    void testToChangeSetMapWithPreconditions() {
        List<IChangelogPrecondition> checks = Collections.singletonList(new DbmsPrecondition("h2"));
        ChangelogPreconditionContainer container = new ChangelogPreconditionContainer(
                ChangelogPreconditionFailureAction.WARN,
                ChangelogPreconditionFailureAction.HALT,
                checks);

        ChangelogChangeSetIdentity identity = new ChangelogChangeSetIdentity("cs-3", "author", "file.db.yaml");
        List<IChangelogEntry> entries = Collections.singletonList(new SqlChangelogEntry("SELECT 1"));
        ChangelogChangeSet cs = new ChangelogChangeSet(identity, entries, Collections.emptyList(), null,
                false, false, true, true, Collections.emptyList(), Collections.emptyList(), container);

        Map<String, Object> map = ChangelogWriterUtil.getInstance().toChangeSetMap(cs);

        assertTrue(map.containsKey("preconditions"));
        Map<String, Object> precondMap = (Map<String, Object>) map.get("preconditions");
        assertEquals("WARN", precondMap.get("onFail"));
        assertEquals("HALT", precondMap.get("onError"));

        List<Map<String, Object>> checksList = (List<Map<String, Object>>) precondMap.get("checks");
        assertNotNull(checksList);
        assertEquals(1, checksList.size());
        assertEquals("h2", checksList.get(0).get("dbms"));
    }
}
