package com.hivemind.group.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("groups")
public class Group
{
    @PrimaryKey("group_id")
    private UUID groupId;

    @Column("creator_id")
    private UUID creatorId;

    @Column("name")
    private String name;

    @Column("description")
    private String description;

    @Column("privacy")
    private String privacy; // PUBLIC, PRIVATE

    @Column("member_count")
    private int memberCount;

    @Column("created_at")
    private LocalDateTime createdAt;
}
