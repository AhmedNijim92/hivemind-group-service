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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("group_members")
public class GroupMember
{
    @PrimaryKeyColumn(name = "group_id", type = PrimaryKeyType.PARTITIONED)
    private UUID groupId;

    @PrimaryKeyColumn(name = "user_id", type = PrimaryKeyType.CLUSTERED)
    private UUID userId;

    @Column("role")
    private String role; // ADMIN, MODERATOR, MEMBER

    @Column("joined_at")
    private LocalDateTime joinedAt;
}
