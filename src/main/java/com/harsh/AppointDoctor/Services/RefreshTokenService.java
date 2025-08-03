package com.harsh.AppointDoctor.Services;

import com.harsh.AppointDoctor.Models.RefreshToken;
import com.harsh.AppointDoctor.Repo.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RefreshToken createRefreshToken(String username) {
        refreshTokenRepository.deleteByUsername(username);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsername(username);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7)); // 4 hours
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public boolean isValid(RefreshToken token) {
        return token.getExpiryDate().isAfter(LocalDateTime.now());
    }

    @Transactional
    public void deleteByUsername(String username) {
        refreshTokenRepository.deleteByUsername(username);
    }
}
