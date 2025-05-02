package com.example.oauthservice.provider;

import com.example.oauthservice.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.models.UserModel;
import org.keycloak.storage.adapter.AbstractInMemoryUserAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
public class UserAdapter extends AbstractInMemoryUserAdapter {
    private final UserEntity user;
    private final Set<String> requiredActions = new HashSet<>();

    public UserAdapter(KeycloakSession session, RealmModel realm, ComponentModel storageProviderModel, UserEntity user) {
        super(session, realm, String.valueOf(storageProviderModel));
        this.user = user;
    }

    @Override
    public String getId() {
        return String.valueOf(user.getId());
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public void setUsername(String username) {
        user.setUsername(username);
    }

    @Override
    public boolean isEnabled() {
        return user.getEnabled() != null && user.getEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        user.setEnabled(enabled);
    }

    @Override
    public boolean isEmailVerified() {
        return false; // Since UserEntity doesn't have email verification
    }

    @Override
    public void setEmailVerified(boolean verified) {
        // Not implemented since UserEntity doesn't have email verification
    }

    @Override
    public SubjectCredentialManager credentialManager() {
        return new SubjectCredentialManager() {
            @Override
            public boolean isValid(List<CredentialInput> inputs) {
                if (inputs == null || inputs.isEmpty()) {
                    return false;
                }
                // We only support password credentials
                return inputs.stream()
                        .filter(input -> "password".equals(input.getType()))
                        .anyMatch(input -> input.getChallengeResponse().equals(user.getPassword()));
            }

            @Override
            public boolean updateCredential(CredentialInput input) {
                if ("password".equals(input.getType())) {
                    user.setPassword(input.getChallengeResponse());
                    return true;
                }
                return false;
            }

            @Override
            public void updateStoredCredential(CredentialModel model) {
                if ("password".equals(model.getType())) {
                    user.setPassword(model.getSecretData());
                }
            }

            @Override
            public CredentialModel createStoredCredential(CredentialModel model) {
                if ("password".equals(model.getType())) {
                    user.setPassword(model.getSecretData());
                    return model;
                }
                return null;
            }

            @Override
            public boolean removeStoredCredentialById(String id) {
                // Not implemented as we only store password
                return false;
            }

            @Override
            public CredentialModel getStoredCredentialById(String id) {
                // Not implemented as we only store password
                return null;
            }

            @Override
            public Stream<CredentialModel> getStoredCredentialsStream() {
                // Return empty stream as we only store password in the user entity
                return Stream.empty();
            }

            @Override
            public Stream<CredentialModel> getStoredCredentialsByTypeStream(String type) {
                if ("password".equals(type)) {
                    CredentialModel model = new CredentialModel();
                    model.setType(type);
                    model.setSecretData(user.getPassword());
                    return Stream.of(model);
                }
                return Stream.empty();
            }

            @Override
            public CredentialModel getStoredCredentialByNameAndType(String name, String type) {
                if ("password".equals(type)) {
                    CredentialModel model = new CredentialModel();
                    model.setType(type);
                    model.setSecretData(user.getPassword());
                    return model;
                }
                return null;
            }

            @Override
            public boolean moveStoredCredentialTo(String id, String newPreviousCredentialId) {
                // Not implemented as we only store password
                return false;
            }

            @Override
            public void updateCredentialLabel(String id, String label) {
                // Not implemented as we only store password
            }

            @Override
            public void disableCredentialType(String type) {
                // Not implemented as we only store password
            }

            @Override
            public Stream<String> getDisableableCredentialTypesStream() {
                return Stream.empty();
            }

            @Override
            public boolean isConfiguredFor(String type) {
                return "password".equals(type);
            }

            @Override
            public boolean isConfiguredLocally(String type) {
                return "password".equals(type);
            }

            @Override
            public Stream<String> getConfiguredUserStorageCredentialTypesStream() {
                return Stream.of("password");
            }

            @Override
            public CredentialModel createCredentialThroughProvider(CredentialModel model) {
                if ("password".equals(model.getType())) {
                    user.setPassword(model.getSecretData());
                    return model;
                }
                return null;
            }
        };
    }

    @Override
    public void removeRequiredAction(String action) {
        log.debug("removeRequiredAction called, action {}", action);
        requiredActions.remove(action);
    }

    @Override
    public void addRequiredAction(String action) {
        log.debug("addRequiredAction called, action {}", action);
        requiredActions.add(action);
    }

    @Override
    public Stream<String> getRequiredActionsStream() {
        log.debug("getRequiredActionsStream called");
        return this.requiredActions.stream();
    }

    @Override
    public String getFirstAttribute(String name) {
        List<String> list = getAttributes().getOrDefault(name, List.of());
        return list.isEmpty() ? null : list.getFirst();
    }

    @Override
    public void setAttribute(String name, List<String> values) {
        super.setAttribute(name, values);
    }

    @Override
    public Stream<String> getAttributeStream(String name) {
        return switch (name) {
            case "id" -> Stream.of(String.valueOf(user.getId()));
            case "username" -> Stream.of(user.getUsername());
            case "clientId" -> Stream.of(user.getClientId());
            case "status" -> Stream.of(user.getStatus() != null ? user.getStatus().name() : null);
            default -> Stream.empty();
        };
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        MultivaluedHashMap<String, String> attributeMap = new MultivaluedHashMap<>();
        attributeMap.add("id", String.valueOf(user.getId()));
        attributeMap.add("username", user.getUsername());
        if (user.getClientId() != null) {
            attributeMap.add("clientId", user.getClientId());
        }
        if (user.getStatus() != null) {
            attributeMap.add("status", user.getStatus().name());
        }
        return attributeMap;
    }
} 