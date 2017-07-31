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
);

create table categories (
    category_id BIGSERIAL PRIMARY KEY
  , name        VARCHAR   NOT NULL
);

create table tags (
    tag_id  BIGSERIAL PRIMARY KEY
  , tag     VARCHAR   NOT NULL
);

CREATE TABLE post_category (
    category_id   int REFERENCES categories (category_id)   ON UPDATE CASCADE
  , post_id       int REFERENCES posts (post_id)            ON UPDATE CASCADE
  , CONSTRAINT post_category_pkey PRIMARY KEY (category_id, post_id)  -- explicit pk
);

CREATE TABLE post_tag (
    tag_id        int REFERENCES tags (tag_id)              ON UPDATE CASCADE
  , post_id       int REFERENCES posts (post_id)            ON UPDATE CASCADE
  , CONSTRAINT post_tag_pkey PRIMARY KEY (tag_id, post_id)  -- explicit pk
);

# --- !Downs

drop table post_tag;
drop table post_category;
drop table tags;
drop table categories;
drop table posts;

drop table password_info;
drop table user_login_info;
drop table login_info;
drop table users;