CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS categories
(
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    created_at    TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    category_type VARCHAR(50),
    creator       UUID REFERENCES users(id) ON DELETE SET NULL
    );

CREATE INDEX IF NOT EXISTS idx_categories_name ON categories(name);
CREATE INDEX IF NOT EXISTS idx_categories_creator ON categories(creator);


CREATE TABLE IF NOT EXISTS category_managers
(
    category_id UUID NOT NULL REFERENCES categories (id) ON DELETE CASCADE,
    user_id     UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    PRIMARY KEY (category_id, user_id)
);
CREATE INDEX IF NOT EXISTS idx_category_managers_user ON category_managers (user_id);


CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS topics
(
    id                 UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title              VARCHAR(255) NOT NULL,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    content            TEXT,
    topic_visibility   VARCHAR(50) DEFAULT 'PUBLIC',
    category_id        UUID REFERENCES categories(id) ON DELETE SET NULL,
    created_by          UUID REFERENCES users(id) ON DELETE SET NULL
    );
CREATE INDEX IF NOT EXISTS idx_topics_category ON topics(category_id);
CREATE INDEX IF NOT EXISTS idx_topics_created_by ON topics(created_by);
CREATE INDEX IF NOT EXISTS idx_topics_title ON topics(title);


CREATE TABLE IF NOT EXISTS topic_members
(
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    topic_role VARCHAR(50),
    approved   BOOLEAN          DEFAULT FALSE,
    joined_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    user_id    UUID REFERENCES users (id) ON DELETE CASCADE,
    topic_id   UUID REFERENCES topics (id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_topic_members_user ON topic_members (user_id);
CREATE INDEX IF NOT EXISTS idx_topic_members_topic ON topic_members (topic_id);


CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS posts
(
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title                   VARCHAR(255) NOT NULL,
    content                 TEXT NOT NULL,
    created_date_time       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    post_status             VARCHAR(50) DEFAULT 'PENDING',
    approved_by             UUID REFERENCES users(id) ON DELETE SET NULL,
    approved_at             TIMESTAMP NULL,
    topic_id                UUID REFERENCES topics(id) ON DELETE CASCADE,
    author_id               UUID REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_posts_topic ON posts(topic_id);
CREATE INDEX IF NOT EXISTS idx_posts_author ON posts(author_id);
CREATE INDEX IF NOT EXISTS idx_posts_status ON posts(post_status);


CREATE TABLE IF NOT EXISTS comments
(
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    post_id           UUID REFERENCES posts (id) ON DELETE CASCADE,
    author_id         UUID REFERENCES users (id) ON DELETE SET NULL,
    parent_id         UUID REFERENCES comments (id) ON DELETE CASCADE,
    content           TEXT,
    created_date_time TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_comments_post ON comments (post_id);
CREATE INDEX IF NOT EXISTS idx_comments_author ON comments (author_id);
CREATE INDEX IF NOT EXISTS idx_comments_parent ON comments (parent_id);
