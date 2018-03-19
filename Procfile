web: java -Dserver.port=$PORT $JAVA_OPTS -Dspring.profiles.active=prod -Dio.iconator.commons.amqp.url=$AMQP_URL -jar services/core/build/libs/core.jar
worker: java $JAVA_OPTS -Dspring.profiles.active=prod -Dio.iconator.commons.amqp.url=$AMQP_URL -jar services/monitor/build/libs/monitor.jar
worker: java $JAVA_OPTS -Dspring.profiles.active=prod -Dio.iconator.commons.amqp.url=$AMQP_URL -Dio.iconator.commons.mail.service.host=email-smtp.us-east-1.amazonaws.com -Dio.iconator.commons.mail.service.admin=eureka@sciencematters.io -jar services/email/build/libs/email.jar
