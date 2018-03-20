web: java -Dserver.port=$PORT $JAVA_OPTS -Dspring.profiles.active=prod -jar services/core/build/libs/core.jar
worker: java $JAVA_OPTS -Dspring.profiles.active=prod -jar services/monitor/build/libs/monitor.jar
mail: java $JAVA_OPTS -Dspring.profiles.active=prod -jar services/email/build/libs/email.jar
