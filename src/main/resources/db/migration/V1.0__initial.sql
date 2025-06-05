CREATE TABLE notes
(
    uuid        UUID PRIMARY KEY,
    note        TEXT NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);