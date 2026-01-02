CREATE TABLE user_relations (
    follower_id UUID NOT NULL,
    following_id UUID NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    PRIMARY KEY (follower_id, following_id),
    CONSTRAINT fk_user_relations_follower FOREIGN KEY (follower_id) REFERENCES users (id),
    CONSTRAINT fk_user_relations_following FOREIGN KEY (following_id) REFERENCES users (id)
);

CREATE INDEX idx_following_follower ON user_relations (following_id, follower_id);
