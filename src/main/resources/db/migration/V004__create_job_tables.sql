-- Job module: skills (master data), jobs, job_skills

CREATE TABLE skills (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    skill_name VARCHAR(150) NOT NULL,
    category VARCHAR(150),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX uq_skills_name ON skills (skill_name);

CREATE TABLE jobs (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    department_id BIGINT,
    recruiter_id BIGINT NOT NULL,
    hiring_manager_id BIGINT,

    title VARCHAR(200),
    description TEXT,
    employment_type VARCHAR(30) NOT NULL DEFAULT 'FULL_TIME',
    location VARCHAR(200),
    positions_count INTEGER NOT NULL DEFAULT 1,
    salary_min NUMERIC(15, 2),
    salary_max NUMERIC(15, 2),
    salary_currency VARCHAR(3) NOT NULL DEFAULT 'VND',
    deadline DATE,
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    requires_approval BOOLEAN NOT NULL DEFAULT TRUE,

    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    approved_by BIGINT,
    approved_at TIMESTAMPTZ,
    rejection_reason VARCHAR(1000),

    utm_source VARCHAR(150),
    utm_medium VARCHAR(150),
    published_at TIMESTAMPTZ,
    closed_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,

    CONSTRAINT fk_jobs_department FOREIGN KEY (department_id) REFERENCES departments (id),
    CONSTRAINT fk_jobs_recruiter FOREIGN KEY (recruiter_id) REFERENCES users (id),
    CONSTRAINT fk_jobs_hiring_manager FOREIGN KEY (hiring_manager_id) REFERENCES users (id),
    CONSTRAINT ck_jobs_salary_range CHECK (salary_min IS NULL OR salary_max IS NULL OR salary_max >= salary_min),
    CONSTRAINT ck_jobs_positions CHECK (positions_count BETWEEN 1 AND 99)
);

CREATE INDEX idx_jobs_status ON jobs (status);
CREATE INDEX idx_jobs_recruiter_id ON jobs (recruiter_id);
CREATE INDEX idx_jobs_hiring_manager_id ON jobs (hiring_manager_id);
CREATE INDEX idx_jobs_department_id ON jobs (department_id);
CREATE INDEX idx_jobs_published_at ON jobs (published_at);

CREATE TABLE job_skills (
    job_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,

    CONSTRAINT pk_job_skills PRIMARY KEY (job_id, skill_id),
    CONSTRAINT fk_job_skills_job FOREIGN KEY (job_id) REFERENCES jobs (id),
    CONSTRAINT fk_job_skills_skill FOREIGN KEY (skill_id) REFERENCES skills (id)
);

CREATE INDEX idx_job_skills_skill_id ON job_skills (skill_id);

INSERT INTO skills (skill_name, category, created_by, updated_by) VALUES
    ('Java', 'Programming Language', 'system', 'system'),
    ('Spring Boot', 'Framework', 'system', 'system'),
    ('PostgreSQL', 'Database', 'system', 'system'),
    ('MySQL', 'Database', 'system', 'system'),
    ('JavaScript', 'Programming Language', 'system', 'system'),
    ('TypeScript', 'Programming Language', 'system', 'system'),
    ('React', 'Framework', 'system', 'system'),
    ('Angular', 'Framework', 'system', 'system'),
    ('Node.js', 'Framework', 'system', 'system'),
    ('Python', 'Programming Language', 'system', 'system'),
    ('C#', 'Programming Language', 'system', 'system'),
    ('.NET', 'Framework', 'system', 'system'),
    ('Go', 'Programming Language', 'system', 'system'),
    ('Docker', 'DevOps', 'system', 'system'),
    ('Kubernetes', 'DevOps', 'system', 'system'),
    ('AWS', 'Cloud', 'system', 'system'),
    ('Microsoft Azure', 'Cloud', 'system', 'system'),
    ('Git', 'Tool', 'system', 'system'),
    ('SQL', 'Database', 'system', 'system'),
    ('HTML', 'Web', 'system', 'system'),
    ('CSS', 'Web', 'system', 'system'),
    ('Figma', 'Design', 'system', 'system'),
    ('Project Management', 'Management', 'system', 'system'),
    ('Agile', 'Methodology', 'system', 'system'),
    ('Scrum', 'Methodology', 'system', 'system'),
    ('Data Analysis', 'Analytics', 'system', 'system'),
    ('Machine Learning', 'Analytics', 'system', 'system'),
    ('Communication', 'Soft Skill', 'system', 'system'),
    ('English', 'Language', 'system', 'system'),
    ('Japanese', 'Language', 'system', 'system');
