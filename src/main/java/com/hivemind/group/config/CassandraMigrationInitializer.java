package com.hivemind.group.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

/**
 * Ensures new columns exist on Cassandra tables at startup.
 * This handles schema evolution without requiring manual ALTER TABLE commands.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CassandraMigrationInitializer implements CommandLineRunner
{
    private final CassandraOperations cassandraOperations;

    @Override
    public void run(String... args)
    {
        addColumnIfNotExists("groups", "profile_picture_url", "text");
        addColumnIfNotExists("groups", "cover_picture_url", "text");
        createIndexIfNotExists("groups", "privacy", "groups_privacy_idx");
    }

    private void addColumnIfNotExists(String table, String column, String type)
    {
        try
        {
            cassandraOperations.getCqlOperations().execute(
                String.format("ALTER TABLE %s ADD %s %s", table, column, type)
            );
            log.info("Column {}.{} added successfully", table, column);
        }
        catch (Exception e)
        {
            if (e.getMessage() != null && e.getMessage().contains("already exist"))
            {
                log.debug("Column {}.{} already exists", table, column);
            }
            else
            {
                log.warn("Could not add column {}.{}: {}", table, column, e.getMessage());
            }
        }
    }

    private void createIndexIfNotExists(String table, String column, String indexName)
    {
        try
        {
            cassandraOperations.getCqlOperations().execute(
                String.format("CREATE INDEX IF NOT EXISTS %s ON %s (%s)", indexName, table, column)
            );
            log.info("Index {} on {}.{} created or exists", indexName, table, column);
        }
        catch (Exception e)
        {
            log.warn("Could not create index {}: {}", indexName, e.getMessage());
        }
    }
}
