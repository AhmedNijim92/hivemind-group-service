# Group Service — Code-Level Reference

## GroupServiceApplication

**Package:** `com.hivemind.group`

**Annotations:**
- `@SpringBootApplication` — Enables auto-configuration, component scanning, and configuration properties
- `@EnableDiscoveryClient` — Registers with Eureka service registry
- `@EnableKafka` — Enables Kafka listener/producer annotations

**Design Pattern:** Application Entry Point (Spring Boot convention)

### Methods

#### `main(String[] args)`
- **Signature:** `public static void main(String[] args)`
- **Logic:** `SpringApplication.run(GroupServiceApplication.class, args)`
- **Returns:** void

---

## CassandraConfig

**Package:** `com.hivemind.group.config`

**Extends:** `AbstractCassandraConfiguration`

**Annotations:**
- `@Configuration`

**Design Pattern:** Template Method — overrides hook methods from abstract parent

### Overridden Methods

#### `getKeyspaceName()`
- **Returns:** `"group_keyspace"`

#### `getContactPoints()`
- **Returns:** Cassandra contact points (from configuration or default `"localhost"`)

#### `getPort()`
- **Returns:** Cassandra port (default `9042`)

#### `getLocalDataCenter()`
- **Returns:** `"datacenter1"`

#### `getSchemaAction()`
- **Returns:** `SchemaAction.CREATE_IF_NOT_EXISTS`

#### `getEntityBasePackages()`
- **Returns:** `new String[] { "com.hivemind.group.entity" }`

#### `getKeyspaceCreations()`
- **Logic:** Creates keyspace with SimpleStrategy, replication factor = 1, DURABLE_WRITES = true
- **Returns:** `List<CreateKeyspaceSpecification>`

---

## KafkaProducerConfig

**Package:** `com.hivemind.group.config`

**Annotations:**
- `@Configuration`

**Design Pattern:** Factory Method — creates configured Kafka producer components

### Beans

#### `producerFactory()`
- **Signature:** `@Bean public ProducerFactory<String, GroupCreatedEvent> producerFactory()`
- **Logic:** Configures producer with:
  - `bootstrap.servers` from application properties
  - Key serializer: `StringSerializer`
  - Value serializer: `JsonSerializer` (for GroupCreatedEvent)
- **Returns:** `DefaultKafkaProducerFactory<String, GroupCreatedEvent>`

#### `kafkaTemplate()`
- **Signature:** `@Bean public KafkaTemplate<String, GroupCreatedEvent> kafkaTemplate()`
- **Logic:** Wraps the `producerFactory()` in a `KafkaTemplate`
- **Returns:** `KafkaTemplate<String, GroupCreatedEvent>`

---

## GroupController

**Package:** `com.hivemind.group.controller`

**Annotations:**
- `@RestController`
- `@RequestMapping("/api/v1/groups")`

**Design Pattern:** Façade — exposes simplified REST API over service layer

### Fields (Constructor Injection)

| Field | Type |
|-------|------|
| groupService | IGroupService |

### Endpoints

#### `POST /`
- **Signature:** `public ResponseEntity<GroupDto> createGroup(@RequestHeader("X-User-Id") UUID userId, @Valid @RequestBody CreateGroupRequest request)`
- **Logic:** Delegates to `groupService.createGroup(userId, request)`
- **Returns:** `201 Created` with `GroupDto`
- **Headers:** `X-User-Id` — authenticated user ID (injected by API Gateway)

#### `GET /{groupId}`
- **Signature:** `public ResponseEntity<GroupDto> getGroupById(@PathVariable UUID groupId)`
- **Logic:** Delegates to `groupService.getGroupById(groupId)`
- **Returns:** `GroupDto`

#### `GET /my`
- **Signature:** `public ResponseEntity<List<GroupDto>> getMyGroups(@RequestHeader("X-User-Id") UUID userId)`
- **Logic:** Delegates to `groupService.getGroupsByCreator(userId)`
- **Returns:** `List<GroupDto>` — groups created by the user

#### `POST /{groupId}/join`
- **Signature:** `public ResponseEntity<ApiResponse> joinGroup(@PathVariable UUID groupId, @RequestHeader("X-User-Id") UUID userId)`
- **Logic:** Delegates to `groupService.joinGroup(groupId, userId)`
- **Returns:** `ApiResponse` with success message

#### `POST /{groupId}/leave`
- **Signature:** `public ResponseEntity<ApiResponse> leaveGroup(@PathVariable UUID groupId, @RequestHeader("X-User-Id") UUID userId)`
- **Logic:** Delegates to `groupService.leaveGroup(groupId, userId)`
- **Returns:** `ApiResponse` with success message

