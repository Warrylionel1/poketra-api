CREATE TABLE account
(
    id              UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    user_id         UUID           NOT NULL REFERENCES "user" (id) ON DELETE CASCADE,
    name            VARCHAR(100)   NOT NULL,
    type            VARCHAR(20)    NOT NULL
        CHECK (type IN ('CASH', 'MOBILE_MONEY', 'BANK', 'OTHER')),
    currency        VARCHAR(3)     NOT NULL
        CHECK (currency IN ('MGA', 'EUR', 'USD')),
    initial_balance NUMERIC(19, 2) NOT NULL DEFAULT 0,
    archived        BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uk_account_user_name
    ON account (user_id, lower(name))
    WHERE archived = FALSE;