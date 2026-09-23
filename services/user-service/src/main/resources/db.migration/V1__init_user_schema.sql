-- =========================================================
-- USER SERVICE DATABASE
-- Tables:
--   departments
--   users
--   sso_configurations
-- =========================================================

CREATE TABLE departments (
                             id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                             parent_id BIGINT,
                             department_name VARCHAR(255),
                             description TEXT,

                             created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             created_by VARCHAR(255),
                             is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
                             updated_by VARCHAR(255),
                             deleted_at TIMESTAMPTZ,

                             CONSTRAINT fk_departments_parent
                                 FOREIGN KEY (parent_id)
                                     REFERENCES departments(id)
);

CREATE TABLE users (
                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                       department_id BIGINT,

                       full_name VARCHAR(255),
                       email VARCHAR(255),
                       password_hash VARCHAR(255),
                       phone VARCHAR(30),

                       role VARCHAR(50),

                       sso_provider_id VARCHAR(255),

                       status VARCHAR(50),

                       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       created_by VARCHAR(255),
                       is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
                       updated_by VARCHAR(255),
                       deleted_at TIMESTAMPTZ,

                       CONSTRAINT fk_users_department
                           FOREIGN KEY (department_id)
                               REFERENCES departments(id)
);

CREATE TABLE sso_configurations (
                                    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                    provider_type VARCHAR(50),

                                    ldap_url VARCHAR(500),
                                    base_dn VARCHAR(500),
                                    bind_user VARCHAR(255),

                                    is_active BOOLEAN NOT NULL DEFAULT TRUE,

                                    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    created_by VARCHAR(255),
                                    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
                                    updated_by VARCHAR(255),
                                    deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_users_department_id
    ON users(department_id);

CREATE INDEX idx_users_email
    ON users(email);

CREATE INDEX idx_users_status
    ON users(status);

CREATE INDEX idx_departments_parent_id
    ON departments(parent_id);