#### `GET /{groupId}/members`
- **Signature:** `public ResponseEntity<List<GroupMember>> getMembers(@PathVariable UUID groupId)`
- **Logic:** Delegates to `groupService.getMembers(groupId)`
- **Returns:** `List<GroupMember>`

#### `DELETE /{groupId}/members/{userId}`
- **Signature:** `public ResponseEntity<ApiResponse> removeMember(@PathVariable UUID groupId, @PathVariable UUID userId, @RequestHeader("X-User-Id") UUID requesterId)`
- **Logic:** Delegates to `groupService.removeMember(groupId, userId, requesterId)`
- **Returns:** `ApiResponse` with success message
- **Authorization:** Requester must be ADMIN or MODERATOR of the group

---

## Group (Entity)

**Package:** `com.hivemind.group.entity`

**Annotations:**
- `@Table("groups")` — Maps to Cassandra `groups` table

### Fields

| Field | Type | Annotation | Description |
|-------|------|------------|-------------|
| groupId | UUID | `@PrimaryKey` | Unique group identifier |
| creatorId | UUID | | User who created the group |
| name | String | | Group display name |
| description | String | | Group description |
| privacy | String | | `"PUBLIC"` or `"PRIVATE"` |
| memberCount | int | | Current number of members |
| createdAt | LocalDateTime | | Group creation timestamp |

---

## GroupMember (Entity)

**Package:** `com.hivemind.group.entity`

**Annotations:**
- `@Table("group_members")` — Maps to Cassandra `group_members` table

**Design Pattern:** Composite Key — models many-to-many relationship in Cassandra

### Fields

| Field | Type | Key Type | Description |
|-------|------|----------|-------------|
| groupId | UUID | `PARTITIONED` | The group |
| userId | UUID | `CLUSTERED` | The member |
| role | String | | `"ADMIN"`, `"MODERATOR"`, or `"MEMBER"` |
| joinedAt | LocalDateTime | | When the user joined |

---

## GroupRepository

**Package:** `com.hivemind.group.repository`

**Extends:** `CassandraRepository<Group, UUID>`

**Design Pattern:** Repository pattern

### Methods

#### `findByCreatorId(UUID creatorId)`
- **Signature:** `@Query(allowFiltering = true) List<Group> findByCreatorId(UUID creatorId)`
- **Logic:** CQL query with ALLOW FILTERING to find groups by creator
- **Returns:** `List<Group>`

---

## GroupMemberRepository

**Package:** `com.hivemind.group.repository`

**Extends:** `CassandraRepository<GroupMember, Object>`

**Design Pattern:** Repository pattern

### Methods

#### `findByGroupId(UUID groupId)`
- **Signature:** `@Query List<GroupMember> findByGroupId(UUID groupId)`
- **Returns:** `List<GroupMember>` — all members of a group

#### `findByGroupIdAndUserId(UUID groupId, UUID userId)`
- **Signature:** `Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId)`
- **Returns:** `Optional<GroupMember>` — specific membership record

---

## IGroupService (Interface)

**Package:** `com.hivemind.group.service`

### Method Signatures

| Method | Parameters | Returns |
|--------|-----------|---------|
| `createGroup` | `UUID creatorId, CreateGroupRequest request` | `GroupDto` |
| `getGroupById` | `UUID groupId` | `GroupDto` |
| `getGroupsByCreator` | `UUID creatorId` | `List<GroupDto>` |
| `joinGroup` | `UUID groupId, UUID userId` | `void` |
| `leaveGroup` | `UUID groupId, UUID userId` | `void` |
| `getMembers` | `UUID groupId` | `List<GroupMember>` |
| `removeMember` | `UUID groupId, UUID userId, UUID requesterId` | `void` |

---

## GroupServiceImpl

**Package:** `com.hivemind.group.service.impl`

**Annotations:**
- `@Service`

**Implements:** `IGroupService`

**Design Pattern:** Service Layer — encapsulates group management business logic

### Fields (Constructor Injection)

| Field | Type |
|-------|------|
| groupRepository | GroupRepository |
| groupMemberRepository | GroupMemberRepository |
| kafkaTemplate | KafkaTemplate<String, GroupCreatedEvent> |

### Methods

