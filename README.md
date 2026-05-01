# Group Service

Group management service for the HiveMind platform. Handles group creation, membership management, and group-based interactions.

## Details

| Property | Value |
|----------|-------|
| **Port** | `8083` |
| **Database** | Cassandra |
| **Messaging** | Kafka |
| **Role** | Groups + Membership |

## Build & Run

```bash
# Build
mvn clean package

# Run
java -jar target/*.jar

# Docker
docker build -t hivemind/group-service .
```

## Links

- [Main Repository](https://github.com/AhmedNijim92/hivemind-backend)
