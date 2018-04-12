[![Build Status](https://travis-ci.com/ICOnator/ICOnator-backend.svg?token=eUVyeGxidafMUjk8JWFo&branch=master)](https://travis-ci.com/ICOnator/ICOnator-backend.svg?token=eUVyeGxidafMUjk8JWFo&branch=master)

# ICOnator

ICOnator has the aim to make the tokenization of assets a popular and an easy process.

## Description

We are building the most straight-forward, secure, configurable, and user-friendly open source ICO engine -- driven by the community!

## API documentation

ICOnator has integrated swagger in the non-production profile. The API documentation is available on [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html).

## Architecture

To be described.

## Components

To be described.

## Required Infrastructure

To be described.

## Development

### RabbitMQ (AMQP 0.9.1 protocol)

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

# Docker

## Build the images

You can build the images as simple as running a command in gradle. However, before building a docker image for each service (`services/*`) it's required to build the executable JARs.

In a single command line:

```
$ sh gradlew clean build && sh gradlew docker
```  

By default, only the tag `latest` will be added to the docker image. If a version is required, then the project parameter `dockerVersion` should be added to gradle:

```
$ sh gradlew clean build && sh gradlew -PdockerVersion=1.0 docker dockerTag
```

This command will produce something similar to the following result:

```
$ docker images                                              
REPOSITORY          TAG        IMAGE ID         CREATED                  SIZE
iconator/core       1.0        424c921ac2e9     Less than a second ago   179MB
iconator/core       latest     424c921ac2e9     Less than a second ago   179MB
iconator/email      1.0        6d489a3eda44     Less than a second ago   184MB
iconator/email      latest     6d489a3eda44     Less than a second ago   184MB
iconator/rates      1.0        87d7240a7b5a     Less than a second ago   149MB
iconator/rates      latest     87d7240a7b5a     Less than a second ago   149MB
iconator/monitor    1.0        f58d5eaf674a     Less than a second ago   180MB
iconator/monitor    latest     f58d5eaf674a     Less than a second ago   180MB
``` 

Now you can already run the images. :-)

## Run

The git repository [ICOnator-docker-compose](https://github.com/ICOnator/ICOnator-docker-compose) provides the easiest and
fastest way to run and deploy the ICOnator.

If you want to run manually, you would need to specify the environment variables provided by each application.

Basically, the general command is:

```
$ SPRING_PROFILES_ACTIVE=dev docker run iconator/<SERVICE_NAME>:latest
```

where `<SERVICE_NAME>` is the module under `services/*`, and `SPRING_PROFILES_ACTIVE` env variable specifies 
the profile -- which can be "dev" or "prod". If no `SPRING_PROFILES_ACTIVE` is specified, then the default config is used.

For example, to run the `core` service:

```
$ SPRING_PROFILES_ACTIVE=dev docker run iconator/core:latest
```

# Authentication

Get JWT
```
curl -i -H "application/x-www-form-urlencoded" -X POST -d "username=<USER>&password=<PASSWORD>" http://localhost:8081/login
```

where `<USER>` = spring.security.user.name and `<PASSWORD>` = spring.security.user.password

Send authenticated request
```
curl -i -H "Authorization: Bearer <TOKEN>" http://localhost:8081/actuator/health
```

# License

```

   Copyright 2018 AxLabs GmbH and ICOnator Project
   
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
   
       http://www.apache.org/licenses/LICENSE-2.0
       
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
```