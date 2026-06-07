package com.hivemind.group.repository;

import com.hivemind.group.entity.GroupLike;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupLikeRepository extends CassandraRepository<GroupLike, Object>
{
    @Query("SELECT * FROM group_likes WHERE group_id = ?0")
    List<GroupLike> findByGroupId(UUID groupId);

    @Query("SELECT * FROM group_likes WHERE group_id = ?0 AND user_id = ?1")
    Optional<GroupLike> findByGroupIdAndUserId(UUID groupId, UUID userId);
}
