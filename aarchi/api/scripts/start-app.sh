java -javaagent:/opentelemetry-javaagent.jar \
   -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE \
  -jar /app.jar \
  $PROG_ARGS
