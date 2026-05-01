package com.hivemind.group.repository;

import com.hivemind.group.entity.GroupMember;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupMemberRepository extends CassandraRepository<GroupMember, Object>
{
    @Query("SELECT * FROM group_members WHERE group_id = ?0")
    List<GroupMember> findByGroupId(UUID groupId);

    @Query("SELECT * FROM group_members WHERE group_id = ?0 AND user_id = ?1")
    Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId);
}
