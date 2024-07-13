# Anonichat - Technical Document

This document contains the backend internal software organization, the data model and the main implementation challenges.

## Introduction

The backend server exposes a HTTP API that provides a system for the Anonichat, written in [Kotlin](https://kotlinlang.org/) that uses the [Spring Boot](https://spring.io/projects/spring-boot) library for processing the HTTP requests. 

The backend implementation was made to be used by frontend applications that communicate with each other via the backend service, which is responsible for ensuring all the restriction of the chat, as well as managing and storing all user and router related data. It is also designed in a way to allow the evolution of chat.


## Data Model

### Conceptual Model

The conceptual data model of the backend server is represented in the following diagram, that holds the 
entity-relationship diagram of the database:

![Data Model](../../docs/diagrams/er-diagram.png)

It is also represented in the following relational schema:

![Data Model](../../docs/diagrams/relational-diagram.png)

The data model contains the following entities:
* The `User` entity represents a user of the system;
* The `Token` entity represents a token used to authenticate a user;
* The `Message` entity represents a message sent by a user;
* The `Router` entity represents a router of the system;


The model also contains the following restrictions:
* __email__ must follow the following pattern "example@mail.com"


### Physical Model

The physical data model contains the implementation of the conceptual data model in the PostgreSQL database, that can be found in the [create-schema.sql](./src/sql/create-schema.sql) file.


The physical data model contains the following aspects:
* All primary keys that are represented by the `id` attribute are `serial`;
* Other attributes that are not primary keys, but also identify the entity are marked as `unique`;
* All the restrictions described in the [Conceptual Model](#conceptual-model) are reflected in the physical model, using `check`;
* The `created_at` and `last_used_at` attributes of the `Token` entity are stored as a bigint, which represents an instant in time.


## Application Architecture

### Software Organization

The backend server is organized in the following packages:
* [/http](./src/main/kotlin/pt/isel/ps/anonichat/http), which contains the controllers used to process the HTTP requests - [see more](#http);
* [/services](./src/main/kotlin/pt/isel/ps/anonichat/services/), which contains the classes that perform validations of parameters and implement the logic of the operations - [see more](#services);
* [/repository](./src/main/kotlin/pt/isel/ps/anonichat/repository/), which contains the interfaces and implementations that are used to access the PostgreSQL database, using the [JDBI](https://jdbi.org/) library - [see more](#repository);
* [/domain](./src/main/kotlin/pt/isel/ps/anonichat/domain/), which contains all the core entities of the anonichat - [see more](#domain);


#### HTTP

The HTTP package contains the controllers that process the HTTP requests.

The controllers are organized in the following classes:
* [controllers](./src/main/kotlin/pt/isel/ps/anonichat/http/controllers/), which contains the controllers that process the requests using the [Spring Web MVC](https://docs.spring.io/spring-framework/reference/web/webmvc.html);
* [pipeline](./src/main/kotlin/pt/isel/ps/anonichat/http/pipeline/), which contains the pipeline classes that process the requests, like the authentication and the error handling;
* [utils](./src/main/kotlin/pt/isel/ps/anonichat/http/utils/), which contains the utility objects that are used by the controllers, like the `Uris` and `Params`.


#### Services

The services package contains the classes that perform validations of parameters and implement the logic of the operations. It is responsible for receiving the parameters from the controllers, validating them and calling the corresponding repository methods to access the database.

The services are organized in the following classes:
* [UsersService](./src/main/kotlin/pt/isel/ps/anonichat/services/UserService.kt), which contains the services related to the `User` entity;
* [RouterService](./src/main/kotlin/pt/isel/ps/anonichat/services/RouterService.kt), which contains the services related to the `Router` entity;

#### Repository

The repository package contains the interfaces and implementations that are used to access the PostgreSQL database, using the [JDBI](https://jdbi.org/) library.

The repository is organized in the following classes:
* [UsersRepository](./src/main/kotlin/pt/isel/ps/anonichat/repository/UserRepository.kt), which contains the repository related to the `User` entity;
* [TokenRepository](./src/main/kotlin/pt/isel/ps/anonichat/repository/TokenRepository.kt), which contains the repository related to the `Token` entity;
* [RouterRepository](./src/main/kotlin/pt/isel/ps/anonichat/repository/RouterRepository.kt), which contains the repository related to the `Router` entity;
* [MessageRepository](./src/main/kotlin/pt/isel/ps/anonichat/repository/MessageRepository.kt), which contains the repository related to the `Message` entity;

This package also contains the `JdbiTransactionManager` used to access the database, and the `JdbiConfig` used to register the mappers of the application.


#### Domain

The domain package contains all the core entities of the anonichat, such as:
* [User](./src/main/kotlin/pt/isel/ps/anonichat/domain/user/User.kt), which represents a user of the system;
* [Router](./src/main/kotlin/pt/isel/ps/anonichat/domain/router/Router.kt), which represents a router of the system;
* [Token](./src/main/kotlin/pt/isel/ps/anonichat/domain/user/Token.kt), which represents a token of the user;
* [Message](./src/main/kotlin/pt/isel/ps/anonichat/domain/user/Message.kt), which represents a message of the user.

This package also contains the specific custom exceptions that are thrown by the application.


### Authentication

The authentication of the backend service is made using an Interceptor that is registered in the `PipelineConfigurer`, which is responsible for processing the `Authorization` or the `Cookie` headers of the requests, validating the token and retrieving a user.

The [`AuthenticationInterceptor`](./src/main/kotlin/pt/isel/ps/anonichat/http/pipeline/authentication/AuthenticationInterceptor.kt) class overrides the `preHandle` method, which is called before each request is processed by the controller.
The controllers that require authentication have a custom parameter [Session](./src/main/kotlin/pt/isel/ps/anonichat/http/pipeline/authentication/Session.kt) that is injected by the interceptor, which contains the user that is authenticated and the user token. 


## Error Handling

The backend service uses an [Exception Handler](./src/main/kotlin/pt/isel/ps/anonichat/http/pipeline/ExceptionHandler.kt), which is responsible to catch all the exceptions thrown by the 
application and return the corresponding error response, with the corresponding status code and message, represented by
the [Problem](./src/main/kotlin/pt/isel/ps/anonichat/http/media/Problem.kt) class, which is then represented in the JSON format. For example, when a token is invalid, a `InvalidTokenException` is thrown, which is then converted to the following response:

```json
{
    "type": "https://github.com/2BrainsProjects/PS/blob/main/docs/problems/invalid-token",
    "title": "Unauthorized",
    "detail": "Invalid user token",
    "instance": "api/logout"
}
```


## Implementation Challenges

The implementation of the controllers with the `SirenEntity` class was a bit challenging, since we had to understand how `hypermedia` worked, and how it is used to represent entities in the JSON format.

While implementing the JDBI repository methods, we ended up having problems with the JDBI mappers, specially with the nested properties of the classes that were needed to map, such as the Game and Player mappers. 
Instead of the implementing multiple column mappers to allow the mapping of those two classes, we implemented row mapper for each one of them, which seemed easier to understand.

## Deployment

To run the application, make sure you have Docker Desktop installed, and execute the following command:
```bash
docker compose up -d
```
# Conclusion

In conclusion, this technical document provides an in-depth overview of the Anonichat backend service. It outlines the application architecture, data model, software organization and various implementation details of the server.
The system is built with flexibility in mind, allowing for the adaptation of game rules in the future, and dynamic interaction between frontend applications and the backend service.

To enhance the system in the future, there are several potential improvements we can make, such as:
* Implement a mechanism to delete expired tokens from the database
