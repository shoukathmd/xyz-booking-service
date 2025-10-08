# 🎬 Booking Service

A **Spring Boot 3.5.6** microservice for managing movie shows, theatres, and bookings.  
Implements modern engineering standards including **RBAC**, **ABAC**, **observability**, and **scalability** best practices.

---

## ⚙️ Tech Stack

| Category | Technology |
|-----------|-------------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.6 |
| ORM | Hibernate / JPA |
| Build Tool | Gradle |
| Database | H2 (Dev) |
| Mapper | MapStruct |
| Logging | SLF4J / Logback |
| Testing | JUnit 5, Mockito |
| Observability | Spring Boot Actuator |

---

## 🚀 Build & Run

### Prerequisites
- Java 21+
- Gradle 8+
- (Optional) Docker & Docker Compose

### Build the project
```bash
./gradlew clean build
```

### Run the service locally
```bash
./gradlew bootRun
```

The application starts at **http://localhost:8080**  
Actuator endpoint: **http://localhost:8080/actuator/health**

---

## 🧩 Core Modules & Layers

| Layer | Responsibility |
|--------|----------------|
| **Controller** | REST endpoints; request validation; RBAC authorization |
| **Service** | Business logic; ABAC checks; transaction management |
| **Repository** | Database access using Spring Data JPA |
| **Mapper** | Entity ↔ DTO conversion using MapStruct |
| **Exception Handling** | Centralized via `@ControllerAdvice` |
| **Observability** | Managed via Spring Boot Actuator |

---

## 🔐 Security

### 🧭 RBAC (Role-Based Access Control)
- Applied at the controller layer using `@PreAuthorize`.
- Roles: `ADMIN`, `THEATRE_OWNER`, `CUSTOMER`.

**Example:**
```java
@PreAuthorize("hasRole('THEATRE_OWNER')")
public ResponseEntity<ShowDTO> createShow(...) { ... }
```

---

### 🧠 ABAC (Attribute-Based Access Control)
- Enforced at the service layer.

**Example:**
```java
if (!authenticatedTenantId.equals(show.getTheatre().getTenantId())) {
    throw new AccessDeniedException("Unauthorized theatre access");
}
```

Attributes like `tenantId` are extracted from JWT claims (issued by AWS Cognito or Auth0).

---

### ☁️ Integration with AWS API Gateway + Cognito
- JWT tokens validated at the API Gateway.
- Claims (roles, tenantId, userId) are forwarded in headers.
- Spring Security parses headers and builds the `Authentication` context.

---

### 🛡️ OWASP Top 10 Considerations

| Concern | Mitigation |
|----------|-------------|
| **Injection** | Use JPA parameter binding, input validation |
| **Broken Auth** | Use JWT with short expiry + refresh tokens |
| **Sensitive Data Exposure** | Mask logs, enforce HTTPS, never log credentials |
| **XML/XXE** | Disable XML parsing (use JSON only) |
| **Broken Access Control** | RBAC + ABAC enforcement |
| **Security Misconfiguration** | Use Spring Boot profiles, secure defaults |
| **XSS** | JSON escaping (if UI added) |
| **CSRF** | Disabled for stateless APIs |
| **Vulnerable Dependencies** | Dependabot / Gradle dependency updates |
| **Insufficient Logging** | SLF4J structured logs + centralized monitoring |

---

## 🩺 Observability & Health

### ✅ Spring Boot Actuator Endpoints

| Endpoint | Purpose |
|-----------|----------|
| `/actuator/health` | Liveness & readiness probe for Docker/K8s |
| `/actuator/info` | Application metadata |
| `/actuator/metrics` | JVM, memory, and request metrics |
| `/actuator/loggers` | Runtime log-level management |

---

### 🧠 Health Check Integration (Kubernetes Example)
```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
```

---

## 🐳 Dockerization

**Dockerfile:**
```dockerfile
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY build/libs/booking-service-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

**Build & Run:**
```bash
docker build -t booking-service .
docker run -p 8080:8080 booking-service
```

---

## 🧱 Scalability & Reliability

| Concern | Practice |
|----------|-----------|
| **Stateless Services** | Store no session state; scale horizontally |
| **Connection Pooling** | HikariCP (default in Spring Boot) |
| **Resilience** | Retry, circuit breaker (Resilience4j) |
| **Caching** | Redis for frequently accessed data |
| **Async Processing** | `@Async` or message queue for long-running ops |
| **Load Balancing** | AWS ALB / K8s Service |
| **Observability** | Prometheus + Grafana integration |
| **CI/CD** | Gradle + Docker + AWS CodePipeline |

---

## 💾 Database & Transactions

- ACID transactions managed via `@Transactional`
- Optimistic locking using `` for booking concurrency
- Isolation levels configurable for critical operations
- Validation handled via `javax.validation` (`@NotNull`, `@Size`, etc.)

---

## 🧠 Cross-Cutting Concerns

| Concern | Implementation |
|----------|----------------|
| **Logging** | SLF4J + optional AOP logging |
| **Error Handling** | Global `@ControllerAdvice` |
| **Security** | JWT-based authentication with RBAC (role checks) and ABAC (attribute-based access control) |
| **Validation** | `@Valid` annotations + custom validators for input sanitization |
| **Transactions** | Managed by Spring’s `@Transactional` (supports ACID compliance) |
| **Observability** | Spring Boot Actuator for health, metrics, and tracing |
| **Entity Auditing** | Entities extend `AuditEntity` for automatic `createdBy`, `createdDate`, `modifiedBy`, and `modifiedDate` tracking |
| **Multi-Tenancy (ABAC Ready)** | Extend `AuditEntity` with `tenantId` or `partnerId` for attribute-based access control and tenant isolation |
| **Documentation** | OpenAPI / Swagger integration (optional) |

---

### 🧾 Entity Auditing

- All entities inherit from `AuditEntity`, which extends `BasicBaseEntity`.
- Automatically tracks:
    - `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`
- Powered by Spring Data JPA Auditing (`@CreatedBy`, `@LastModifiedBy`, etc.).
- Integrated with `AuditorAware` to populate user or tenant from Security Context (e.g., JWT claims).
- Provides a foundation for **ABAC enforcement** and **audit logging** across all data changes.


## 🧪 Testing Strategy

| Level | Framework | Scope |
|--------|------------|--------|
| **Unit Tests** | JUnit 5, Mockito | Services, Mappers |
| **Integration Tests** | Spring Boot Test | Repositories, Controllers |
| **Mocking** | Mockito | External dependencies |
| **Test Data** | H2 Database | Isolated, reproducible |

**Run tests:**
```bash
./gradlew test
```

---

## 🔮 Future Enhancements
- Integrate with **AWS API Gateway** for external exposure
- Add **Cognito authentication** for partner access
- Implement **distributed tracing** (OpenTelemetry + Zipkin)
- Introduce **Kafka-based async booking**
- Deploy on **EKS/ECS** with autoscaling policies

---

