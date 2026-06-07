package com.hivemind.group.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Reverse-lookup table: user_id (partition) -> group_id (cluster).
 * Allows efficient lookup of all groups a user belongs to.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user_groups")
public class UserGroup
{
    @PrimaryKeyColumn(name = "user_id", type = PrimaryKeyType.PARTITIONED)
    private UUID userId;

    @PrimaryKeyColumn(name = "group_id", type = PrimaryKeyType.CLUSTERED)
    private UUID groupId;

    @Column("role")
    private String role; // ADMIN, MODERATOR, MEMBER

    @Column("joined_at")
    private LocalDateTime joinedAt;
}
