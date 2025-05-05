FROM quay.io/keycloak/keycloak:26.2.2

# Copy our custom provider JAR to Keycloak's providers directory
COPY build/libs/oauthservice-0.0.1-SNAPSHOT.jar /opt/keycloak/providers/

# Build Keycloak with custom providers
RUN /opt/keycloak/bin/kc.sh build

# Configure the database
ENV KC_DB=postgres
ENV KC_DB_URL=jdbc:postgresql://postgres:5432/keycloak
ENV KC_DB_USERNAME=keycloak
ENV KC_DB_PASSWORD=password
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

ENV JAVA_OPTS="-Dquarkus.jar.disable-signature-verification=true"

# Start Keycloak
ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start-dev"]