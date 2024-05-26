# Anonichat - API Documentation

This document contains the HTTP API documentation for a frontend client application.

## Introduction

This API is designed to be consumed by a frontend client application, divided by the following entities:
* [User](#user), that provide actions such as register, login and get more users;
* [Router](#router), that provide actions such as create, delete and get more routers.

## Media Types

This API uses the following media types:
* `application/json`, which is used in request bodies - [JSON](https://www.json.org/json-en.html)
* `application/vnd.siren+json`, which is being used in the response bodies - [Siren](https://github.com/kevinswiber/siren)
* `application/problem+json`, which is used in response errors - [RFC9457](https://www.rfc-editor.org/rfc/rfc9457.html)

This API uses [hypermedia](https://en.wikipedia.org/wiki/Hypermedia), which is a way of providing information to the client application about the available actions that can be taken from a certain resource.
* All the response bodies contain are represented by a [Siren](https://github.com/kevinswiber/siren), which contains the following properties:
    * `class`, which is an array of strings that represent the type of the resource;
    * `properties`, which is an object that contains the properties of the resource;
    * `entities`, which is an array of sub-entities that represent the sub-resources of the resource;
    * `actions`, which is an array of actions that can be taken from the resource;
    * `links`, which is an array of links that can be used to navigate to other resources.

* The available navigation links are:
    * `self`: Represents the uri of the resource itself
    * `user-home`: User Home

* The available actions are:
    * `logout`: Logout a user

    
## Available Operations


### User

* __User Home__ - The user home contains all the actions that require authentication that can be taken from there. __Requires authentication__.


* __Register__ - Register a new user in the system.

    * The request body to this request should be a JSON object with the following properties:
        * `name` - the user's name __(required)__;
        * `email` - the user's email __(required)__;
        * `password` - the user's password (must have uppercase and lowercase letters, at least a number and a special character) __(required)__;
        * `clientCSR` - the user's csr (Certificate Signing Request) __(required)__.
        
    * The client application should then login the user to get a valid token that should be used in authenticated requests.


* __Login__ - Login a user in the system.

    * The request body to this request should be a JSON object with the following properties:
        * `name` or `email` - the user's name or email __(required)__;
        * `password` - the user's password __(required)__;
        * `ip` - the user's ip __(required)__.
        
    * This request returns a token that should be used in authenticated requests.
    * The token is refreshed every time an authenticated request is made.


* __Logout__ - Logout a user from the system. __Requires authentication__.
    
    * The request body to this request should be a JSON object with the following properties:
        * `seesionInfo` - the session info __(required)__.


* __Get User__ - Get a user in the system. __Requires authentication__.


* __Get Users__ - Get users from the system.

    * The query parameter for this request are:

        * `ids` - the ids of the users  __(required)__.
            * Type: `List<int>`

* __Get Users Count__ - Get the count of users in the system.

* __Get Messages__ - Get messages from the system. __Requires authentication__.

    * The query parameter for this request are:

        * cid - the conversation id __(required)__.
        * msgDate - the message date (format must be yyyy-MM-dd HH:mm:ss) __(optional)__.
          
* __Save Message__ - Save a message in the system. __Requires authentication__.

    * The request body to this request should be a JSON object with the following properties:
        * `cid` - the conversation id __(required)__.
        * `message` - the message __(required)__.
        * `msgDate` - the message date (format must be yyyy-MM-dd HH:mm:ss) __(required)__.


### Router

* __Create Router__ - Create a new router in the system.

    * The request body to this request should be a JSON object with the following properties:
        * `ip` - the router's ip __(required)__;
        * `routerCSR` - the router's csr (Certificate Signing Request) __(required)__.
        * `pwd` - the router's password __(required)__.


* __Get Routers__ - Get a router in the system.
    
    * The query parameter for this request are:

        * `ids` - the ids of the users __(required)__.
            * Type: `List<int>`


* __Get Routers Count__ - Get the count of routers in the system.


* __Delete Router__ - Delete a router in the system.

    * The path parameter for this request is `routerId` - the id of the router to delete. __(required)__.
    * The request body to this request should be a JSON object with the following properties:
        * `pwd` - the router's password __(required)__.


## Authentication

The authentication is made using a token, which is required to be in one of the following headers in a request:
* `Authorization Header`
  * Uses the [Bearer](https://swagger.io/docs/specification/authentication/bearer-authentication/) authentication scheme.
* `Cookie Header` 
  * Uses the [Cookie](https://swagger.io/docs/specification/authentication/cookie-authentication/) authentication scheme.

Each user has a max of 3 tokens, and if a user tries to login with more than 3 tokens, the oldest token is deleted.
The token has max validity of 15 days, but if no authenticated request is made in 3 days, it expires.
When it does, the user should login again to get a new token.

