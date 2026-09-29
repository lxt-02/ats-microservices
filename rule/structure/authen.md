# OAuth2 + API Gateway Trusted Header Architecture

## 1. Mục tiêu

Hệ thống sử dụng:

- Google OAuth2 để đăng nhập.
- User Service chịu trách nhiệm xử lý identity và cấp token của hệ thống.
- API Gateway chịu trách nhiệm xác thực access token.
- API Gateway chuyển thông tin người dùng xuống các microservice bằng Trusted Headers.
- Downstream services không cần phụ thuộc trực tiếp vào Google OAuth2.
- Không sử dụng Google Access Token làm token chung cho các microservice.
- Không tạo thêm một "Gateway Token" riêng ở giai đoạn hiện tại.

Kiến trúc tổng thể:

```text
Google OAuth2
      │
      ▼
API Gateway
      │
      ▼
User Service / Auth
      │
      ├── Verify Google identity
      ├── Find/Create local user
      ├── Link Google SSO account
      ├── Issue ATS Access Token
      └── Issue Refresh Token
               │
               ▼
             Client
```

Các request sau khi login:

```text
Client
  │
  │ Authorization: Bearer <ATS_ACCESS_TOKEN>
  ▼
API Gateway
  │
  ├── Validate JWT
  ├── Extract user identity
  ├── Remove untrusted identity headers
  ├── Inject trusted headers
  │
  ▼
Downstream Microservice
```

---

# 2. Phân chia trách nhiệm

## 2.1 Google

Google chỉ đóng vai trò External Identity Provider.

Google có trách nhiệm:

```text
Authenticate Google Account
        ↓
Return Google identity
```

Ví dụ thông tin nhận được:

```json
{
  "sub": "google-user-id",
  "email": "user@gmail.com",
  "name": "Nguyen Van A"
}
```

Không sử dụng Google Access Token làm authentication token cho toàn bộ hệ thống ATS.

Sau khi Google xác nhận identity, hệ thống phải chuyển identity đó thành local identity.

---

# 2.2 User Service

User Service hiện tại đóng vai trò:

```text
User Management
+
Authentication / Identity
```

User Service chịu trách nhiệm:

- Google OAuth2 login.
- Xử lý OAuth2 callback.
- Verify Google identity.
- Tìm local user tương ứng.
- Tạo user nếu business rule cho phép.
- Link Google account với local user.
- Kiểm tra trạng thái user.
- Load roles/permissions của user.
- Cấp ATS Access Token.
- Cấp Refresh Token.
- Refresh access token.
- Logout/revoke refresh token nếu hệ thống hỗ trợ.

Ví dụ:

```text
Google identity

email = user@gmail.com
sub   = 109238192381
```

Map sang:

```text
ATS User

id     = 125
email  = user@gmail.com
role   = RECRUITER
status = ACTIVE
```

Sau đó cấp ATS JWT:

```json
{
  "sub": "125",
  "roles": ["RECRUITER"],
  "iss": "ats-auth-service",
  "iat": 1790680000,
  "exp": 1790680900
}
```

`sub` nên sử dụng internal User ID chứ không sử dụng Google ID.

---

# 3. Access Token và Refresh Token

## Access Token

Access Token:

- JWT.
- Lifetime ngắn.
- Ví dụ 10–15 phút.
- Dùng để gọi API.
- Được API Gateway validate.

Ví dụ:

```http
Authorization: Bearer eyJhbGciOi...
```

JWT chỉ nên chứa những claim thực sự cần cho authentication/authorization:

```json
{
  "sub": "125",
  "roles": ["RECRUITER"],
  "iss": "ats-auth-service"
}
```

Không nhét quá nhiều dữ liệu profile vào JWT.

Ví dụ không nên đưa:

```text
phone
avatar
dateOfBirth
address
departmentName
...
```

nếu downstream không thực sự cần.

---

## Refresh Token

Refresh Token:

- Lifetime dài hơn Access Token.
- Ví dụ 7–30 ngày.
- Không dùng để gọi business API.
- Chỉ dùng để lấy Access Token mới.

Ưu tiên lưu phía client bằng:

```text
HttpOnly Cookie
Secure
SameSite
```

Refresh Token cần được quản lý bởi User/Auth Service.

---

# 4. Vai trò của API Gateway

API Gateway KHÔNG phải nơi quản lý user.

API Gateway cũng KHÔNG phải nơi xử lý business authorization.

Gateway đóng vai trò Edge Authentication / Policy Enforcement Point.

Nhiệm vụ:

```text
Routing
Load Balancing
Rate Limiting
Authentication
Basic Authorization
Trusted Header Injection
```

Request:

```http
GET /api/jobs

Authorization: Bearer <ATS_ACCESS_TOKEN>
```

Gateway thực hiện:

```text
1. Đọc Bearer Token
2. Validate JWT signature
3. Validate expiration
4. Validate issuer
5. Validate các claim cần thiết
6. Extract userId
7. Extract roles
8. Remove identity headers do client gửi
9. Inject Trusted Headers
10. Route request xuống service
```

---

# 5. Trusted Header Pattern

Sau khi JWT hợp lệ, Gateway convert authentication context thành internal headers.

Ví dụ JWT:

```json
{
  "sub": "125",
  "roles": ["RECRUITER"]
}
```

Gateway inject:

```http
X-User-Id: 125
X-User-Roles: RECRUITER
```

Nếu hệ thống multi-tenant có thể thêm:

```http
X-Tenant-Id: 10
```

Không truyền thêm thông tin nếu downstream không thực sự cần.

Downstream service có thể lấy:

```java
request.getHeader("X-User-Id");
```

hoặc xây dựng một `CurrentUser` / `RequestContext`.

Ví dụ:

```java
public record CurrentUser(
    Long userId,
    Set<String> roles
) {}
```

---

# 6. Quy tắc bảo mật quan trọng nhất của Trusted Header

Client KHÔNG được quyền tự quyết định:

```http
X-User-Id
X-User-Roles
X-Tenant-Id
```

Ví dụ attacker gửi:

```http
Authorization: Bearer <normal-user-token>

X-User-Id: 1
X-User-Roles: ADMIN
```

Gateway tuyệt đối không được forward những giá trị này.

Gateway phải:

```text
Incoming Request
       │
       ├── REMOVE X-User-Id
       ├── REMOVE X-User-Roles
       ├── REMOVE X-Tenant-Id
       │
       ▼
Validate JWT
       │
       ▼
Generate trusted values
       │
       ├── X-User-Id = JWT.sub
       ├── X-User-Roles = JWT.roles
       └── X-Tenant-Id = trusted claim nếu có
```

Rule:

> Tất cả identity headers từ client đều được coi là untrusted.

Gateway phải luôn remove rồi tự inject lại.

---

# 7. Downstream services phải private

Trusted Header chỉ an toàn khi client không thể bypass Gateway.

Không được thiết kế:

```text
Internet
   │
   ├── API Gateway :8080
   ├── User Service :8081
   ├── Job Service :8082
   └── Application Service :8083
```

Vì attacker có thể gọi:

```http
GET http://job-service:8082/api/jobs

X-User-Id: 1
X-User-Roles: ADMIN
```

mà không cần Gateway.

Kiến trúc phải là:

```text
                     Internet
                        │
                        ▼
                 ┌─────────────┐
                 │ API Gateway │
                 └──────┬──────┘
                        │
              Private Network
                        │
          ┌─────────────┼──────────────┐
          ▼             ▼              ▼
     User Service   Job Service   Application Service
```

Chỉ Gateway được expose ra ngoài.

Các business services chỉ hoạt động trong internal Docker/Kubernetes/network.

---

# 8. Gateway Authentication vs Business Authorization

Gateway chịu trách nhiệm trả lời:

```text
WHO ARE YOU?
```

Ví dụ:

```text
JWT hợp lệ không?

User ID là gì?

Role là gì?
```

Gateway có thể thực hiện coarse-grained authorization.

Ví dụ:

```text
/api/admin/**

requires ADMIN
```

Nhưng Gateway KHÔNG xử lý business authorization phức tạp.

Ví dụ:

```http
PUT /jobs/100
```

Gateway biết:

```text
userId = 125
role   = RECRUITER
```

Nhưng Gateway không nên query database để xác định:

```text
Job 100 có thuộc recruiter 125 hay không?
```

Việc đó thuộc Job Service.

Ví dụ:

```java
if (!job.isOwnedBy(currentUser.userId())) {
    throw new ForbiddenException();
}
```

Phân chia:

```text
Authentication
       │
       ▼
API Gateway

Basic / Route Authorization
       │
       ▼
API Gateway

Business Authorization
       │
       ▼
Downstream Service
```

---

# 9. Google OAuth2 Login Flow

Flow mong muốn:

```text
Frontend
   │
   │ Login with Google
   ▼
API Gateway
   │
   │ Route OAuth endpoint
   ▼
User Service
   │
   ▼
Google
```

Sau khi user login Google:

```text
Google
   │
   │ Authorization Code
   ▼
API Gateway
   │
   │ route callback
   ▼
User Service
```

User Service:

```text
Authorization Code
       │
       ▼
Exchange code with Google
       │
       ▼
Receive Google identity
       │
       ▼
Verify identity
       │
       ▼
Find/Create local User
       │
       ▼
Load roles
       │
       ▼
Generate ATS Access Token
       │
       ├── Access Token
       └── Refresh Token
```

Google authentication kết thúc tại đây.

Các business API phía sau chỉ làm việc với ATS identity.

---

# 10. Request Flow sau khi login

Ví dụ client gọi:

```http
GET /api/jobs

Authorization: Bearer <ATS_ACCESS_TOKEN>
```

Flow:

```text
Client
   │
   │ ATS JWT
   ▼
API Gateway
   │
   ├── validate JWT
   ├── extract sub = 125
   ├── extract roles = RECRUITER
   │
   ├── remove X-User-Id
   ├── remove X-User-Roles
   │
   ├── add X-User-Id: 125
   ├── add X-User-Roles: RECRUITER
   │
   ▼
Job Service
   │
   ├── create CurrentUser
   │
   └── execute business authorization
```

---

# 11. Không implement Gateway Token thứ hai

Hiện tại KHÔNG implement flow:

```text
ATS JWT
   ↓
Gateway
   ↓
Generate another Gateway JWT
   ↓
Downstream Service
```

Token exchange/internal service token là pattern hợp lệ nhưng chưa cần thiết cho project hiện tại.

Nó làm tăng complexity:

```text
multiple issuers
multiple audiences
key management
key rotation
token exchange
service identity
internal token expiration
mTLS
```

Hiện tại sử dụng:

```text
ATS JWT
   ↓
Gateway validation
   ↓
Trusted Headers
   ↓
Private Downstream Services
```

là đủ.

---

# 12. Desired Package Structure

## API Gateway

Có thể tổ chức:

```text
api-gateway
└── security
    ├── JwtAuthenticationFilter
    ├── JwtTokenValidator
    ├── TrustedHeaderFilter
    ├── SecurityProperties
    └── SecurityConfig
```

Hoặc GlobalFilter:

```text
security
├── AuthenticationGlobalFilter
├── JwtValidator
├── AuthenticatedUser
└── TrustedHeaderNames
```

Gateway cần xử lý:

```text
Bearer Token
    ↓
JwtValidator
    ↓
AuthenticatedUser
    ↓
TrustedHeaderFilter
    ↓
Downstream
```

---

# 13. User Service Structure

User Service nên tách authentication logic khỏi user domain càng nhiều càng tốt.

Ví dụ:

```text
user-service
├── domain
│   └── user
│
├── application
│   ├── auth
│   │   ├── LoginWithGoogleUseCase
│   │   ├── RefreshTokenUseCase
│   │   └── LogoutUseCase
│   │
│   └── user
│
├── infrastructure
│   ├── oauth2
│   │   └── GoogleOAuth2Client
│   │
│   └── security
│       ├── JwtTokenProvider
│       └── RefreshTokenRepository
│
└── presentation
    └── auth
```

Nếu project phát triển lớn hơn, có thể tách authentication thành:

```text
Identity Service
```

sau này.

Hiện tại chưa bắt buộc.

---

# 14. Downstream Service Structure

Không để controller tự đọc raw header ở mọi nơi.

Nên tạo abstraction chung:

```text
HTTP Headers
      ↓
CurrentUserResolver
      ↓
CurrentUser
      ↓
Application / Domain logic
```

Ví dụ:

```java
public record CurrentUser(
    Long userId,
    Set<String> roles
) {

    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
```

Controller:

```java
@GetMapping
public ResponseEntity<?> getJobs(CurrentUser currentUser) {
    ...
}
```

Business service không cần biết:

```text
JWT
OAuth2
Google
API Gateway
HTTP headers
```

Nó chỉ cần biết:

```text
currentUser.userId()
currentUser.roles()
```

---

# 15. Security Rules

Phải tuân thủ các rule sau:

1. Google token không được sử dụng làm system-wide token.

2. ATS Access Token phải được hệ thống tự phát hành.

3. API Gateway phải validate Access Token.

4. API Gateway phải remove identity headers từ incoming request.

5. Gateway chỉ inject trusted headers sau khi authentication thành công.

6. Downstream services không được public ra Internet.

7. Downstream không được tin identity header từ nguồn ngoài Gateway.

8. Business authorization vẫn phải nằm trong từng service.

9. Gateway không query business database để kiểm tra ownership.

10. Không tạo thêm Gateway Token/Internal JWT ở phiên bản hiện tại.

11. Không truyền Google Access Token xuống business services.

12. Không nhét toàn bộ user profile vào JWT hoặc headers.

---

# 16. Target Architecture

