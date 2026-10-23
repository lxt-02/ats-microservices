-- Candidate module, recruitment pipeline: Kanban stages, stage transitions (audit trail), evaluation notes,
-- transfer link and hiring manager decision on applications.

ALTER TABLE applications ADD COLUMN transferred_from_id BIGINT;
ALTER TABLE applications ADD COLUMN hm_approved BOOLEAN;
ALTER TABLE applications ADD COLUMN assigned_department_id BIGINT;
ALTER TABLE applications ADD COLUMN decision_comment VARCHAR(2000);
ALTER TABLE applications ADD COLUMN decided_by BIGINT;
ALTER TABLE applications ADD COLUMN decided_at TIMESTAMPTZ;
ALTER TABLE applications ADD COLUMN status_changed_at TIMESTAMPTZ;

UPDATE applications SET status_changed_at = applied_at WHERE status_changed_at IS NULL;

ALTER TABLE applications
    ADD CONSTRAINT fk_applications_transferred_from FOREIGN KEY (transferred_from_id) REFERENCES applications (id);
ALTER TABLE applications
    ADD CONSTRAINT fk_applications_assigned_department FOREIGN KEY (assigned_department_id) REFERENCES departments (id);
ALTER TABLE applications
    ADD CONSTRAINT fk_applications_decided_by FOREIGN KEY (decided_by) REFERENCES users (id);

CREATE INDEX idx_applications_job_status ON applications (job_id, status);

-- One Kanban column per pipeline state (BRULE-12); admins rename, reorder and recolour them (UC-04.3)
CREATE TABLE pipeline_stages (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    status_code VARCHAR(30) NOT NULL,
    stage_name VARCHAR(100) NOT NULL,
    stage_order INTEGER NOT NULL,
    color VARCHAR(30),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,

    CONSTRAINT uq_pipeline_stages_status UNIQUE (status_code)
);

INSERT INTO pipeline_stages (status_code, stage_name, stage_order, color, is_default, created_by, updated_by) VALUES
    ('NEW', 'New', 1, '#6366f1', TRUE, 'system', 'system'),
    ('CV_SCREENING', 'CV Reviewing', 2, '#0ea5e9', FALSE, 'system', 'system'),
    ('CV_PASSED', 'CV Passed', 3, '#14b8a6', FALSE, 'system', 'system'),
    ('TECHNICAL_INTERVIEW', 'Technical Interview', 4, '#8b5cf6', FALSE, 'system', 'system'),
    ('TECH_PASSED', 'Technical Passed', 5, '#22c55e', FALSE, 'system', 'system'),
    ('HM_INTERVIEW', 'HM Interview', 6, '#f59e0b', FALSE, 'system', 'system'),
    ('OFFERED', 'Offered', 7, '#3b82f6', FALSE, 'system', 'system'),
    ('HIRED', 'Hired', 8, '#059669', FALSE, 'system', 'system'),
    ('OFFER_DECLINED', 'Offer Declined', 9, '#94a3b8', FALSE, 'system', 'system'),
    ('TRANSFERRED', 'Transferred', 10, '#64748b', FALSE, 'system', 'system'),
    ('REJECTED', 'Rejected', 11, '#ef4444', FALSE, 'system', 'system');

-- Audit trail of every pipeline move (previous state, new state, who, when)
CREATE TABLE stage_transitions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    application_id BIGINT NOT NULL,
    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,
    notes VARCHAR(2000),
    moved_at TIMESTAMPTZ NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,

    CONSTRAINT fk_stage_transitions_application FOREIGN KEY (application_id) REFERENCES applications (id)
);

CREATE INDEX idx_stage_transitions_application_id ON stage_transitions (application_id);

-- Applications created before this migration get their initial "NEW" record
INSERT INTO stage_transitions (application_id, from_status, to_status, moved_at, created_by, updated_by)
SELECT id, NULL, status, applied_at, created_by, created_by FROM applications;

CREATE TABLE evaluation_notes (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    application_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    content TEXT NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,

    CONSTRAINT fk_evaluation_notes_application FOREIGN KEY (application_id) REFERENCES applications (id)
);

CREATE INDEX idx_evaluation_notes_application_id ON evaluation_notes (application_id);
