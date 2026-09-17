-- UnitDB schema
-- Recreates the core database structure from scratch.

PRAGMA foreign_keys = ON;

BEGIN TRANSACTION;

CREATE TABLE IF NOT EXISTS settings (
    key TEXT PRIMARY KEY,
    value TEXT
);

CREATE TABLE IF NOT EXISTS records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    type TEXT NOT NULL,
    name TEXT NOT NULL,
    data TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO settings (key, value)
VALUES
    ('theme', 'dark'),
    ('notifications', 'enabled'),
    ('language', 'en')
ON CONFLICT(key) DO NOTHING;

COMMIT;
