# How to build it

```
$ export DOCKER_UBUNTU_ORACLE_JAVA8=v1
$ docker build -t iconator/ubuntu-oracle-java8:${DOCKER_UBUNTU_ORACLE_JAVA8} -t iconator/ubuntu-oracle-java8:latest ./
```

# How to push it to Docker Hub

```
$ docker push iconator/ubuntu-oracle-java8
```