```text
                         ┌─────────────┐
                         │   GOOGLE    │
                         └──────▲──────┘
                                │
                             OAuth2
                                │
                                ▼
┌──────────┐             ┌─────────────┐
│ Frontend │────────────▶│ API Gateway │
└──────────┘             └──────┬──────┘
                                │
                                ▼
                         ┌──────────────┐
                         │ User Service │
                         │              │
                         │ OAuth2       │
                         │ Local User   │
                         │ JWT Issuer   │
                         │ RefreshToken │
                         └──────────────┘
```

Sau authentication:

```text
┌──────────┐
│ Frontend │
└────┬─────┘
     │
     │ ATS Access Token
     ▼
┌────────────────────────────┐
│         API Gateway        │
│                            │
│ JWT Validation             │
│ Rate Limiting              │
│ Routing                    │
│ Load Balancing             │
│ Trusted Header Injection   │
└─────────────┬──────────────┘
              │
              │ X-User-Id
              │ X-User-Roles
              │
       Private Network
              │
     ┌────────┼─────────────┐
     ▼        ▼             ▼
   User      Job       Application
  Service   Service       Service
```

---

# 17. Implementation Tasks

Implement theo thứ tự sau.

### Phase 1 — User Service Authentication

Implement:

```text
Google OAuth2 login
OAuth2 callback
Google identity mapping
Local user lookup
SSO account linking
ATS JWT generation
Refresh Token
Refresh Access Token endpoint
Logout/revoke nếu cần
```

---

### Phase 2 — API Gateway Authentication

Implement:

```text
JWT validation
JWT signature validation
expiration validation
issuer validation
extract sub
extract roles
```

Public endpoints phải bypass authentication:

```text
/oauth2/**
/login/**
/auth/refresh
actuator endpoints cần thiết
```

Các business API còn lại phải authenticated.

---

### Phase 3 — Trusted Header

Gateway phải remove:

```text
X-User-Id
X-User-Roles
X-Tenant-Id
```

trước khi xử lý.

Sau khi JWT hợp lệ:

```text
X-User-Id    <- JWT.sub
X-User-Roles <- JWT.roles
```

Nếu có tenant:

```text
X-Tenant-Id <- trusted JWT claim
```

Sau đó forward request xuống downstream.

---

### Phase 4 — Downstream Authentication Context

Tạo reusable mechanism để convert headers:

```text
X-User-Id
X-User-Roles
```

thành:

```java
CurrentUser
```

Không để business/application layer phụ thuộc trực tiếp vào HTTP header.

---

### Phase 5 — Infrastructure Security

Docker Compose / deployment phải bảo đảm:

```text
API Gateway
    ports:
      - "8080:8080"
```

Business service không expose host port nếu không cần.

Ví dụ:

```text
Job Service
User Service
Application Service
```

chỉ giao tiếp bằng internal Docker network/service name.

---

# 18. Acceptance Criteria

Implementation được coi là đúng khi test được các case sau.

### Case 1 — Valid JWT

```text
Client JWT
    ↓
Gateway validates
    ↓
X-User-Id injected
    ↓
Service receives correct User ID
```

---

### Case 2 — Invalid JWT

```text
Invalid JWT
    ↓
Gateway
    ↓
401 Unauthorized
```

Request không được tới downstream.

---

### Case 3 — Expired JWT

```text
Expired JWT
    ↓
Gateway
    ↓
401 Unauthorized
```

---

### Case 4 — Header Spoofing

Client gửi:

```http
X-User-Id: 1
X-User-Roles: ADMIN
```

nhưng JWT:

```json
{
  "sub": "125",
  "roles": ["RECRUITER"]
}
```

Downstream bắt buộc nhận:

```http
X-User-Id: 125
X-User-Roles: RECRUITER
```

Không được nhận giá trị do client gửi.

---

### Case 5 — Business Authorization

Recruiter 125 gọi:

```text
PUT /jobs/100
```

Nếu Job 100 thuộc recruiter 200:

```text
Gateway authentication: SUCCESS

Job Service authorization: DENIED
```

Response:

```text
403 Forbidden
```

---

# Final Architecture Decision

Sử dụng kiến trúc:

```text
Google OAuth2
       ↓
User Service authenticates external identity
       ↓
User Service issues ATS Access + Refresh Token
       ↓
Client sends ATS Access Token
       ↓
API Gateway validates ATS JWT
       ↓
API Gateway strips untrusted identity headers
       ↓
API Gateway injects Trusted Headers
       ↓
Private downstream services
       ↓
Business authorization inside each service
```

Không sử dụng:

```text
Google Token → tất cả microservices
```

và chưa sử dụng:

```text
ATS Token → Gateway → Internal Gateway Token → Service
```

ở phiên bản hiện tại.