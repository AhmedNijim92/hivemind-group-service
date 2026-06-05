# Group Service

> HiveMind Group Management Microservice

## Overview

The group-service manages the creation and membership of groups. Users can create groups, join/leave them, view members, and admins can remove members. Group creation events are published to Kafka for downstream consumption.

## Service Info

| Property | Value |
|----------|-------|
| Port | 8083 |
| Service Name | `group-service` |
| Database | Apache Cassandra + Redis |
| Keyspace | `group_keyspace` |
| Spring Boot | 3.3.5 |
| Spring Cloud | 2023.0.3 |
| Java | 17 |

## Architecture

```
Client (via Gateway)
  │
  ▼
GroupController
  │
  ├── IGroupService (createGroup, getGroupById, getGroupsByCreator, join, leave, getMembers, removeMember)
  │       ├── GroupRepository (Cassandra)
  │       └── GroupMemberRepository (Cassandra)
  │
  └── Kafka Producer → group-created-topic
```

## API Endpoints

Base path: `/api/v1/groups`
All endpoints require JWT (X-User-Id header injected by gateway).

| Method | Path | Description |
|--------|------|-------------|
| POST | `/` | Create a new group |
| GET | `/{groupId}` | Get group details |
| GET | `/my` | Get groups created by current user |
| POST | `/{groupId}/join` | Join a group |
| POST | `/{groupId}/leave` | Leave a group |
| GET | `/{groupId}/members` | List group members |
| DELETE | `/{groupId}/members/{userId}` | Remove member (admin only) |

### Request/Response Examples

#### POST /api/v1/groups
```json
// Request
{
  "name": "Backend Team",
  "description": "Discussion group for backend developers",
  "privacy": "PUBLIC"
}

// Response (201)
{
  "groupId": "uuid",
  "creatorId": "uuid",
  "name": "Backend Team",
  "description": "Discussion group for backend developers",
  "privacy": "PUBLIC",
  "memberCount": 1,
  "createdAt": "2025-06-04T10:00:00"
}
```

#### GET /api/v1/groups/{groupId}/members
```json
// Response (200)
[
  {
    "groupId": "uuid",
    "userId": "uuid",
    "role": "ADMIN",
    "joinedAt": "2025-06-04T10:00:00"
  }
]
```

## Data Model

### Group (Cassandra table: `groups`)

| Column | Type | Description |
|--------|------|-------------|
| group_id | UUID | Primary key |
| creator_id | UUID | Group creator |
| name | String | Group name |
| description | String | Group description |
| privacy | String | PUBLIC or PRIVATE |
| member_count | int | Number of members |
| created_at | LocalDateTime | Creation timestamp |

### GroupMember (Cassandra table: `group_members`)

| Column | Type | Key Type | Description |
|--------|------|----------|-------------|
| group_id | UUID | PARTITION | Group identifier |
| user_id | UUID | CLUSTERED | Member identifier |
| role | String | — | ADMIN, MODERATOR, MEMBER |
| joined_at | LocalDateTime | — | Join timestamp |

## Kafka Events

### Produces: `group-created-topic`

Published when a new group is created:

```json
{
  "groupId": "uuid",
  "creatorId": "uuid",
  "groupName": "Backend Team",
  "timestamp": "2025-06-04T10:00:00"
}
```

**Consumers:**
- `notification-service` — generates a notification for the creator

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| CASSANDRA_HOST | localhost | Cassandra contact point |
| CASSANDRA_PORT | 9042 | Cassandra port |
| CASSANDRA_DATACENTER | datacenter1 | Cassandra datacenter |
| KAFKA_BOOTSTRAP_SERVERS | localhost:9092 | Kafka brokers |
| REDIS_HOST | localhost | Redis host |
| REDIS_PORT | 6379 | Redis port |
| EUREKA_SERVER | http://localhost:8761/eureka | Eureka URL |

## Dependencies

- spring-boot-starter-web
- spring-boot-starter-data-cassandra
- spring-boot-starter-data-redis
- spring-boot-starter-validation
- spring-cloud-starter-netflix-eureka-client
- spring-cloud-starter-config
- spring-kafka
- hivemind-common (1.0.0)
- lombok

## Running Locally

```bash
# Prerequisites: Cassandra, Kafka, Redis running
cd microservices/group-service
mvn spring-boot:run
```

Auto-creates `group_keyspace`, `groups`, and `group_members` tables on startup.

## Business Rules

- The creator automatically becomes a member with `ADMIN` role
- Only `ADMIN` role members can remove other members
- `member_count` is incremented/decremented on join/leave
- A member cannot be removed if they are the only ADMIN
