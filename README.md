# xyz-booking-service
🎬 Booking Service

A Spring Boot 3.5.6 microservice for managing movie shows, theatres, and bookings.
Implements modern engineering standards including RBAC, ABAC, observability, and scalability best practices.

⚙️ Tech Stack
Category	Technology
Language	Java 21
Framework	Spring Boot 3.5.6
ORM	Hibernate / JPA
Build Tool	Gradle
Database	H2 (Dev)
Mapper	MapStruct
Logging	SLF4J / Logback
Testing	JUnit 5, Mockito
Observability	Spring Boot Actuator
🚀 Build & Run
1. Prerequisites

Java 21+

Gradle 8+

(Optional) Docker & Docker Compose

2. Build the project
   ./gradlew clean build

3. Run the service locally
   ./gradlew bootRun


The application starts at http://localhost:8080.
Actuator endpoint: http://localhost:8080/actuator/health

🧩 Core Modules & Layers
Layer	Responsibility
Controller	REST endpoints; request validation; RBAC authorization
Service	Business logic; ABAC checks; transaction management
Repository	Database access using Spring Data JPA
Mapper	Entity ↔ DTO conversion using MapStruct
Exception Handling	Centralized via @ControllerAdvice
Observability	Managed via Spring Boot Actuator
🔐 Security
1. RBAC (Role-Based Access Control)

Applied at the controller layer using @PreAuthorize.

Roles: ADMIN, THEATRE_OWNER, CUSTOMER.

Example:

@PreAuthorize("hasRole('THEATRE_OWNER')")
public ResponseEntity<ShowDTO> createShow(...) { ... }

2. ABAC (Attribute-Based Access Control)

Enforced at the service layer.

Example:

if (!authenticatedTenantId.equals(show.getTheatre().getTenantId())) {
throw new AccessDeniedException("Unauthorized theatre access");
}


Attributes like tenantId are extracted from JWT claims (issued by Cognito/Auth0).

3. Integration with API Gateway (AWS API Gateway + Cognito)

JWT tokens validated at the gateway.

Claims (roles, tenantId, userId) forwarded in headers.

Spring Security parses headers → builds Authentication context.

4. OWASP Top 10 Considerations
   Concern	Mitigation
   Injection	Use JPA parameter binding, input validation
   Broken Auth	Use JWT with short expiry; refresh tokens
   Sensitive Data Exposure	Mask logs; HTTPS; never log credentials
   XML/XXE	Disable XML parsing (use JSON only)
   Broken Access Control	RBAC + ABAC enforcement
   Security Misconfig	Spring Boot auto-hardening + profiles
   XSS	Jackson/Thymeleaf escaping (if UI added)
   CSRF	Disabled for stateless APIs
   Using Components with Vulnerabilities	Dependabot + Gradle dependency updates
   Insufficient Logging	SLF4J structured logs + centralized logging support
   🩺 Observability & Health
   ✅ Spring Boot Actuator

Provides runtime insights and metrics:

Endpoint	Purpose
/actuator/health	Liveness & readiness probe for K8s/Docker
/actuator/info	App metadata (version, description)
/actuator/metrics	JVM, memory, and HTTP request stats
/actuator/loggers	Runtime log-level management
🧠 Health Check Integration

Docker or Kubernetes can use:

livenessProbe:
httpGet:
path: /actuator/health/liveness
port: 8080
readinessProbe:
httpGet:
path: /actuator/health/readiness
port: 8080

🐳 Dockerization (Optional)

Add a Dockerfile:

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY build/libs/booking-service-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]


Build and run:

docker build -t booking-service .
docker run -p 8080:8080 booking-service

🧱 Scalability & Reliability
Concern	Practice
Stateless Services	Store sessionless state; enables horizontal scaling
Database Connection Pooling	Use HikariCP (Spring Boot default)
Resilience	Add retry, circuit breaker (Resilience4j)
Caching	Introduce Redis for frequently accessed data
Async Processing	Use @Async or message queue for long tasks
Load Balancing	AWS ALB / Kubernetes Service
Observability	Centralized logs + metrics via Prometheus & Grafana
CI/CD	Automated builds with Gradle + Docker + AWS CodePipeline
💾 Database & Transactions

ACID transactions handled by Spring’s @Transactional.

Optimistic locking with @Version (for booking concurrency).

Isolation level configurable via @Transactional(isolation = Isolation.SERIALIZABLE) for critical ops.

Data validation via javax.validation (@NotNull, @Size, etc.).

🧠 Cross-Cutting Concerns
Concern	Implementation
Logging	SLF4J + AOP (optional)
Error Handling	Global exception handler (@ControllerAdvice)
Security	JWT validation + RBAC + ABAC
Validation	@Valid annotations + custom validators
Transactions	Spring-managed via @Transactional
Observability	Spring Boot Actuator
Documentation	OpenAPI/Swagger (optional)
🧪 Testing Strategy
Level	Framework	Scope
Unit Tests	JUnit 5, Mockito	Services, Mappers
Integration Tests	Spring Boot Test	Repositories, Controllers
Mocking	Mockito	External deps
Test Data	H2 Database	Isolated, reproducible tests

Run tests:

./gradlew test
 
#Future Enhancements

Integrate with AWS API Gateway for external exposure

Add Cognito authentication for user and partner access

Implement distributed tracing (OpenTelemetry + Zipkin)

Introduce Kafka-based async seat booking

Deploy on EKS or ECS with autoscaling policies
