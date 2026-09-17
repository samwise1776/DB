PRAGMA foreign_keys = ON;

-- =========================
-- USERS
-- =========================

CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,

    username TEXT NOT NULL UNIQUE COLLATE NOCASE,
    email TEXT NOT NULL UNIQUE COLLATE NOCASE,

    -- NEVER store plain passwords here.
    password_hash TEXT NOT NULL,

    two_factor_enabled INTEGER NOT NULL DEFAULT 0
        CHECK (two_factor_enabled IN (0, 1)),

    is_verified INTEGER NOT NULL DEFAULT 0
        CHECK (is_verified IN (0, 1)),

    is_active INTEGER NOT NULL DEFAULT 1
        CHECK (is_active IN (0, 1)),

    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- 2FA / VERIFICATION CODES
-- =========================

CREATE TABLE IF NOT EXISTS auth_codes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,

    user_id INTEGER NOT NULL,

    -- Hash the code instead of storing the raw 6-digit code.
    code_hash TEXT NOT NULL,

    purpose TEXT NOT NULL
        CHECK (
            purpose IN (
                'email_verify',
                'login_2fa',
                'password_reset'
            )
        ),

    expires_at TEXT NOT NULL,

    used INTEGER NOT NULL DEFAULT 0
        CHECK (used IN (0, 1)),

    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- =========================
-- LOGIN SESSIONS
-- =========================

CREATE TABLE IF NOT EXISTS sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,

    user_id INTEGER NOT NULL,

    session_token_hash TEXT NOT NULL UNIQUE,

    expires_at TEXT NOT NULL,

    revoked INTEGER NOT NULL DEFAULT 0
        CHECK (revoked IN (0, 1)),

    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- =========================
-- INDEXES
-- =========================

CREATE INDEX IF NOT EXISTS idx_auth_codes_user_id
ON auth_codes(user_id);

CREATE INDEX IF NOT EXISTS idx_auth_codes_expires_at
ON auth_codes(expires_at);

CREATE INDEX IF NOT EXISTS idx_sessions_user_id
ON sessions(user_id);

CREATE INDEX IF NOT EXISTS idx_sessions_expires_at
ON sessions(expires_at);

-- =========================
-- AUTO UPDATE TIMESTAMP
-- =========================

CREATE TRIGGER IF NOT EXISTS users_updated_at
AFTER UPDATE ON users
FOR EACH ROW
BEGIN
    UPDATE users
    SET updated_at = CURRENT_TIMESTAMP
    WHERE id = OLD.id;
END;
