CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

create table IF NOT EXISTS password_reset_session (
  id UUID primary key DEFAULT  uuid_generate_v4(),
  email varchar(255) ,
    otp varchar(10),
    is_verified BOOLEAN DEFAULT FALSE ,
    expires_at timestamp NOT NULL ,
    used BOOLEAN default false
);
