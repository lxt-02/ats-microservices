# Applicant Tracking System

Một ứng dụng Spring Boot duy nhất (modular monolith) theo kiến trúc DDD phân tầng.
Thay cho bộ cũ gồm `eureka-service` + `api-gateway` + `user-service`: giờ chỉ còn **1 jar + 1 PostgreSQL**.

Stack: Java 21, Spring Boot 4.1, MyBatis, Flyway, Spring Modulith, PostgreSQL.

## Chạy

```powershell
# Ứng dụng + PostgreSQL
docker compose up --build

# Kèm Prometheus + Grafana
docker compose --profile monitoring up --build
```

Sao chép `.env.example` thành `.env` để điền Google OAuth và `ATS_JWT_SECRET`.

Chạy từ IDE / Maven (cần PostgreSQL ở `localhost:5432`, DB `ats`, user/pass `ats`/`ats`):

```powershell
docker compose up -d db
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

| URL | |
|---|---|
| http://localhost:8080/api/v1/... | API |
| http://localhost:8080/actuator/health | Health |
| http://localhost:9090 | Prometheus (profile `monitoring`) |
| http://localhost:3000 | Grafana `admin`/`admin` (profile `monitoring`) |

## Module (bounded context)

```
com.ats
├── AtsApplication.java
├── shared/          # kỹ thuật dùng chung, không chứa nghiệp vụ (OPEN module)
│   ├── exception/   # ApplicationException, RuleViolationException, ResourceNotFoundException, ConflictException...
│   ├── web/         # ApiResponse, FieldErrorDetail, GlobalExceptionHandler, CurrentUserArgumentResolver
│   ├── security/    # CurrentUser, SecureTokens
│   └── infrastructure/{time,mybatis}
├── organization/    # Phòng ban          — công bố: application.contract.DepartmentDirectory
├── user/            # Tài khoản người dùng — công bố: application.contract.UserAccountContract,
│                    #                                 application.response.UserResponse
└── auth/            # Google login, access/refresh token, cấu hình SSO, filter xác thực JWT
```

Mỗi module chia 4 tầng: `api` → `application` → `domain` ← `infrastructure`.

- `domain`: Java thuần (aggregate, value object, enum, `<Entity>RuleViolation`, interface repository).
- `application`: mỗi use case một class với `execute(...)`, `@Transactional` đặt ở đây; `contract/` là phần module khác được gọi.
- `api`: controller, `*Request.toCommand()`, bọc kết quả vào `ApiResponse`.
- `infrastructure`: `*Record` ↔ aggregate qua `*PersistenceConverter`, `*MyBatisMapper` + XML ở `resources/mapper/<module>/`.

Chiều phụ thuộc giữa module: `auth → user → organization`, tất cả → `shared`.
`ModularityTests` (Spring Modulith) fail nếu module nào dùng lớp nội bộ của module khác hoặc tạo vòng phụ thuộc,
và sinh sơ đồ PlantUML vào `target/spring-modulith-docs/`.

## Xác thực

Không còn API Gateway. `auth.infrastructure.security.JwtAuthenticationFilter` kiểm tra
`Authorization: Bearer <ATS access token>` cho mọi `/api/**`, trừ `ats.security.public-paths`
(`/api/v1/auth/google/`, `/api/v1/auth/refresh`, `/api/v1/auth/logout`). Token hợp lệ thành `CurrentUser`;
controller nhận `CurrentUser` làm tham số. Header `X-User-Id` / `X-User-Roles` từ client bị bỏ qua.

## Response

```json
{ "result": "OK", "code": 200, "data": { ... } }
{ "result": "NG", "code": 400, "message": "Validation failed", "data": [ { "field": "email", "code": "Email", "message": "Email is invalid" } ] }
{ "result": "NG", "code": 409, "message": "Deleted user cannot be changed", "data": { "rule": "USER_R1" } }
```

| Lỗi | HTTP |
|---|---|
| Validate, `IllegalArgumentException`, `RuleViolationException(invalidInput=true)` | 400 |
| Thiếu/sai token, đăng nhập thất bại | 401 |
| `AccessDeniedException` | 403 |
| `ResourceNotFoundException`, sai đường dẫn | 404 |
| `ConflictException`, `BusinessRuleException`, `RuleViolationException(invalidInput=false)` | 409 |
| Google không phản hồi | 503 |

## Test

```powershell
mvn test
```

- `ModularityTests`: kiểm tra ranh giới module.
- `ApiIntegrationTest`: toàn bộ app qua HTTP (filter, controller, MyBatis, Flyway) trên H2 chế độ PostgreSQL.
- Unit test cho luồng Google login, xoay vòng refresh token, ký/kiểm tra JWT.

## Thêm module mới (ví dụ `job`)

1. Tạo `com.ats.job` với `package-info.java` có `@ApplicationModule(displayName = "Job")`.
2. Chỉ tạo các package con cần dùng (`api/`, `application/`, `domain/`, `infrastructure/`).
3. Bảng mới đặt trong migration `db/migration/V00N__...sql`; chỉ module `job` đọc/ghi bảng đó.
4. Cần dữ liệu module khác thì gọi `application.contract` của module đó; nếu chưa có, thêm interface vào
   contract của module kia và đánh dấu package bằng `@NamedInterface`.
