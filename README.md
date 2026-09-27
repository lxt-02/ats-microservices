# Applicant Tracking System

## Load balancer and monitoring

The API Gateway routes `GET /api/users/ping` to `lb://user-service` through Eureka. Run multiple `user-service` replicas with Docker Compose scaling, then Prometheus runs from Docker and scrapes Spring Boot actuator metrics. Grafana loads a ready-made dashboard.

Start the stack with two `user-service` instances:

```powershell
docker compose up --build --scale user-service=2
```

Useful URLs:

- API Gateway: http://localhost:8080/api/users/ping
- Eureka: http://localhost:8761
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000

Grafana login is `admin` / `admin`. Open the `ATS / ATS Overview` dashboard.

To test load balancing, call the gateway several times and compare the `hostname` or `instanceId` in the response:

```powershell
1..10 | ForEach-Object { Invoke-RestMethod http://localhost:8080/api/users/ping }
```

To run a different number of `user-service` replicas:

```powershell
docker compose up --build --scale user-service=3
```
