# HiveMind Group Service

> Manages groups, membership, privacy controls, and the group follow/like system.

## Overview

The Group Service handles group lifecycle operations including creation (limited to one group per user), join/leave, member management, and profile updates (cover and profile pictures). It supports PUBLIC and PRIVATE group privacy modes with join requests for private groups. A group follow and like system enables discovery and engagement. `CassandraMigrationInitializer` adds `profile_picture_url`, `cover_picture_url` columns and creates a privacy index on startup.

## Features

- Group creation (one group per user limit)
- Join, leave, and member listing
- Profile and cover picture updates
- PUBLIC / PRIVATE privacy modes
- Join requests for private groups
- Group follow system
- Group likes
- Search public groups by name
- Schema auto-migration on startup via `CassandraMigrationInitializer`

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/groups` | JWT | Create a group |
| GET | `/api/v1/groups/{id}` | JWT | Get group details |
| PUT | `/api/v1/groups/{id}` | JWT | Update group (name, pics) |
| POST | `/api/v1/groups/{id}/join` | JWT | Join a group |
| POST | `/api/v1/groups/{id}/leave` | JWT | Leave a group |
| GET | `/api/v1/groups/{id}/members` | JWT | List group members |
| POST | `/api/v1/groups/{id}/follow` | JWT | Follow a group |
| DELETE | `/api/v1/groups/{id}/follow` | JWT | Unfollow a group |
| POST | `/api/v1/groups/{id}/like` | JWT | Like a group |
| GET | `/api/v1/groups/search?q=` | JWT | Search public groups |
| POST | `/api/v1/groups/{id}/join-requests` | JWT | Request to join private group |
| PUT | `/api/v1/groups/{id}/join-requests/{reqId}` | JWT | Approve/deny join request |

## Configuration

| Property | Description | Default |
|----------|-------------|---------|
| `server.port` | Service port | `8083` |
| `spring.cassandra.contact-points` | Cassandra hosts | `localhost` |
| `spring.cassandra.keyspace-name` | Cassandra keyspace | `hivemind` |
| `spring.data.redis.host` | Redis host | `localhost` |
| `eureka.client.serviceUrl.defaultZone` | Eureka registry URL | `http://localhost:8761/eureka` |

## Tech Stack

- Java 17
- Spring Boot 3.x
- Apache Cassandra
- Redis
- Eureka Client
- Maven

## Docker

```
Port: 8083
Base image: eclipse-temurin:17-jre-alpine
JVM flags: -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC
User: non-root (spring)
```

## CI/CD

- **Build**: Maven `clean package` with JDK 17 (Temurin)
- **Test**: Unit tests run during build phase
- **Docker**: Build and push to Docker Hub on `main` branch merge
- **Security**: Trivy vulnerability scan (CRITICAL, HIGH) on built image
