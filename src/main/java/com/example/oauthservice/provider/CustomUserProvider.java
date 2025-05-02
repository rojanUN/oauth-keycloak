package com.example.oauthservice.provider;

import com.example.oauthservice.entity.UserEntity;
import com.example.oauthservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
public class CustomUserProvider implements UserStorageProvider, UserLookupProvider, CredentialInputValidator, CredentialInputUpdater {

    private final KeycloakSession session;
    private final ComponentModel model;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    protected Map<String, UserModel> loadedUsers = new HashMap<>();

    public CustomUserProvider(KeycloakSession session, ComponentModel componentModel, 
                            UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.session = session;
        this.model = componentModel;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean updateCredential(RealmModel realmModel, UserModel userModel, CredentialInput credentialInput) {
        if (!(credentialInput instanceof UserCredentialModel)) {
            return false;
        }

        UserCredentialModel cred = (UserCredentialModel) credentialInput;
        Optional<UserEntity> userOpt = userRepository.findById(Long.parseLong(StorageId.externalId(userModel.getId())));
        
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            user.setPassword(passwordEncoder.encode(cred.getChallengeResponse()));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public void disableCredentialType(RealmModel realmModel, UserModel userModel, String s) {
        // Not implemented
    }

    @Override
    public Stream<String> getDisableableCredentialTypesStream(RealmModel realmModel, UserModel userModel) {
        return Stream.empty();
    }

    @Override
    public boolean supportsCredentialType(String s) {
        return PasswordCredentialModel.TYPE.equals(s);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realmModel, UserModel userModel, String s) {
        return supportsCredentialType(s);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) {
            return false;
        }

        Optional<UserEntity> userOpt = userRepository.findById(Long.parseLong(StorageId.externalId(user.getId())));
        if (userOpt.isEmpty()) {
            return false;
        }

        UserEntity userEntity = userOpt.get();
        UserCredentialModel cred = (UserCredentialModel) input;
        return passwordEncoder.matches(cred.getChallengeResponse(), userEntity.getPassword());
    }

    @Override
    public void close() {
        // Clean up resources if needed
    }

    @Override
    public UserModel getUserById(RealmModel realmModel, String id) {
        String externalId = StorageId.externalId(id);
        return findUser(realmModel, externalId);
    }

    @Override
    public UserModel getUserByUsername(RealmModel realmModel, String username) {
        return userRepository.findByUsername(username)
                .map(user -> new UserAdapter(session, realmModel, model, user))
                .orElse(null);
    }

    @Override
    public UserModel getUserByEmail(RealmModel realmModel, String email) {
        // Since UserEntity doesn't have email, we'll return null
        return null;
    }

    private UserModel findUser(RealmModel realmModel, String identifier) {
        UserModel adapter = loadedUsers.get(identifier);
        if (adapter == null) {
            try {
                Optional<UserEntity> userOpt = userRepository.findById(Long.parseLong(identifier));
                if (userOpt.isPresent()) {
                    UserEntity user = userOpt.get();
                    adapter = new UserAdapter(session, realmModel, model, user);
                    loadedUsers.put(identifier, adapter);
                }
            } catch (Exception e) {
                log.warn("User with identifier '{}' could not be found", identifier, e);
            }
        }
        return adapter;
    }
}
