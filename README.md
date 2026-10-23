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
│   ├── security/    # CurrentUser, @RequiresRole, Roles, SecureTokens, HtmlSanitizer
│   ├── validation/  # @AllowedSortKeys
│   └── infrastructure/{time,mybatis,async}
├── organization/    # departments                — công bố: DepartmentDirectory
├── user/            # users                      — công bố: UserAccountContract, UserResponse
├── auth/            # refresh_tokens, sso_configurations: Google login, token, SSO, filter JWT
├── job/             # jobs, job_skills, skills: tin tuyển dụng, duyệt, Career Site
│                    #                            — công bố: JobDirectory, SkillCatalog, JobApplicationCounter (SPI)
└── candidate/       # candidates, candidate_skills, cvs, applications: nộp đơn, CV, parse, tìm kiếm
                     #                            — công bố: event ApplicationSubmittedEvent
```

Các bảng ít quan trọng được gộp vào module dùng chúng nhiều nhất thay vì tách module riêng:
`skills` nằm trong `job`; `cvs`, `candidate_skills`, `applications` nằm trong `candidate`.

Mỗi module chia 4 tầng: `api` → `application` → `domain` ← `infrastructure`.

- `domain`: Java thuần (aggregate, value object, enum, `<Entity>RuleViolation`, interface repository).
- `application`: mỗi use case một class với `execute(...)`, `@Transactional` đặt ở đây; `contract/` là phần module khác được gọi.
- `api`: controller, `*Request.toCommand()`, bọc kết quả vào `ApiResponse`.
- `infrastructure`: `*Record` ↔ aggregate qua `*PersistenceConverter`, `*MyBatisMapper` + XML ở `resources/mapper/<module>/`.

Chiều phụ thuộc giữa module: `candidate → job → user → organization`, `auth → user`, tất cả → `shared`.
`job` cần số đơn ứng tuyển nhưng không phụ thuộc `candidate`: nó khai báo interface `JobApplicationCounter`,
`candidate` cài đặt interface đó.
`ModularityTests` (Spring Modulith) fail nếu module nào dùng lớp nội bộ của module khác hoặc tạo vòng phụ thuộc,
và sinh sơ đồ PlantUML vào `target/spring-modulith-docs/`.

## Xác thực

Không còn API Gateway. `auth.infrastructure.security.JwtAuthenticationFilter` kiểm tra
`Authorization: Bearer <ATS access token>` cho mọi `/api/**`, trừ `ats.security.public-paths`
(`/api/v1/auth/google/`, `/api/v1/auth/refresh`, `/api/v1/auth/logout`, `/api/v1/public/`). Token hợp lệ thành
`CurrentUser`; controller nhận `CurrentUser` làm tham số. Header `X-User-Id` / `X-User-Roles` từ client bị bỏ qua.

Phân quyền 2 lớp:
- Theo màn hình: `@RequiresRole` trên controller (theo bảng Screen Authorization của SRS) → 403.
- Theo dữ liệu, trong use case: recruiter chỉ thấy/sửa job được giao và ứng viên của các job đó hoặc do mình import
  (BRULE-01); hiring manager chỉ duyệt job mình phụ trách (BRULE-05/11); admin thấy tất cả.

## API chính

| Method | Path | Ai dùng |
|---|---|---|
| GET | `/api/v1/public/jobs?keyword=&departmentId=&location=&employmentType=&sort=MOST_RECENT&page=&size=` | Công khai |
| GET | `/api/v1/public/jobs/{id}`, `/api/v1/public/jobs/filters` | Công khai |
| POST | `/api/v1/public/jobs/{id}/applications` (multipart: fullName, email, phone, cv, coverLetter, source, referralCode, utm*) | Công khai |
| GET/POST | `/api/v1/jobs`, `/api/v1/jobs/stats`, `/api/v1/jobs/{id}` (GET/PUT/DELETE) | Recruiter, Admin (HM xem) |
| POST | `/api/v1/jobs/{id}/submit` `publish` `unpublish` `close` `reopen`, `/api/v1/jobs/bulk-actions` | Recruiter, Admin |
| POST | `/api/v1/jobs/{id}/approve`, `/api/v1/jobs/{id}/reject` | Hiring Manager của job |
| GET/POST | `/api/v1/skills` | Đọc: nội bộ; tạo: Admin |
| GET | `/api/v1/candidates?keyword=&skillIds=&jobId=&source=&parseStatus=&duplicate=` | Recruiter, Admin |
| GET/PUT | `/api/v1/candidates/{id}` | Recruiter, Admin (HM xem) |
| POST | `/api/v1/candidates/import` (multipart `files`, `jobId` tuỳ chọn) | Recruiter, Admin |
| GET/POST | `/api/v1/candidates/{id}/cvs/{cvId}/file`, `.../parse` | Recruiter, Admin (HM tải) |
| GET | `/api/v1/applications?jobId=` | Recruiter/HM của job, Admin |
| GET | `/api/v1/users?role=HIRING_MANAGER&status=ACTIVE` | Admin, Recruiter |

Luồng job: `DRAFT` → submit → `PENDING_APPROVAL` (nếu cần duyệt) → HM approve → `DRAFT` sẵn sàng publish →
`PUBLISHED` → `CLOSED` (thủ công hoặc tự động 23:59 ngày hạn chót, giờ Việt Nam) → reopen → `DRAFT`.

CV: PDF/DOCX tối đa 5 MB (kiểm tra cả chữ ký file). File lưu ở `ATS_CV_DIRECTORY` (Docker: volume `cv-data`).
Sau khi lưu, CV được parse ở nền: họ tên, email, SĐT, kỹ năng (khớp với danh mục skill), học vấn, kinh nghiệm
(tiêu đề tiếng Anh hoặc tiếng Việt). Thiếu trường chính → `PARTIAL`; không đọc được → `FAILED`.

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
- `JobCandidateFlowIntegrationTest`: tạo job → duyệt → publish → Career Site → nộp đơn kèm CV → parse → tìm kiếm,
  import CV hàng loạt, phân quyền và cách ly dữ liệu giữa recruiter.
- Unit test: Google login, refresh token, JWT, trạng thái Job, lọc HTML, kiểm tra file CV, đọc PDF/DOCX, trích xuất CV.

Chạy test tích hợp trên PostgreSQL thật thay cho H2:

```powershell
mvn test "-Dspring.datasource.url=jdbc:postgresql://localhost:5432/ats_test" "-Dspring.datasource.username=ats" "-Dspring.datasource.password=ats" "-Dspring.datasource.driver-class-name=org.postgresql.Driver"
```

## Thêm module mới (ví dụ `interview`)

1. Tạo `com.ats.interview` với `package-info.java` có `@ApplicationModule(displayName = "Interview")`.
2. Chỉ tạo các package con cần dùng (`api/`, `application/`, `domain/`, `infrastructure/`).
3. Bảng mới đặt trong migration `db/migration/V00N__...sql`; chỉ module đó đọc/ghi bảng đó.
4. Cần dữ liệu module khác thì gọi `application.contract` của module đó; nếu chưa có, thêm interface vào
   contract của module kia và đánh dấu package bằng `@NamedInterface`.
