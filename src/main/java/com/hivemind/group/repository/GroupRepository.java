package com.hivemind.group.repository;

import com.hivemind.group.entity.Group;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupRepository extends CassandraRepository<Group, UUID>
{
    @Query("SELECT * FROM groups WHERE creator_id = ?0 ALLOW FILTERING")
    List<Group> findByCreatorId(UUID creatorId);
}
