[![Build Status](https://travis-ci.com/ICOnator/ICOnator-backend.svg?token=eUVyeGxidafMUjk8JWFo&branch=master)](https://travis-ci.com/ICOnator/ICOnator-backend.svg?token=eUVyeGxidafMUjk8JWFo&branch=master)

# ICOnator

ICOnator has the goal to support the worldwide tokenization of assets.

The ICOnator is an easy, secure, configurable, and scalable open source **ICO engine** -- driven by the community.

Basically, the concept of an ICO engine is to provide a generic core and, on top of that, ICO platforms can build specific features and tailor requirements which certainly differ depending on ICO's business requirements.

## How to run?

The repository [ICOnator-docker-compose](https://github.com/ICOnator/ICOnator-docker-compose) provides the easiest and
fastest way to experiment, see how it works, and check how to run ICOnator backend applications.

ICOnator provides its backend applications as:

* [Docker images](https://hub.docker.com/u/iconator/)
* [Plain JAR files](https://github.com/ICOnator/ICOnator-backend/releases)

## Architectural Components

The ICOnator backend is composed by the following applications:

* [Core](https://github.com/ICOnator/ICOnator-backend/tree/master/services/core): Responsible to expose APIs to clients (i.e., browsers). Such APIs provide functionalities as, e.g., registering new investors and distributing pay-in addresses; 
* [Email](https://github.com/ICOnator/ICOnator-backend/tree/master/services/email): Responsible to listen to events and send emails to investors;
* [Rates](https://github.com/ICOnator/ICOnator-backend/tree/master/services/rates): Responsible to constantly fetch rates, which would be consumed by other applications; 
* [Monitor](https://github.com/ICOnator/ICOnator-backend/tree/master/services/monitor): Responsible to monitor payment transactions to pay-in addresses, convert amounts, and listen to blockchain-related events.  

Each of them has specific functionalities that are documented in the respective sub-modules.

## Required Services and Infrastructure

The ICOnator applications require these services:

* **SQL Server**: for example, [PostgreSQL](https://www.postgresql.org) or [MariaDB](https://mariadb.org).
* **Message Broker Server**, AMQP 0.9.1 protocol: for example, [RabbitMQ](https://www.rabbitmq.com).
* **Ethereum Full Node**: for example, [Parity](https://www.parity.io) or [Geth](https://github.com/ethereum/go-ethereum/releases) -- or providers such as [Infura](http://infura.io) or [QuikNode](https://quiknode.io/).
* **SMTP Server** or **SMTP Relay**: for example, [Postfix](https://www.postfix.org), or services like [Amazon SES](https://aws.amazon.com/ses/) and [Google Gmail](https://www.google.com/gmail/).

# Development

## Local Dev

The module `local-dev` provides the `ICOnatorLocalDevApplication` class with a `main()` that is able to run all the ICOnator backend applications at once, without
any dependencies.

The `email` application is configured to use a mocked/dummy SMTP server, meaning that emails are only printed to the console instead of actually being sent.

Also, Apache QPID version 7.0.2 is used as a message broker server.

## Build with Gradle

In MacOS or Linux:

```
$ sh gradlew clean build
```

In Windows, with `CMD`:

```
gradlew.bat clean build
```

The resulting JAR files are located under `services/<APPLICATION_NAME>/build/libs/`.

## Docker

All the applications are configured to be packaged as docker images.

Then, it's possible to build the applications and run them independently.

### Build docker images

You can build the images as simple as running a command in gradle. However, before building a docker image for each application (`services/*`) it's required to build the executable JARs.

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

### Run docker images

If you want to run manually, you would need to specify the environment variables provided by each application.
You can get an overview of the supported environment variables by checking the [ICOnator-docker-compose](https://github.com/ICOnator/ICOnator-docker-compose) repository.

The general command to run an ICOnator application is:

```
$ SPRING_PROFILES_ACTIVE=dev docker run iconator/<APPLICATION_NAME>:latest
```

where `<APPLICATION_NAME>` is the module under `services/*`, and `SPRING_PROFILES_ACTIVE` env variable specifies 
the profile -- which can be "dev" or "prod". If no `SPRING_PROFILES_ACTIVE` is specified, then the default config is used.

For example, to run the `core` application:

```
$ SPRING_PROFILES_ACTIVE=dev docker run iconator/core:latest
```

### Running Application's Dependencies

#### Message Broker: RabbitMQ

All the applications interact with AMQP protocol and require a message broker running.

The easiest way to bootstrap a message broker is to use a RabbitMQ docker image. Execute the following:

```
docker run --rm -d --hostname test-rabbitmq --name rabbitmq-for-tests -e RABBITMQ_DEFAULT_VHOST=vhost1 -p 127.0.0.1:5672:5672 -p 127.0.0.1:5671:5671 -p 127.0.0.1:15671:15671 -p 127.0.0.1:15672:15672 rabbitmq:3-management
```

#### SMTP server

The `email` application sends emails using SMTP and, thus, requires a server running.

The easiest way to run a private SMTP server is to setup a Google GMail Relay server using plain username/password.

Once you have a GMail test account, execute the following:

```
docker run --rm -d --name smtp-server -e GMAIL_USER='<USER>' -e GMAIL_PASSWORD='<PASSWORD>' -p 127.0.0.1:2525:25 namshi/smtp
```

where the `<USER>` and `<PASSWORD>` are, respectively, the username and password of your GMail test account. 

#### Parity node

Run parity in the Kovan (testnet) chain:

```
docker run --rm -ti -p 127.0.0.1:8180:8180 -p 127.0.0.1:8545:8545 -p 127.0.0.1:8546:8546 -p 127.0.0.1:30303:30303 -p 127.0.0.1:30303:30303/udp parity/parity --ui-interface all --jsonrpc-interface all --tracing on --pruning fast --warp --mode active --chain kovan
```

#### PostgreSQL

Run PostgreSQL with the following command:

```
docker run --rm -ti -e POSTGRES_USER=testuser -e POSTGRES_PASSWORD=testpass -p 127.0.0.1:5432:5432 postgres
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