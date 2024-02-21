create schema dbo;

create table if not exists dbo.User(
    id serial primary key,
    name varchar(64) unique not null,
    password_hash varchar(60) not null,
    email varchar(64) unique not null check (email ~ '^[A-Za-z0-9+_.-]+@(.+)$')
);

create table if not exists dbo.Token(
    token_hash varchar(256) primary key,
    user_id int references dbo.User(id) on delete cascade,
    created_at bigint not null,
    last_used_at bigint not null
);
