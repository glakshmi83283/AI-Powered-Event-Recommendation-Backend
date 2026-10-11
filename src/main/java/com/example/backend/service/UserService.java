
package com.example.backend.service;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User saveUser(User user) {
        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User loginUser(String email, String password) {
        List<User> users = userRepository.findAllByEmail(email);

        for (User user : users) {
            if (user.getPassword() == null) {
                continue;
            }

            String storedPassword = user.getPassword();

            if (passwordEncoder.matches(password, storedPassword)) {
                return user;
            }

            // Support older accounts with plain-text passwords.
            if (!storedPassword.startsWith("$2")
                    && storedPassword.equals(password)) {
                user.setPassword(passwordEncoder.encode(password));
                return userRepository.save(user);
            }
        }

        return null;
    }
}
