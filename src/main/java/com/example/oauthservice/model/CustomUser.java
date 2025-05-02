package com.example.oauthservice.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.models.UserModel;
import org.keycloak.storage.adapter.AbstractInMemoryUserAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class CustomUser extends AbstractInMemoryUserAdapter {
    public static final String ID = "id";
    public static final String EMAIL = "email";
    public static final String LOGIN_NAME = "loginName";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    @Column(unique = true)
    private String email;

    private String lastName;

    @Column(unique = true)
    private String loginName;

    private String firstName;

    @Column(nullable = false)
    private String password;

    private final Set<String> requiredActions = new HashSet<>();

    @Builder
    public CustomUser(KeycloakSession session, RealmModel realm, ComponentModel storageProviderModel, 
                     UUID userId, String loginName, String email, String firstName, String lastName, String password) {
        super(session, realm, String.valueOf(storageProviderModel));
        this.userId = userId;
        this.loginName = loginName;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
    }

    @Override
    public String getUsername() {
        return loginName;
    }

    @Override
    public SubjectCredentialManager credentialManager() {
        UserModel userModel1 = session.users().getUserById(realm, userId.toString());
        return userModel1.credentialManager();
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
            case ID -> Stream.of(String.valueOf(userId));
            case LOGIN_NAME -> Stream.of(loginName);
            case EMAIL -> Stream.of(email);
            default -> Stream.empty();
        };
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        MultivaluedHashMap<String, String> attributeMap = new MultivaluedHashMap<>();
        attributeMap.add(ID, String.valueOf(userId));
        attributeMap.add(LOGIN_NAME, loginName);
        attributeMap.add(EMAIL, email);
        return attributeMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomUser that = (CustomUser) o;
        return Objects.equals(userId, that.userId) && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, email);
    }
}
