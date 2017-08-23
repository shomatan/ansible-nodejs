# --- !Ups

create table users (
  id VARCHAR NOT NULL,
  first_name VARCHAR,
  last_name VARCHAR,
  email VARCHAR,
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL,
  primary key (id)
);

create table login_info (
  id BIGSERIAL,
  provider_id VARCHAR NOT NULL,
  provider_key VARCHAR NOT NULL,
  primary key (id)
);

create table user_login_info (
  user_id VARCHAR NOT NULL,
  login_info_id BIGINT NOT NULL
);

create table password_info (
  hasher VARCHAR NOT NULL,
  password VARCHAR NOT NULL,
  salt VARCHAR,
  login_info_id BIGINT NOT NULL
);

create table posts (
    post_id     BIGSERIAL PRIMARY KEY
  , title       VARCHAR   NOT NULL
  , content     TEXT      NOT NULL
  , created_at  BIGINT    NOT NULL
  , updated_at  BIGINT    NOT NULL
  , posted_at  BIGINT    NOT NULL
);

create table categories (
    category_id BIGSERIAL PRIMARY KEY
  , category    VARCHAR   NOT NULL    UNIQUE
);

create table tags (
    tag_id  BIGSERIAL PRIMARY KEY
  , tag     VARCHAR   NOT NULL    UNIQUE
);

CREATE TABLE post_category (
    category_id   int REFERENCES categories (category_id)   ON UPDATE CASCADE ON DELETE CASCADE
  , post_id       int REFERENCES posts (post_id)            ON UPDATE CASCADE ON DELETE CASCADE
  , CONSTRAINT pk_post_category PRIMARY KEY (category_id, post_id)  -- explicit pk
);

CREATE TABLE post_tag (
    tag_id        int REFERENCES tags (tag_id)              ON UPDATE CASCADE ON DELETE CASCADE
  , post_id       int REFERENCES posts (post_id)            ON UPDATE CASCADE ON DELETE CASCADE
  , CONSTRAINT pk_post_tag PRIMARY KEY (tag_id, post_id)  -- explicit pk
);

CREATE TABLE post_custom_fields (
    post_id       int     REFERENCES posts (post_id)        ON UPDATE CASCADE ON DELETE CASCADE
  , key_name      VARCHAR NOT NULL
  , value         VARCHAR NOT NULL
  , PRIMARY KEY (post_id, key_name)
);

INSERT INTO users VALUES ('c622137e-d45f-4542-bd46-934d3e8a0dd7', 'Admin', 'user', 'admin@admin.com', 1502153234, 1502153234);
INSERT INTO login_info (provider_id, provider_key) VALUES ('credentials', 'admin@admin.com');
INSERT INTO user_login_info VALUES ('c622137e-d45f-4542-bd46-934d3e8a0dd7', 1);
INSERT INTO password_info VALUES ('bcrypt', '$2a$10$TgaGi7bBm9BGjVvq9H/pOucAUq6gVin.nrtDw5wz7Ux0NZyXzxieq', NULL, 1);

# --- !Downs
drop table post_custom_fields;
drop table post_tag;
drop table post_category;
drop table tags;
drop table categories;
drop table posts;

drop table password_info;
drop table user_login_info;
drop table login_info;
drop table users;