CREATE TABLE "user"
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    google_id   VARCHAR(255) NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL UNIQUE,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100),
    picture_url VARCHAR(512),
    currency    VARCHAR(3)   NOT NULL DEFAULT 'MGA'
        CHECK (currency IN ('MGA', 'EUR', 'USD')),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);