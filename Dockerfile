FROM quay.io/keycloak/keycloak:latest

# Copy our custom provider JAR to Keycloak's providers directory
COPY build/libs/oauthservice-0.0.1-SNAPSHOT.jar /opt/keycloak/providers/

# Enable health and metrics support
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

# Configure the database
ENV KC_DB=postgres
ENV KC_DB_URL=jdbc:postgresql://postgres:5432/keycloak
ENV KC_DB_USERNAME=keycloak
ENV KC_DB_PASSWORD=password

# Start Keycloak
ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start"] 