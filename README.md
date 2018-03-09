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
docker run -d --hostname test-rabbitmq --name rabbitmq-for-tests -e RABBITMQ_DEFAULT_VHOST=vhost1 -p 5672:5672 -p 5671:5671 -p 127.0.0.1:15671:15671 -p 127.0.0.1:15672:15672 rabbitmq:3-management
```

### SMTP server

The `service/email` application sends emails using SMTP and, thus, requires a server running.

The easiest way to have a private SMTP server running is to setup a Gmail Relay server using plain username/password.

Once you have a Gmail test account to send emails from, execute the following:

```
docker run -d --name smtp-server -e GMAIL_USER='<USER>' -e GMAIL_PASSWORD='<PASSWORD>' -p 25:25 namshi/smtp
```

where the `<USER>` and `<PASSWORD>` are, respectively, the username and password of your Gmail test account. 