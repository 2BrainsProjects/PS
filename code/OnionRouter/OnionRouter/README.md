# Anonichat Frontend Technical Document

This document contains the frontend internal software organization and the main implementation challenges.

## Introduction

The frontend application is a console application built using threads and sockets to communicate with the backend server.

The Anonichat API documentation can be found [here](/docs/README.md).
The backend internal software organization can be found [here](https://github.com/2BrainsProjects/PS/blob/main/code/Api/README.md).

## API Communication

The communication with the API is done in the `HttpRequest` layer, thie uses the `HttpUtils` to make the request.

The media types supported by the application are the following:
* Request bodies:
    * `application/json`;

* Responses:
    * `application/vnd.siren+json` - when a response is successful;
    * `application/problem+json` - when a response represents an error.

If the response is `application/problem+json` the class throw an error with the message of the response
If any other media type is used, the application will throw an error, indicating the server response is not supported.

## Commands

In this application has 3 menu:
* initialization;
* operation;
* authentication.

The initialization menu is the first and is use so the client can choose is role: 
* user; 
* router;
* both.

The second is the authentication menu this doesn't show is the client is router. 
In this is possibly to:
* register a user;
* user authentication.

For the last operation, the menu will only be shown when the user is authenticated. In this is possibly:
* To add a contact;
* enter a conversation;
    * send a message;
    * see previous messages (Not implement);
    * see next messages (Not implement);
* list contacts (Not implement);
* logout.
