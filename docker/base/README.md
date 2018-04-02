# How to build it

IMPORTANT:
* Set the `ICONATOR_VERSION` env variable to publish always with a different version.
* Execute the commands in the root of the ICOnator-backend git repository.

```
$ export ICONATOR_VERSION=0.0.1
$ ./gradlew clean build
$ docker build -f docker/base/Dockerfile -t iconator/base:${ICONATOR_VERSION} -t iconator/base:latest ./
```

# How to push it

```
$ docker push iconator/base
```