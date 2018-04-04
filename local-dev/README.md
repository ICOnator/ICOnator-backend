# ICOnator: Local Development Project

This module provides an easy way to start all the ICOnator applications at once for local development. You have two options:

* Execute the `ICOnatorLocalDevApplication` class directly from the IDE;
* or, run from a single JAR file.

You can produce the single JAR file running the following command (relative of the git root dir):

```
$ sh gradlew clean :local-dev:build
$ sh local-dev/build/libs/local-dev.jar
```

A built-in message broker (Apache QPID) and an email sink would be automatically be started.

Also, a Parity node running on `localhost:8545` is required. You can override that by setting the `MONITOR_ETHEREUM_NODE_URL` environment variable.