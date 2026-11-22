-- Mensajería interna entre compradores y productores.
CREATE TABLE conversations (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT,
    buyer_id BIGINT NOT NULL,
    producer_id BIGINT NOT NULL,
    last_message_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_conversations_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE SET NULL,
    CONSTRAINT fk_conversations_buyer FOREIGN KEY (buyer_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_conversations_producer FOREIGN KEY (producer_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_conversations_participants CHECK (buyer_id <> producer_id)
);

CREATE UNIQUE INDEX uq_conversations_product_buyer
    ON conversations (product_id, buyer_id)
    WHERE product_id IS NOT NULL;

CREATE INDEX idx_conversations_buyer ON conversations (buyer_id, last_message_at DESC);
CREATE INDEX idx_conversations_producer ON conversations (producer_id, last_message_at DESC);

CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content VARCHAR(2000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMPTZ,
    CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_messages_conversation ON messages (conversation_id, created_at);
CREATE INDEX idx_messages_unread ON messages (conversation_id, read_at);
