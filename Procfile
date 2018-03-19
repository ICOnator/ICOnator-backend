web: java -Dserver.port=$PORT $JAVA_OPTS -Dspring.profiles.active=prod -Dio.iconator.commons.amqp.url=$AMQP_URL -jar services/core/build/libs/core.jar
monitor: java -Dserver.port=$PORT $JAVA_OPTS -Dspring.profiles.active=prod -Dio.iconator.commons.amqp.url=$AMQP_URL -jar services/monitor/build/libs/monitor.jar