#### `createGroup(UUID creatorId, CreateGroupRequest request)`
- **Signature:** `@Override public GroupDto createGroup(UUID creatorId, CreateGroupRequest request)`
- **Logic:**
  1. Builds `Group` entity:
     - `groupId` = `UUID.randomUUID()`
     - `creatorId` = creatorId parameter
     - `name` = request.getName()
     - `description` = request.getDescription()
     - `privacy` = request.getPrivacy() (defaults to "PUBLIC")
     - `memberCount` = 1 (creator is first member)
     - `createdAt` = LocalDateTime.now()
  2. Saves group via `groupRepository.save(group)`
  3. Creates `GroupMember` for the creator:
     - `groupId` = group.getGroupId()
     - `userId` = creatorId
     - `role` = "ADMIN"
     - `joinedAt` = LocalDateTime.now()
  4. Saves member via `groupMemberRepository.save(member)`
  5. Publishes `GroupCreatedEvent` to Kafka (contains groupId, creatorId, name)
  6. Maps to DTO and returns
- **Returns:** `GroupDto`

#### `getGroupById(UUID groupId)`
- **Signature:** `@Override public GroupDto getGroupById(UUID groupId)`
- **Logic:**
  1. Calls `groupRepository.findById(groupId)`
  2. If not found → throws RuntimeException ("Group not found")
  3. Maps to DTO
- **Returns:** `GroupDto`
- **Exceptions:** RuntimeException if group not found

#### `getGroupsByCreator(UUID creatorId)`
- **Signature:** `@Override public List<GroupDto> getGroupsByCreator(UUID creatorId)`
- **Logic:**
  1. Calls `groupRepository.findByCreatorId(creatorId)`
  2. Maps each Group to GroupDto
- **Returns:** `List<GroupDto>`

#### `joinGroup(UUID groupId, UUID userId)`
- **Signature:** `@Override public void joinGroup(UUID groupId, UUID userId)`
- **Logic:**
  1. Checks if user is already a member via `groupMemberRepository.findByGroupIdAndUserId(groupId, userId)`
  2. If already member → throws exception ("Already a member of this group")
  3. Creates `GroupMember` with role = "MEMBER", joinedAt = now
  4. Saves member
  5. Loads group, increments `memberCount`, saves group
- **Returns:** void
- **Exceptions:** RuntimeException if already a member

#### `leaveGroup(UUID groupId, UUID userId)`
- **Signature:** `@Override public void leaveGroup(UUID groupId, UUID userId)`
- **Logic:**
  1. Finds member via `groupMemberRepository.findByGroupIdAndUserId(groupId, userId)`
  2. If not found → throws exception ("Not a member of this group")
  3. Deletes the GroupMember record
  4. Loads group, decrements `memberCount`, saves group
- **Returns:** void
- **Exceptions:** RuntimeException if not a member

#### `getMembers(UUID groupId)`
- **Signature:** `@Override public List<GroupMember> getMembers(UUID groupId)`
- **Logic:** Calls `groupMemberRepository.findByGroupId(groupId)`
- **Returns:** `List<GroupMember>`

#### `removeMember(UUID groupId, UUID userId, UUID requesterId)`
- **Signature:** `@Override public void removeMember(UUID groupId, UUID userId, UUID requesterId)`
- **Logic:**
  1. Finds requester's membership via `groupMemberRepository.findByGroupIdAndUserId(groupId, requesterId)`
  2. Validates requester has role `"ADMIN"` or `"MODERATOR"` — throws if not authorized
  3. Finds target member via `groupMemberRepository.findByGroupIdAndUserId(groupId, userId)`
  4. If target not found → throws exception
  5. Deletes target's GroupMember record
  6. Loads group, decrements `memberCount`, saves group
- **Returns:** void
- **Exceptions:** RuntimeException if requester not authorized or target not found

---

## DTOs

**Package:** `com.hivemind.group.dto`

### CreateGroupRequest

| Field | Type | Validation | Default |
|-------|------|------------|---------|
| name | String | `@NotBlank` | — |
| description | String | Optional | — |
| privacy | String | Optional | `"PUBLIC"` |

### GroupDto

| Field | Type | Description |
|-------|------|-------------|
| groupId | UUID | Unique group identifier |
| creatorId | UUID | Creator's user ID |
| name | String | Group name |
| description | String | Group description |
| privacy | String | PUBLIC or PRIVATE |
| memberCount | int | Number of members |
| createdAt | LocalDateTime | Creation timestamp |

### GroupCreatedEvent (Kafka Event — produced)

| Field | Type | Description |
|-------|------|-------------|
| groupId | UUID | New group's ID |
| creatorId | UUID | Creator's user ID |
| name | String | Group name |

### ApiResponse

| Field | Type | Description |
|-------|------|-------------|
| message | String | Success/error message |
| success | boolean | Operation result |
