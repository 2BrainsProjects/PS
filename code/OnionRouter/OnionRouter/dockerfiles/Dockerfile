FROM ubuntu:latest

RUN apt-get update
RUN apt-get install -y build-essential wget zlib1g-dev

WORKDIR /usr/local/src
RUN wget https://www.openssl.org/source/openssl-3.0.3.tar.gz
RUN tar -xf openssl-3.0.3.tar.gz
WORKDIR /usr/local/src/openssl-3.0.3

RUN ./config --prefix=/usr/local/ssl --openssldir=/usr/local/ssl shared zlib
RUN make
RUN make install

ENV PATH="${PATH}:/usr/local/ssl/bin"

WORKDIR /

RUN apt-get install -y openjdk-21-jdk

RUN apt-get install -y curl

ARG DEPENDENCY=build
# first layer with the external libs (i.e. the files that change the least).
COPY ${DEPENDENCY}/libs /app
# last layer with the application JARs (i.e. the files that change the most).
COPY ${DEPENDENCY}/classes /app

COPY dockerfiles/scripts/start.sh /app/start.sh
RUN chmod +x /app/start.sh

ENTRYPOINT ["/app/start.sh"]