# Prometheus Config Template For Spring Boot Projects

This guide is a reusable template for adding Prometheus monitoring to a Spring Boot project, especially a Docker Compose microservices project.

## 1. Add Maven Dependencies

Add these dependencies to every Spring Boot service that should expose metrics:

```xml
<dependencies>
    <!-- Exposes actuator endpoints such as /actuator/health and /actuator/metrics -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <!-- Exposes Prometheus metrics at /actuator/prometheus -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>
</dependencies>
```

## 2. Configure Each Spring Boot Service

Add this to each service's `application.yaml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus

  endpoint:
    health:
      show-details: always

  metrics:
    tags:
      application: ${spring.application.name}
```

This enables:

- `/actuator/health` for health checks.
- `/actuator/metrics` for Spring Boot metrics.
- `/actuator/prometheus` for Prometheus scraping.
- A common `application` tag using the service name.

Optional: expose actuator on a separate port:

```yaml
management:
  server:
    port: 9091
```

If you use a separate actuator port, Prometheus must scrape that port instead of the main application port.

## 3. Create Prometheus Config

Create this file:

```text
infras/observability/prometheus/prometheus.yml
```

Template:

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: api-gateway
    metrics_path: /actuator/prometheus
    static_configs:
      - targets:
          - api-gateway:8080

  - job_name: service-a
    metrics_path: /actuator/prometheus
    static_configs:
      - targets:
          - service-a:8081

  - job_name: service-b
    metrics_path: /actuator/prometheus
    static_configs:
      - targets:
          - service-b:8082
```

Replace:

- `job_name` with a readable service name.
- `service-a`, `service-b`, `api-gateway` with Docker Compose service names.
- `8080`, `8081`, `8082` with the actual ports inside the Docker network.

## 4. Add Prometheus To Docker Compose

Add this service to `docker-compose.yml`:

```yaml
services:
  prometheus:
    image: prom/prometheus:v3.5.3
    container_name: prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./infras/observability/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml:ro
    networks:
      - app-network

networks:
  app-network:
    driver: bridge
```

All Spring Boot services that Prometheus scrapes must be on the same Docker network:

```yaml
services:
  service-a:
    build:
      context: .
      dockerfile: ./services/service-a/Dockerfile
    ports:
      - "8081:8081"
    networks:
      - app-network
```

## 5. Docker Target Rules

When Prometheus runs in Docker Compose, use Docker Compose service names as targets.

Correct:

```yaml
targets:
  - service-a:8081
```

Incorrect:

```yaml
targets:
  - localhost:8081
```

Inside the Prometheus container, `localhost` means the Prometheus container itself, not your Spring Boot service.

## 6. How To Verify

Start the project:

```bash
docker compose up -d
```

Check that each service exposes metrics:

```text
http://localhost:8081/actuator/prometheus
```

Open Prometheus:

```text
http://localhost:9090
```

Then go to:

```text
Status > Targets
```

Expected result:

- `UP`: Prometheus can scrape the service.
- `DOWN`: hostname, port, network, or actuator config is wrong.

## 7. Common Problems

### Target Is DOWN

Check:

- The target uses the Docker Compose service name, not `localhost`.
- Prometheus and the service are on the same Docker network.
- The service port is the internal container port.
- `/actuator/prometheus` is exposed in `application.yaml`.
- `micrometer-registry-prometheus` exists in `pom.xml`.

### Endpoint Returns 404

Usually one of these is missing:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: prometheus
```

or:

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

### Works Locally But Not In Docker

Do not use local URLs inside Prometheus config:

```yaml
targets:
  - localhost:8081
```

Use Docker service names:

```yaml
targets:
  - service-a:8081
```

## 8. Minimal Checklist

- Add `spring-boot-starter-actuator`.
- Add `micrometer-registry-prometheus`.
- Expose `health`, `metrics`, and `prometheus` in `application.yaml`.
- Create `prometheus.yml`.
- Mount `prometheus.yml` into the Prometheus container.
- Put Prometheus and services on the same Docker network.
- Check `http://localhost:9090` and `Status > Targets`.
