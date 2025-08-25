package com.acf.careerfinder.service;

import com.acf.careerfinder.model.UserData;
import com.acf.careerfinder.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Optional;

@Service
public class UserService {

    @Autowired private UserRepository repository;

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class EmailAlreadyExistsException extends RuntimeException {
        public EmailAlreadyExistsException(String email) {
            super("A user with email '" + email + "' already exists");
        }
    }

    public UserData createUser(UserData user) {
        if (repository.existsById(user.getEmail())) {
            throw new EmailAlreadyExistsException(user.getEmail());
        }
        return repository.save(user);
    }

    public Optional<UserData> findByEmail(String email) {
        return repository.findById(email);
    }
}
