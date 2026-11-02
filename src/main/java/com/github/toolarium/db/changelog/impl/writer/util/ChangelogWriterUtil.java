/*
 * ChangelogWriterUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.writer.util;

import com.github.toolarium.db.changelog.model.IChangelogChangeSet;
import com.github.toolarium.db.changelog.model.IChangelogEntry;
import com.github.toolarium.db.changelog.model.precondition.ChangeLogPropertyDefinedPrecondition;
import com.github.toolarium.db.changelog.model.precondition.ChangelogPreconditionContainer;
import com.github.toolarium.db.changelog.model.precondition.DbmsPrecondition;
import com.github.toolarium.db.changelog.model.precondition.IChangelogPrecondition;
import com.github.toolarium.db.changelog.model.precondition.RowCountPrecondition;
import com.github.toolarium.db.changelog.model.precondition.TableExistsPrecondition;
import com.github.toolarium.db.changelog.model.precondition.TableIsEmptyPrecondition;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Singleton utility for serializing changelog changesets to map structures.
 *
 * @author patrick
 */
public final class ChangelogWriterUtil {

    /**
     * Private class, the only instance of the singleton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ChangelogWriterUtil INSTANCE = new ChangelogWriterUtil();
    }


    /**
     * Private constructor
     */
    private ChangelogWriterUtil() {
    }


    /**
     * Get the instance.
     *
     * @return the instance
     */
    public static ChangelogWriterUtil getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Convert a changeset to a map structure suitable for serialization.
     *
     * @param changeSet the changeset
     * @return the map representation
     */
    public Map<String, Object> toChangeSetMap(IChangelogChangeSet changeSet) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", changeSet.getIdentity().getId());
        map.put("author", changeSet.getIdentity().getAuthor());

        if (changeSet.getComment() != null) {
            map.put("comment", changeSet.getComment());
        }

        if (changeSet.isRunAlways()) {
            map.put("runAlways", Boolean.TRUE);
        }
        if (changeSet.isRunOnChange()) {
            map.put("runOnChange", Boolean.TRUE);
        }
        if (!changeSet.isFailOnError()) {
            map.put("failOnError", Boolean.FALSE);
        }
        if (!changeSet.isRunInTransaction()) {
            map.put("runInTransaction", Boolean.FALSE);
        }

        if (changeSet.getFilePath() != null) {
            map.put("filePath", changeSet.getFilePath());
        }

        if (!changeSet.getContexts().isEmpty()) {
            map.put("context", changeSet.getContexts());
        }
        if (!changeSet.getLabels().isEmpty()) {
            map.put("labels", changeSet.getLabels());
        }

        if (changeSet.getPreconditions() != null) {
            map.put("preconditions", toPreconditionsMap(changeSet.getPreconditions()));
        }

        List<Object> changes = toEntryList(changeSet.getEntries());
        map.put("changes", changes);

        if (!changeSet.getRollbackEntries().isEmpty()) {
            map.put("rollback", toEntryList(changeSet.getRollbackEntries()));
        }

        return map;
    }


    /**
     * Convert a list of changelog entries to a list of map structures.
     *
     * @param entries the entries
     * @return the list of maps
     */
    private List<Object> toEntryList(List<IChangelogEntry> entries) {
        List<Object> result = new ArrayList<>();
        for (IChangelogEntry entry : entries) {
            result.add(ChangelogEntryWriterUtil.getInstance().toMap(entry));
        }
        return result;
    }


    /**
     * Convert a precondition container to a map structure.
     *
     * @param container the precondition container
     * @return the map representation
     */
    private Map<String, Object> toPreconditionsMap(ChangelogPreconditionContainer container) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("onFail", container.getOnFail().name());
        map.put("onError", container.getOnError().name());

        List<Map<String, Object>> checks = new ArrayList<>();
        for (IChangelogPrecondition precondition : container.getChecks()) {
            Map<String, Object> checkMap = new LinkedHashMap<>();
            if (precondition instanceof DbmsPrecondition) {
                checkMap.put("dbms", ((DbmsPrecondition) precondition).getDbms());
            } else if (precondition instanceof TableExistsPrecondition) {
                checkMap.put("tableExists", ((TableExistsPrecondition) precondition).getTableName());
            } else if (precondition instanceof TableIsEmptyPrecondition) {
                checkMap.put("tableIsEmpty", ((TableIsEmptyPrecondition) precondition).getTableName());
            } else if (precondition instanceof RowCountPrecondition) {
                RowCountPrecondition rc = (RowCountPrecondition) precondition;
                Map<String, Object> rcMap = new LinkedHashMap<>();
                rcMap.put("tableName", rc.getTableName());
                rcMap.put("expectedCount", rc.getExpectedCount());
                checkMap.put("rowCount", rcMap);
            } else if (precondition instanceof ChangeLogPropertyDefinedPrecondition) {
                checkMap.put("changeLogPropertyDefined", ((ChangeLogPropertyDefinedPrecondition) precondition).getPropertyName());
            }
            if (!checkMap.isEmpty()) {
                checks.add(checkMap);
            }
        }

        if (!checks.isEmpty()) {
            map.put("checks", checks);
        }

        return map;
    }
}
