# Gomoku Frontend Technical Document

This document contains the frontend internal software organization and the main implementation challenges.


## Introduction

The frontend application is a single page application built using React and served with Webpack.

The Gomoku API documentation can be found [here](/docs/README.md).
The backend internal software organization can be found [here](../jvm/README.md).


## Code Organization

The frontend code is organized in the following structure:

* `js`
    * `public` - Contains the `index.html` file, which is the page that is served to the browser;
    * `src`
        * `app` - Contains the main component of the application, with the routes and the navigation logic;
        * `components` - Contains the React components and pages used in the application;
        * `contexts` - Contains all the contexts/providers used in the application;
        * `domain` - Contains the domain classes used in the application;
        * `services` - Contains the services used in the application;
        * `index.tsx` - The entry point of the application;
    * `tests` - Contain the front-end tests build with the `Playwright` framework.


## API Communication

The communication with the API is done in the `services` layer, that has the following structure:

* `LinksService` - The class that contains all the hypermedia links needed for the communication with the API;
* `HttpService` - The base class for all the services, which allows to make requests with the `GET`, `POST`, `PUT` and `DELETE` methods;
* `GomokuService` - The class that contains all the domain related services:
    * `HomeService` - The service that contains the method to get the API Home.
    * `UsersService` - The service that contains the methods for the user management;
    * `GamesService` - The service that contains the methods for the game management;
    * `LobbyService` - The service that contains the methods for the lobby management;

The media types supported by the application are the following:
* Request bodies:
    * `application/json`;

* Responses:
    * `application/vnd.siren+json` - when a response is successful;
    * `application/problem+json` - when a response represents an error.

If any other media type is used, the application will throw an error, indicating the server response is not supported.


## Authentication

The authentication is done using the browser's CookieStorage that allows the user session to be stored persistently in the browser.
When an authenticated request should be made, the response header `{'credentials': 'include'}` is added to the request, so the browser can send the cookies in the request.

The user session is then handled using the `SessionContext` that provides the application the current user session state and data.

To know if the user is currently logged in, when the page loads, the application makes a request to the user home of the API.
If the request is successful, the user is logged in, otherwise, the user is not logged in. That request also returns the user information
that is then stored in the session context.


## Contexts/Providers

The contexts creates a sub-tree with information that can be accessed by any child present in that same sub-tree.

### SessionContext
    
This context provides the `isLoggedIn` and `session` information that allows the application to know if the user is logged in and the user information.
It also provides the `login` and `logout` methods called in the respective moment of login or logout.

To use this context, the component should use the `useSession` hook to access the context information.


### ServicesContext

This context provides all the services needed for the application with the service of the type `GomokuService`.
This provides a better sense of organization and allows the application to have a single point of access to all the services, without having
to create a new instance of the service in each component.

To use this context, the component should use the `useServices` hook to access the context information.


### ErrorContext

This context allows any component in the application to set an error that can be then consumed by the `ErrorPopup`, that will show a pop-up with the error message.
When an error is thrown, it is then converted to a `Problem` object, that is then used to get the `title` and `detail` of the error.

To use this context, the component should use the `useErrorContext` hook to access the context information.


## Navigation & Deep-linking

The navigation and deep-linking are accomplished using the `react-router-dom` library, that allows the user to have a 
seamless navigation experience, with history and deep-linking support, using URI templates.

It allows the application to have different routes and pages, and persist the state of the application when the page is reloaded.


## Conclusion

In conclusion, this technical document provides an in-depth overview of the Gomoku frontend application organization and structure.
It outlines the application internal software organization, such as the API communication, authentication, contexts/providers, navigation and deep-linking.

All the requirements were met and the application is fully functional.

Along the way, we had some implementation challenges such as:
* Implementing the board component correctly;
* Implementing the session management;
* Implementing error handling;
* UI testing;
* Docker deployment.
