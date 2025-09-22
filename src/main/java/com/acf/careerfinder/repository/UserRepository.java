package com.acf.careerfinder.repository;

import com.acf.careerfinder.model.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserData, String> {
    Optional<UserData> findByUsername(String username);
}



