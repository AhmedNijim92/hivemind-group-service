package com.hivemind.group.repository;

import com.hivemind.group.entity.GroupFollow;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupFollowRepository extends CassandraRepository<GroupFollow, Object>
{
    @Query("SELECT * FROM group_follows WHERE group_id = ?0")
    List<GroupFollow> findByGroupId(UUID groupId);

    @Query("SELECT * FROM group_follows WHERE group_id = ?0 AND followed_group_id = ?1")
    Optional<GroupFollow> findByGroupIdAndFollowedGroupId(UUID groupId, UUID followedGroupId);
}
