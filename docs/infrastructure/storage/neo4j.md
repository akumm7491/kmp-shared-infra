# Neo4j Integration

Neo4j serves as our graph database solution, ideal for handling complex relationships and graph-based queries.

## Features

- Native graph storage
- Cypher query language
- ACID compliance
- Causal clustering
- Full-text search

## Setup

### 1. Infrastructure Configuration

```yaml
# deploy/k8s/modules/storage/neo4j/values.yaml
neo4j:
  enabled: true
  version: "5.11.0"
  
  mode: "cluster"  # standalone or cluster
  
  resources:
    requests:
      memory: "4Gi"
      cpu: "2"
    limits:
      memory: "8Gi"
      cpu: "4"
      
  storage:
    data:
      size: "100Gi"
      class: "standard"
    logs:
      size: "20Gi"
      class: "standard"
      
  cluster:
    coreServers: 3
    readReplicas: 2
    
  security:
    authEnabled: true
    passwordSecret: "neo4j-credentials"
```

### 2. Service Integration

```kotlin
@Configuration
class Neo4jConfig {
    @Bean
    fun neo4jClient(): Neo4jClient {
        return Neo4jClient {
            uri = config.getString("neo4j.uri")
            authentication {
                fromSecret("neo4j-credentials")
            }
            connectionPool {
                maxSize = 50
                idleTime = 30.seconds
            }
        }
    }
}
```

## Usage

### 1. Node Definitions

```kotlin
@Node
data class User(
    @Id val id: String,
    val name: String,
    val email: String,
    
    @Relationship(type = "FOLLOWS")
    val following: Set<User> = emptySet(),
    
    @Relationship(type = "LIKES")
    val likedPosts: Set<Post> = emptySet()
)

@Node
data class Post(
    @Id val id: String,
    val content: String,
    val timestamp: Instant,
    
    @Relationship(type = "POSTED_BY", direction = Direction.INCOMING)
    val author: User
)
```

### 2. Repository Layer

```kotlin
@Repository
class UserRepository(
    private val neo4j: Neo4jClient
) {
    suspend fun findUserWithNetwork(
        userId: String,
        depth: Int = 2
    ): User? = neo4j.query {
        """
        MATCH (u:User {id: $userId})
        OPTIONAL MATCH (u)-[r:FOLLOWS*1..$depth]-(connected)
        RETURN u, collect(r), collect(connected)
        """.trimIndent(),
        parameters = mapOf(
            "userId" to userId,
            "depth" to depth
        )
    }
    
    suspend fun findInfluencers(
        minFollowers: Int = 1000
    ): List<User> = neo4j.query {
        """
        MATCH (u:User)
        WITH u, size((u)<-[:FOLLOWS]-()) as followers
        WHERE followers >= $minFollowers
        RETURN u
        ORDER BY followers DESC
        """.trimIndent(),
        parameters = mapOf(
            "minFollowers" to minFollowers
        )
    }
}
```

### 3. Transaction Management

```kotlin
@Service
class UserService(
    private val neo4j: Neo4jClient
) {
    @Transactional
    suspend fun createUserNetwork(
        user: User,
        initialConnections: List<String>
    ) = neo4j.transaction { tx ->
        try {
            // Create user
            tx.query(
                """
                CREATE (u:User)
                SET u = $user
                RETURN u
                """.trimIndent(),
                parameters = mapOf("user" to user)
            )
            
            // Create connections
            initialConnections.forEach { connectionId ->
                tx.query(
                    """
                    MATCH (u:User {id: $connectionId})
                    MATCH (new:User {id: ${user.id}})
                    CREATE (new)-[:FOLLOWS]->(u)
                    """.trimIndent()
                )
            }
            
            tx.commit()
        } catch (e: Exception) {
            tx.rollback()
            throw e
        }
    }
}
```

## Best Practices

### 1. Schema Design

