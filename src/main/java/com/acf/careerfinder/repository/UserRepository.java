package com.acf.careerfinder.repository;

import com.acf.careerfinder.model.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserData, String> {

    Optional<UserData> findByUsername(String username);

    @Modifying
    @Query("update UserData u set u.loginIdShownAt = CURRENT_TIMESTAMP where u.email = :email")
    int markLoginIdShown(@Param("email") String email);

    @Modifying
    @Query("update UserData u set u.uiLang = :lang where u.email = :email")
    int updateUiLang(@Param("email") String email, @Param("lang") String lang);
}



