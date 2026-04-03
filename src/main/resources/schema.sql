-- ============================================================
-- Finance Dashboard — reference DDL (MySQL-style)
-- ============================================================
-- With H2, Hibernate (ddl-auto=update) manages schema; this file is not
-- applied when spring.sql.init.mode=never. Kept for documentation only.
-- ============================================================

-- MySQL only (not used with H2):
-- CREATE DATABASE IF NOT EXISTS finance_dashboard ...

-- ============================================================
-- Table: users
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id         BIGINT          NOT NULL AUTO_INCREMENT,
    name       VARCHAR(100)    NOT NULL,
    email      VARCHAR(150)    NOT NULL,
    password   VARCHAR(255)    NOT NULL,   -- BCrypt hash
    role       ENUM('VIEWER', 'ANALYST', 'ADMIN') NOT NULL DEFAULT 'VIEWER',
    status     ENUM('ACTIVE', 'INACTIVE')         NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME        NOT NULL,
    updated_at DATETIME,

    PRIMARY KEY (id),
    UNIQUE INDEX idx_users_email  (email),
    INDEX        idx_users_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Table: financial_records
-- ============================================================
CREATE TABLE IF NOT EXISTS financial_records (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    amount      DECIMAL(19, 4)  NOT NULL,
    type        ENUM('INCOME', 'EXPENSE') NOT NULL,
    category    VARCHAR(100)    NOT NULL,
    date        DATE            NOT NULL,
    notes       VARCHAR(500),
    created_by  BIGINT          NOT NULL,  -- FK → users.id
    deleted     TINYINT(1)      NOT NULL DEFAULT 0,
    created_at  DATETIME        NOT NULL,
    updated_at  DATETIME,

    PRIMARY KEY (id),
    INDEX idx_fr_type       (type),
    INDEX idx_fr_category   (category),
    INDEX idx_fr_date       (date),
    INDEX idx_fr_created_by (created_by),
    INDEX idx_fr_deleted    (deleted),

    CONSTRAINT fk_fr_created_by
        FOREIGN KEY (created_by) REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
