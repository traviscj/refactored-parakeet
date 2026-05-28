# Runtime image for iotcj.
#
# Build the distribution ON THE HOST first — the gradle build runs jooq codegen
# against MySQL, so it can't run inside `docker build`:
#
#   . bin/activate-hermit && ./gradlew installDist
#
# Then, from the kv3 repo:
#
#   docker-compose --profile full up --build
#
FROM eclipse-temurin:21-jre

WORKDIR /app
COPY build/install/iotcj/lib/ /app/lib/

# Container-only DB config (points at the compose `mysql` service). Placed on the
# classpath ahead of the app jar so misk overlays it onto traviscj-common.yaml.
COPY docker/traviscj-production.yaml /app/config/traviscj-production.yaml

EXPOSE 1337

# Bypass the generated start script so /app/config is first on the classpath.
ENTRYPOINT ["sh", "-c", "exec java -cp '/app/config:/app/lib/*' io.tcj.TraviscjApp"]
