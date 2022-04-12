package org.commcare.core.interfaces;

import java.sql.SQLException;

import javax.annotation.Nullable;

/**
 * Read and write operations for entity selections made on a mult-select Entity Screen
 */
public interface EntitiesSelectionCache {

    String write(String[] values);

    @Nullable
    String[] read(String key);

    boolean contains(String key);
}
