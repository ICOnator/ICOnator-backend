[![Build Status](https://travis-ci.com/ICOnator/ICOnator-backend.svg?token=eUVyeGxidafMUjk8JWFo&branch=master)](https://travis-ci.com/ICOnator/ICOnator-backend.svg?token=eUVyeGxidafMUjk8JWFo&branch=master)

# ICOnator

ICOnator has the aim to make the tokenization of assets a popular and easy process.

We are building the most straight-forward, secure, configurable, and user-friendly open source ICO/ITO platform ever -- all driven by the community!

## Description

To be described.

## How to run?

To be described.

## Architecture

To be described.

## Components

To be described.

## Required Infrastructure

To be described.

## Development

### RabbitMQ (AMQP 0.9.2 protocol)

All the applications interact with AMQP protocol and require a message broker running.

The easiest way to bootstrap a message broker is to use RabbitMQ using docker. Execute the following:

```
docker run --rm -d --hostname test-rabbitmq --name rabbitmq-for-tests -e RABBITMQ_DEFAULT_VHOST=vhost1 -p 127.0.0.1:5672:5672 -p 127.0.0.1:5671:5671 -p 127.0.0.1:15671:15671 -p 127.0.0.1:15672:15672 rabbitmq:3-management
```

### SMTP server

The `service/email` application sends emails using SMTP and, thus, requires a server running.

The easiest way to have a private SMTP server running is to setup a Gmail Relay server using plain username/password.

Once you have a Gmail test account to send emails from, execute the following:

```
docker run --rm -d --name smtp-server -e GMAIL_USER='<USER>' -e GMAIL_PASSWORD='<PASSWORD>' -p 127.0.0.1:2525:25 namshi/smtp
```

where the `<USER>` and `<PASSWORD>` are, respectively, the username and password of your Gmail test account. 

### Parity node

Run parity in the Kovan (testnet) chain:

```
docker run --rm -ti -p 127.0.0.1:8180:8180 -p 127.0.0.1:8545:8545 -p 127.0.0.1:8546:8546 -p 127.0.0.1:30303:30303 -p 127.0.0.1:30303:30303/udp parity/parity --ui-interface all --jsonrpc-interface all --tracing on --pruning fast --warp --mode active --chain kovan
```

### ELK stack

Install elk-stack:
```
git clone https://github.com/deviantony/docker-elk.git
docker-compose up
```
Full documentation: [docker-elk](https://github.com/deviantony/docker-elk)
