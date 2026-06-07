package com.hivemind.group.repository;

import com.hivemind.group.entity.UserGroup;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserGroupRepository extends CassandraRepository<UserGroup, Object>
{
    @Query("SELECT * FROM user_groups WHERE user_id = ?0")
    List<UserGroup> findByUserId(UUID userId);

    @Query("SELECT * FROM user_groups WHERE user_id = ?0 AND group_id = ?1")
    Optional<UserGroup> findByUserIdAndGroupId(UUID userId, UUID groupId);
}
