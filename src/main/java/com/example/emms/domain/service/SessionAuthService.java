package com.example.emms.domain.service;

import com.example.emms.domain.model.User;
import com.example.emms.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionAuthService {

    private final UserRepository userRepository;
    private final Map<String, User> sessionStore = new HashMap<>();

    public Optional<String> authenticate(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // 简化密码验证
            if (user.getPassword().equals(password)) {
                String sessionId = UUID.randomUUID().toString();
                sessionStore.put(sessionId, user);
                return Optional.of(sessionId);
            }
        }

        return Optional.empty();
    }

    public Optional<User> getUserBySession(String sessionId) {
        return Optional.ofNullable(sessionStore.get(sessionId));
    }

    public void logout(String sessionId) {
        sessionStore.remove(sessionId);
    }
}