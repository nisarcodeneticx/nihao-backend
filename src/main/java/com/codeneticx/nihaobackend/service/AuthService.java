package com.codeneticx.nihaobackend.service;

import com.codeneticx.nihaobackend.config.JwtTokenProvider;
import com.codeneticx.nihaobackend.dto.request.AdminLoginRequest;
import com.codeneticx.nihaobackend.dto.request.GoogleAuthRequest;
import com.codeneticx.nihaobackend.dto.request.MobileLoginRequest;
import com.codeneticx.nihaobackend.dto.request.MobileRegisterRequest;
import com.codeneticx.nihaobackend.dto.response.AdminAuthResponse;
import com.codeneticx.nihaobackend.dto.response.MobileAuthResponse;
import com.codeneticx.nihaobackend.exception.BadRequestException;
import com.codeneticx.nihaobackend.model.User;
import com.codeneticx.nihaobackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String PROVIDER_PASSWORD = "PASSWORD";
    private static final String PROVIDER_GOOGLE = "GOOGLE";
    private static final String PROVIDER_BOTH = "BOTH";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserProgressService userProgressService;
    private final GoogleTokenService googleTokenService;
    private final CurrentUserService currentUserService;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    @Transactional
    public AdminAuthResponse adminLogin(AdminLoginRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.getEmail()))
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BadRequestException("Account is not active");
        }

        if (!"ADMIN".equalsIgnoreCase(user.getRole()) && !"EDITOR".equalsIgnoreCase(user.getRole())) {
            throw new BadRequestException("Admin access required");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid email or password");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getEmail());

        return AdminAuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .expiresIn(jwtExpiration)
                .build();
    }

    @Transactional
    public MobileAuthResponse mobileLogin(MobileLoginRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.getEmail()))
                .orElseThrow(() -> new BadRequestException("ای میل یا پاس ورڈ غلط ہے"));

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BadRequestException("اکاؤنٹ فعال نہیں");
        }

        if (PROVIDER_GOOGLE.equalsIgnoreCase(user.getAuthProvider())
                || user.getPasswordHash() == null
                || user.getPasswordHash().isBlank()) {
            throw new BadRequestException("اس اکاؤنٹ کے لیے Google سے سائن ان کریں");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("ای میل یا پاس ورڈ غلط ہے");
        }

        return issueMobileAuth(user);
    }

    @Transactional
    public MobileAuthResponse mobileRegister(MobileRegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        String fullName = request.getFullName().trim();
        if (fullName.isBlank()) {
            throw new BadRequestException("نام لکھیں");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("یہ ای میل پہلے سے رجسٹرڈ ہے");
        }

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username(uniqueUsername(email.substring(0, email.indexOf('@'))))
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(fullName)
                .role("STUDENT")
                .status("ACTIVE")
                .authProvider(PROVIDER_PASSWORD)
                .build();
        userRepository.save(user);
        return issueMobileAuth(user);
    }

    @Transactional
    public MobileAuthResponse googleLogin(GoogleAuthRequest request) {
        GoogleTokenService.GoogleProfile profile = googleTokenService.verify(request.getIdToken());

        Optional<User> byGoogle = userRepository.findByGoogleId(profile.subject());
        User user = byGoogle.orElseGet(() -> userRepository.findByEmail(profile.email()).orElse(null));

        if (user == null) {
            user = User.builder()
                    .id(UUID.randomUUID().toString())
                    .username(uniqueUsername(profile.email().substring(0, profile.email().indexOf('@'))))
                    .email(profile.email())
                    .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .fullName(truncate(profile.name(), 100))
                    .role("STUDENT")
                    .status("ACTIVE")
                    .googleId(profile.subject())
                    .authProvider(PROVIDER_GOOGLE)
                    .avatarUrl(profile.pictureUrl())
                    .build();
        } else {
            if (!"ACTIVE".equals(user.getStatus())) {
                throw new BadRequestException("اکاؤنٹ فعال نہیں");
            }
            if (user.getGoogleId() == null || user.getGoogleId().isBlank()) {
                user.setGoogleId(profile.subject());
                user.setAuthProvider(hasUsablePassword(user) ? PROVIDER_BOTH : PROVIDER_GOOGLE);
            } else if (!profile.subject().equals(user.getGoogleId())) {
                throw new BadRequestException("یہ ای میل دوسرے اکاؤنٹ سے منسلک ہے");
            }
            if ((user.getFullName() == null || user.getFullName().isBlank()) && profile.name() != null) {
                user.setFullName(truncate(profile.name(), 100));
            }
            if (profile.pictureUrl() != null && !profile.pictureUrl().isBlank()) {
                user.setAvatarUrl(profile.pictureUrl());
            }
        }

        userRepository.save(user);
        return issueMobileAuth(user);
    }

    public MobileAuthResponse currentMobileSession() {
        return toMobileAuth(currentUserService.getCurrentUser());
    }

    private MobileAuthResponse issueMobileAuth(User user) {
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        return toMobileAuth(user);
    }

    private MobileAuthResponse toMobileAuth(User user) {
        String token = jwtTokenProvider.generateToken(user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());
        return MobileAuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(defaultString(user.getFullName(), user.getUsername()))
                .role(user.getRole())
                .status(user.getStatus())
                .expiresIn(jwtExpiration)
                .progress(userProgressService.getUserProgressDto(user.getId()))
                .build();
    }

    private String uniqueUsername(String seed) {
        String base = seed.replaceAll("[^a-zA-Z0-9._]", "").toLowerCase(Locale.ROOT);
        if (base.length() < 3) {
            base = "user" + base;
        }
        if (base.length() > 32) {
            base = base.substring(0, 32);
        }
        String candidate = base;
        int suffix = 0;
        while (userRepository.existsByUsername(candidate)) {
            suffix++;
            String ending = String.valueOf(suffix);
            candidate = base.substring(0, Math.min(base.length(), 50 - ending.length())) + ending;
        }
        return candidate;
    }

    private boolean hasUsablePassword(User user) {
        return user.getPasswordHash() != null
                && !user.getPasswordHash().isBlank()
                && !PROVIDER_GOOGLE.equalsIgnoreCase(user.getAuthProvider());
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
