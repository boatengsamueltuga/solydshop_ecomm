package com.solydshop.ecommerce.service;

import com.solydshop.ecommerce.entity.RefreshToken;
import com.solydshop.ecommerce.entity.User;
import com.solydshop.ecommerce.repository.RefreshTokenRepository;
import com.solydshop.ecommerce.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private final long REFRESH_EXPIRATION = 7 * 24 * 60 * 60;

//    public RefreshToken createRefreshToken(Long userId) {
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        RefreshToken token = new RefreshToken();
//        token.setUser(user);
//        token.setToken(UUID.randomUUID().toString());
//        token.setExpiryDate(Instant.now().plusSeconds(REFRESH_EXPIRATION));
//
//        return refreshTokenRepository.save(token);
//    }

    @Transactional
    public RefreshToken createRefreshToken(Long userId) {

        // delete old tokens (enforces one session per user)
        refreshTokenRepository.deleteByUserUserId(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(Instant.now().plusSeconds(REFRESH_EXPIRATION));

        return refreshTokenRepository.save(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {

        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired");
        }

        return token;
    }
    @Transactional
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserUserId(userId);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
}
