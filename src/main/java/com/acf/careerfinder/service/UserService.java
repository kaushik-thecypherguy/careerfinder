package com.acf.careerfinder.service;

import com.acf.careerfinder.model.UserData;
import com.acf.careerfinder.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired private UserRepository repository;
    @Autowired private PasswordValidator passwordValidator;

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class EmailAlreadyExistsException extends RuntimeException {
        public EmailAlreadyExistsException(String email) {
            super("A user with email '" + email + "' already exists");
        }
    }

    public UserData createUser(UserData user) {
        if (user == null) throw new IllegalArgumentException("User is required");
        if (user.getUserpassword() == null) throw new IllegalArgumentException("Password is required");
        passwordValidator.validateOrThrow(user.getUserpassword());

        if (repository.existsById(user.getEmail())) {
            throw new EmailAlreadyExistsException(user.getEmail());
        }
        // IMPORTANT: do NOT set a DB default uiLang here.
        // Keep null until the user explicitly chooses on /start.
        return repository.save(user);
    }

    public Optional<UserData> findByEmail(String email) {
        return repository.findById(email);
    }

    public Optional<UserData> findByUsername(String username) {
        return repository.findByUsername(username);
    }

    /* ---------- per-account language helpers ---------- */

    public Optional<String> getUiLang(String email) {
        return repository.findById(email).map(UserData::getUiLang);
    }

    public String getUiLangOrDefault(String email) {
        return repository.findById(email)
                .map(UserData::getUiLang)
                .filter(s -> s != null && !s.isBlank())
                .orElse("en");
    }

    @Transactional
    public void setUiLang(String email, String lang) {
        if (email == null) return;
        if (!List.of("en", "hi", "mr").contains(lang)) lang = "en";
        repository.updateUiLang(email, lang);
    }

    /* ---------- one-time login ID banner ---------- */

    @Transactional
    public void markLoginIdShown(String email) {
        if (email == null) return;
        repository.markLoginIdShown(email);
    }
}