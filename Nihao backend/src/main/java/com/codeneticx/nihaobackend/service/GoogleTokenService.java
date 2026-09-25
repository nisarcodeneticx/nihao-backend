package com.codeneticx.nihaobackend.service;

import com.codeneticx.nihaobackend.exception.BadRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GoogleTokenService {

    private static final String TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo";

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.google.client-id:}")
    private String googleClientId;

    public GoogleTokenService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public GoogleProfile verify(String idToken) {
        if (googleClientId == null || googleClientId.isBlank()) {
            throw new BadRequestException("Google Sign-In server پر ترتیب نہیں دیا گیا");
        }
        if (idToken == null || idToken.isBlank()) {
            throw new BadRequestException("Google ٹوکن درست نہیں");
        }
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    TOKEN_INFO_URL + "?id_token={token}",
                    String.class,
                    idToken
            );
            JsonNode payload = objectMapper.readTree(response.getBody());
            if (payload == null || payload.has("error") || payload.has("error_description")) {
                throw new BadRequestException("Google ٹوکن درست نہیں");
            }

            String audience = text(payload, "aud");
            if (!googleClientId.trim().equals(audience)) {
                throw new BadRequestException("Google ٹوکن درست نہیں");
            }
            String issuer = text(payload, "iss");
            if (!"https://accounts.google.com".equals(issuer) && !"accounts.google.com".equals(issuer)) {
                throw new BadRequestException("Google ٹوکن درست نہیں");
            }
            if (!"true".equalsIgnoreCase(text(payload, "email_verified"))) {
                throw new BadRequestException("Google ای میل تصدیق شدہ نہیں");
            }

            String email = text(payload, "email");
            if (email == null || !email.contains("@")) {
                throw new BadRequestException("Google اکاؤنٹ سے ای میل نہیں ملی");
            }
            email = email.trim().toLowerCase();
            String name = defaultString(text(payload, "name"), email.substring(0, email.indexOf('@')));
            String subject = text(payload, "sub");
            if (subject == null || subject.isBlank()) {
                throw new BadRequestException("Google ٹوکن درست نہیں");
            }
            return new GoogleProfile(subject, email, name, text(payload, "picture"));
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadRequestException("Google ٹوکن تصدیق نہیں ہو سکا");
        }
    }

    private String text(JsonNode payload, String field) {
        JsonNode node = payload.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value == null || value.isBlank() ? null : value;
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    public record GoogleProfile(String subject, String email, String name, String pictureUrl) {
    }
}
