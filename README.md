# Instituto Superior de Engenharia de Lisboa
## **Project and Seminar**
## Summer Semester 2023/2024

### Anonichat - Backend & Frontend Web Implementation

<img src="https://skillicons.dev/icons?i=kotlin,java,spring,postgresql"/>

<br>

The **HTTP API documentation** for a frontend client application is available [here](https://github.com/2BrainsProjects/PS/blob/main/docs/README.md).

The **backend technical document** containing the backend service implementation is available [here](https://github.com/2BrainsProjects/PS/blob/main/code/Api/README.md).

The **frontend technical document** containing the frontend service implementation is available [here](https://github.com/2BrainsProjects/PS/blob/main/code/OnionRouter/OnionRouter/README.md)

# How to run

To run, follow the steps below:
* install openssl version 3.3.0;
* add openssl/bin to PATH;
* install docker desktop;


* Run the API using the following command in the folder API:
```sh 
docker compose up -d
```

* To execute an instance of Onion routers, use the command in the folder OnionRouter:
```sh
gradlew launch --args=<port>
```

The network should consist of at least two clients and 4 routers.

The application only runs on localhost, but it receives the port as an argument in order to simulate communication between users.
This port must be different for each user and must be different from 8080


> Nº 49449 Diogo Almeida - [Diogo Almeida](https://github.com/wartuga) \
> Nº 49469 Joana Chuço - [Joana Chuço](https://github.com/49469)

Orientador: Eng. Diego Passos
