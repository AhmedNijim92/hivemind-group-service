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
@Table("group_follows")
public class GroupFollow
{
    @PrimaryKeyColumn(name = "group_id", type = PrimaryKeyType.PARTITIONED)
    private UUID groupId;

    @PrimaryKeyColumn(name = "followed_group_id", type = PrimaryKeyType.CLUSTERED)
    private UUID followedGroupId;

    @Column("created_by")
    private UUID createdBy;

    @Column("created_at")
    private LocalDateTime createdAt;
}
