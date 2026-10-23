-- Candidate module: candidates, candidate_skills, cvs, applications

CREATE TABLE candidates (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id BIGINT,
    full_name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(30),
    phone_key VARCHAR(20),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    source VARCHAR(150),
    utm_source VARCHAR(150),
    utm_medium VARCHAR(150),
    utm_campaign VARCHAR(255),

    is_duplicate BOOLEAN NOT NULL DEFAULT FALSE,
    duplicate_of_id BIGINT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,

    CONSTRAINT fk_candidates_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_candidates_duplicate_of FOREIGN KEY (duplicate_of_id) REFERENCES candidates (id)
);

CREATE INDEX idx_candidates_email ON candidates (email);
CREATE INDEX idx_candidates_phone_key ON candidates (phone_key);
CREATE INDEX idx_candidates_created_by ON candidates (created_by);
CREATE INDEX idx_candidates_created_at ON candidates (created_at);

CREATE TABLE candidate_skills (
    candidate_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,

    CONSTRAINT pk_candidate_skills PRIMARY KEY (candidate_id, skill_id),
    CONSTRAINT fk_candidate_skills_candidate FOREIGN KEY (candidate_id) REFERENCES candidates (id),
    CONSTRAINT fk_candidate_skills_skill FOREIGN KEY (skill_id) REFERENCES skills (id)
);

CREATE INDEX idx_candidate_skills_skill_id ON candidate_skills (skill_id);

CREATE TABLE cvs (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    candidate_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    file_type VARCHAR(10) NOT NULL,
    size_bytes BIGINT NOT NULL,

    parse_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    parsed_data JSONB,
    content_text TEXT,
    parse_message VARCHAR(1000),
    parsed_at TIMESTAMPTZ,
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,

    CONSTRAINT fk_cvs_candidate FOREIGN KEY (candidate_id) REFERENCES candidates (id)
);

CREATE INDEX idx_cvs_candidate_id ON cvs (candidate_id);
CREATE INDEX idx_cvs_parse_status ON cvs (parse_status);

CREATE TABLE applications (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    job_id BIGINT NOT NULL,
    candidate_id BIGINT NOT NULL,
    cv_id BIGINT,
    status VARCHAR(30) NOT NULL DEFAULT 'NEW',

    cover_letter TEXT,
    source VARCHAR(50),
    referral_code VARCHAR(20),
    utm_source VARCHAR(150),
    utm_medium VARCHAR(150),
    utm_campaign VARCHAR(255),
    applied_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,

    -- BRULE-09: one application per candidate (email) and job
    CONSTRAINT uq_applications_job_candidate UNIQUE (job_id, candidate_id),
    CONSTRAINT fk_applications_job FOREIGN KEY (job_id) REFERENCES jobs (id),
    CONSTRAINT fk_applications_candidate FOREIGN KEY (candidate_id) REFERENCES candidates (id),
    CONSTRAINT fk_applications_cv FOREIGN KEY (cv_id) REFERENCES cvs (id)
);

CREATE INDEX idx_applications_candidate_id ON applications (candidate_id);
CREATE INDEX idx_applications_status ON applications (status);
