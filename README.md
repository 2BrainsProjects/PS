# Instituto Superior de Engenharia de Lisboa
## **Project and Seminar**
## Summer Semester 2023/2024

### Anonichat - Backend & Frontend Web Implementation

<img src="https://skillicons.dev/icons?i=kotlin,java,spring,postgresql"/>

<br>

The **HTTP API documentation** for a frontend client application is available [here](docs\README.md).

The **backend technical document** containing the backend service implementation is available [here](code\Api\README.md).

The **frontend technical document** containing the frontend service implementation is available [here](code\OnionRouter\OnionRouter)

# How to run

To run, follow the steps below:
* install openssl version 3.3.0;
* Configure the environment variable `DATABASE_URL` (e.g. jdbc:postgresql://localhost/postgres?user=postgres&password=postgres);

* Build the database using the following commands:
```sh
psql -U postgres

<password>

create schema dbo;

create table if not exists dbo.User(
    id serial primary key,
    ip varchar(64) null,
    name varchar(64) unique not null,
    email varchar(64) unique not null check (email ~ '^[A-Za-z0-9+.-]+@(.+)$'),
    password_hash varchar(60) not null,
    certificate varchar(256) null,
    session_info varchar(256) null
);

create table if not exists dbo.Token(
    token_hash varchar(256) primary key,
    user_id int references dbo.User(id) on delete cascade,
    created_at bigint not null,
    last_used_at bigint not null
);

create table if not exists dbo.Router(
    id serial primary key,
    ip varchar(64) null,
    password_hash varchar(60) not null,
    certificate varchar(256) null
);

create table if not exists dbo.Message(
    user_id int references dbo.User(id) on delete cascade,
    cid varchar(128) not null,
    message varchar(512) not null,
    msg_date timestamp not null
);

```

* Run the API using the following command:
```sh 
gradlew launch
```

* To execute an instance of Onion routers, use the command:
```sh
gradlew launch <port>
```

The network should consist of at least two clients and 4 routers.

The application only runs on localhost, but it receives the port as an argument in order to simulate communication between users.
This port must be different for each user and must be different from 8080


> Nº 49449 Diogo Almeida - [Diogo Almeida](https://github.com/wartuga) \
> Nº 49469 Joana Chuço - [Joana Chuço](https://github.com/49469)

Orientador: Eng. Diego Passos
