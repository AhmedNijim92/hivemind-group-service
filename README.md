# HiveMind Group Service

Groups, posts, and community content service for the HiveMind platform.

## Responsibilities

- **Groups**: CRUD, membership management (join/leave/remove), privacy settings
- **Group Social**: Group-to-group following, group likes
- **Posts**: Create posts within groups, feed aggregation across multiple groups
- **Comments**: Add and list comments on posts
- **Likes**: Toggle like/unlike on posts

## API Endpoints

### Groups (`/api/v1/groups`)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/` | Create group |
| GET | `/{groupId}` | Get group by ID |
| GET | `/search?q=` | Search groups |
| PUT | `/{groupId}` | Update group |
| GET | `/my` | Get groups created by user |
| GET | `/memberships` | Get all user memberships |
| POST | `/{groupId}/join` | Join group |
| POST | `/{groupId}/leave` | Leave group |
| GET | `/{groupId}/members` | List members |
| DELETE | `/{groupId}/members/{userId}` | Remove member |
| POST | `/{groupId}/follow` | Follow another group |
| DELETE | `/{groupId}/follow/{targetGroupId}` | Unfollow group |
| GET | `/{groupId}/following` | Get followed groups |
| POST | `/{groupId}/like` | Like group |
| DELETE | `/{groupId}/like` | Unlike group |

### Posts (`/api/v1/posts`)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/` | Create post |
| GET | `/group/{groupId}` | Get posts by group |
| GET | `/feed?groupIds=` | Aggregated feed (multi-group) |
| GET | `/{groupId}/{postId}` | Get single post |
| POST | `/{groupId}/{postId}/like` | Toggle like/unlike |
| POST | `/{groupId}/{postId}/comments` | Add comment |
| GET | `/{postId}/comments` | Get comments |

## Tech Stack

- Java 17, Spring Boot 3.3
- Cassandra (group_keyspace): groups, members, posts, comments, post_likes, follows, likes tables
- Redis: Caching
- Kafka: Publishes `group-created-topic` and `post-created-topic` events
- Eureka: Service discovery

## Running

```bash
mvn clean package -DskipTests
java -jar target/*.jar
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| CASSANDRA_HOST | localhost | Cassandra contact point |
| REDIS_HOST | localhost | Redis host |
| KAFKA_BOOTSTRAP_SERVERS | localhost:9092 | Kafka brokers |
| EUREKA_SERVER | http://localhost:8761/eureka | Eureka URL |
