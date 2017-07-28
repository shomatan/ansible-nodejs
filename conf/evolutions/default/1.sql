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

# --- !Downs

drop table password_info;
drop table user_login_info;
drop table login_info;
drop table users;