```kotlin
// Good: Efficient graph model
@Node
data class Department(
    @Id val id: String,
    val name: String,
    
    @Relationship(type = "MANAGES", direction = Direction.INCOMING)
    val manager: Employee,
    
    @Relationship(type = "WORKS_IN", direction = Direction.INCOMING)
    val employees: Set<Employee>
)

// Bad: Relational thinking in graph database
@Node
data class DepartmentBad(
    @Id val id: String,
    val name: String,
    val managerId: String,  // Don't use foreign key style references
    val employeeIds: List<String>  // Don't store relationships as properties
)
```

### 2. Query Optimization

```kotlin
@Configuration
class QueryOptimization {
    fun configureIndexes() {
        neo4j.query(
            """
            CREATE INDEX user_email IF NOT EXISTS
            FOR (u:User)
            ON (u.email)
            """.trimIndent()
        )
        
        neo4j.query(
            """
            CREATE INDEX post_timestamp IF NOT EXISTS
            FOR (p:Post)
            ON (p.timestamp)
            """.trimIndent()
        )
    }
}
```

### 3. Caching Strategy

```kotlin
@Configuration
class CacheConfig {
    @Bean
    fun neo4jCache(): Cache<String, Any> {
        return Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(5.minutes)
            .build()
    }
}

@Repository
class CachedUserRepository(
    private val neo4j: Neo4jClient,
    private val cache: Cache<String, Any>
) {
    suspend fun findUser(id: String): User? {
        return cache.get(id) {
            neo4j.query<User>(
                "MATCH (u:User {id: $id}) RETURN u"
            ).firstOrNull()
        }
    }
}
```

## Monitoring

### 1. Health Checks

```kotlin
@HealthIndicator("neo4j")
class Neo4jHealthCheck(
    private val client: Neo4jClient
) {
    suspend fun check(): Health {
        return try {
            val result = client.query("CALL dbms.cluster.overview()")
            Health.up()
                .withDetail("cluster", result)
                .build()
        } catch (e: Exception) {
            Health.down()
                .withException(e)
                .build()
        }
    }
}
```

### 2. Metrics

```kotlin
@MetricsConfiguration
class Neo4jMetrics {
    fun registerMetrics() {
        metrics.gauge("neo4j.connections.active") {
            client.activeConnections()
        }
        
        metrics.timer("neo4j.query.duration") {
            // Query execution time
        }
        
        metrics.counter("neo4j.errors") {
            // Error count
        }
    }
}
```

## Backup and Recovery

### 1. Backup Configuration

```yaml
backup:
  schedule: "0 3 * * *"  # Daily at 3 AM
  retention: "7d"
  destination: "s3://backups/neo4j"
  type: "full"  # or incremental
```

### 2. Recovery Procedures

```bash
# Restore from backup
./tools/scripts/restore-neo4j.sh \
  --backup-id 2024-01-01-03-00 \
  --target-instance neo4j-prod

# Verify restoration
./tools/scripts/verify-neo4j.sh \
  --instance neo4j-prod
```

## Troubleshooting

### Common Issues

1. **Memory Issues**
```kotlin
@Troubleshoot
class MemoryTroubleshooter {
    fun diagnose(): List<Issue> {
        return listOf(
            checkHeapUsage(),
            checkGCMetrics(),
            checkPageCacheUsage(),
            checkMemoryPressure()
        )
    }
}
```

2. **Performance Issues**
```kotlin
@Troubleshoot
class PerformanceTroubleshooter {
    fun diagnose(): List<Issue> {
        return listOf(
            checkSlowQueries(),
            checkIndexUsage(),
            checkLockContention(),
            checkNetworkLatency()
        )
    }
}
```

## References

1. [Neo4j Documentation](https://neo4j.com/docs/)
2. [Graph Data Modeling Guidelines](https://neo4j.com/developer/guide-data-modeling/)
3. [Neo4j Kubernetes Operator](https://neo4j.com/docs/operations-manual/current/kubernetes/)
