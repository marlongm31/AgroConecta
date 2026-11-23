ALTER TABLE messages
    ADD COLUMN client_message_id VARCHAR(120),
    ADD COLUMN attachment_url VARCHAR(400),
    ADD COLUMN attachment_content_type VARCHAR(120),
    ADD COLUMN attachment_size_bytes BIGINT,
    ADD COLUMN attachment_name VARCHAR(120);

ALTER TABLE messages
    ADD CONSTRAINT ck_messages_content_or_attachment
    CHECK (length(btrim(content)) > 0 OR attachment_url IS NOT NULL);

CREATE UNIQUE INDEX uq_messages_client_message
    ON messages (sender_id, client_message_id)
    WHERE client_message_id IS NOT NULL;
