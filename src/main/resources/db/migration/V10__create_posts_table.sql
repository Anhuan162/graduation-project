CREATE TABLE categories
(
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    created_at    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    category_type VARCHAR(50),
    creator       UUID         REFERENCES users (id) ON DELETE SET NULL
);
CREATE INDEX idx_categories_name ON categories (name);
CREATE INDEX idx_categories_creator ON categories (creator);


CREATE TABLE category_managers
(
    category_id UUID NOT NULL REFERENCES categories (id) ON DELETE CASCADE,
    user_id     UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    PRIMARY KEY (category_id, user_id)
);
CREATE INDEX idx_category_managers_user ON category_managers (user_id);


CREATE TABLE topics
(
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    category_id      UUID         REFERENCES categories (id) ON DELETE SET NULL,
    title            VARCHAR(255) NOT NULL,
    created_at       TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    content          TEXT,
    topic_visibility VARCHAR(50)      DEFAULT 'PUBLIC',
    created_by       UUID         REFERENCES users (id) ON DELETE SET NULL
);
CREATE INDEX idx_topics_category ON topics (category_id);
CREATE INDEX idx_topics_created_by ON topics (created_by);
CREATE INDEX idx_topics_title ON topics (title);


CREATE TABLE topic_members
(
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    topic_role VARCHAR(50),
    approved   BOOLEAN          DEFAULT FALSE,
    joined_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    user_id    UUID REFERENCES users (id) ON DELETE CASCADE,
    topic_id   UUID REFERENCES topics (id) ON DELETE CASCADE
);
CREATE INDEX idx_topic_members_user ON topic_members (user_id);
CREATE INDEX idx_topic_members_topic ON topic_members (topic_id);


CREATE TABLE posts
(
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    topic_id   UUID REFERENCES topics (id) ON DELETE CASCADE,
    content    TEXT NOT NULL,
    created_by UUID REFERENCES users (id) ON DELETE SET NULL,
    created_at TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_posts_topic ON posts (topic_id);
CREATE INDEX idx_posts_created_by ON posts (created_by);


CREATE TABLE comments
(
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    post_id           UUID REFERENCES posts (id) ON DELETE CASCADE,
    author_id         UUID REFERENCES users (id) ON DELETE SET NULL,
    parent_id         UUID REFERENCES comments (id) ON DELETE CASCADE,
    content           TEXT,
    created_date_time TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_comments_post ON comments (post_id);
CREATE INDEX idx_comments_author ON comments (author_id);
CREATE INDEX idx_comments_parent ON comments (parent_id);
