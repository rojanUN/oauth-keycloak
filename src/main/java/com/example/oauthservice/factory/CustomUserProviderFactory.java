package com.example.oauthservice.factory;

import com.example.oauthservice.provider.CustomUserProvider;
import com.example.oauthservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class CustomUserProviderFactory implements UserStorageProviderFactory<CustomUserProvider> {
    public static final String PROVIDER_ID = "custom-user-federation-provider";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final List<ProviderConfigProperty> configMetadata;

    public CustomUserProviderFactory() {
        configMetadata = ProviderConfigurationBuilder.create()
                .property()
                .name("dbSchema")
                .label("Database Schema")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("public")
                .helpText("Database schema containing user tables")
                .add()
                .build();
    }

    @Override
    public CustomUserProvider create(KeycloakSession keycloakSession, ComponentModel componentModel) {
        return new CustomUserProvider(keycloakSession, componentModel, userRepository, passwordEncoder);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getHelpText() {
        return "Custom user provider for PostgreSQL federation";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configMetadata;
    }
}
