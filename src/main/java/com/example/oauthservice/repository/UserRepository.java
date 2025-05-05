package com.example.oauthservice.repository;

import com.example.oauthservice.entity.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UsersEntity, Long> {
    Optional<UsersEntity> findByUsername(String username);
} 