package com.aiaudit.platform.common.exception;

import com.aiaudit.platform.common.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // ────────────────────────────────────────────────────────────────────────
    // handleNotFound
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("handleNotFound should return 404 with exception message")
    void handleNotFound_returns404WithMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("AI System", "id", "abc-123");

        ResponseEntity<ApiResponse<Void>> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("AI System not found with id: 'abc-123'");
    }

    @Test
    @DisplayName("handleNotFound should return 404 with simple message constructor")
    void handleNotFound_simpleMessage_returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found");

        ResponseEntity<ApiResponse<Void>> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Resource not found");
    }

    // ────────────────────────────────────────────────────────────────────────
    // handleBadRequest
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("handleBadRequest should return 400 with exception message")
    void handleBadRequest_returns400WithMessage() {
        BadRequestException ex = new BadRequestException("Email already registered");

        ResponseEntity<ApiResponse<Void>> response = handler.handleBadRequest(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Email already registered");
    }

    // ────────────────────────────────────────────────────────────────────────
    // handleUnauthorized
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("handleUnauthorized should return 401 with exception message")
    void handleUnauthorized_returns401WithMessage() {
        UnauthorizedException ex = new UnauthorizedException("Invalid email or password");

        ResponseEntity<ApiResponse<Void>> response = handler.handleUnauthorized(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid email or password");
    }

    // ────────────────────────────────────────────────────────────────────────
    // handleForbidden
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("handleForbidden should return 403 with exception message")
    void handleForbidden_returns403WithMessage() {
        ForbiddenException ex = new ForbiddenException("You do not have permission");

        ResponseEntity<ApiResponse<Void>> response = handler.handleForbidden(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("You do not have permission");
    }

    // ────────────────────────────────────────────────────────────────────────
    // handleSubscriptionLimit
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("handleSubscriptionLimit should return 402 with exception message")
    void handleSubscriptionLimit_returns402WithMessage() {
        SubscriptionLimitException ex = new SubscriptionLimitException("AI system limit reached for your subscription plan");

        ResponseEntity<ApiResponse<Void>> response = handler.handleSubscriptionLimit(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYMENT_REQUIRED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("AI system limit reached for your subscription plan");
    }

    // ────────────────────────────────────────────────────────────────────────
    // handleBadCredentials
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("handleBadCredentials should return 401 with fixed message")
    void handleBadCredentials_returns401WithFixedMessage() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");

        ResponseEntity<ApiResponse<Void>> response = handler.handleBadCredentials(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid email or password");
    }

    // ────────────────────────────────────────────────────────────────────────
    // handleAccessDenied
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("handleAccessDenied should return 403 with fixed message")
    void handleAccessDenied_returns403WithFixedMessage() {
        AccessDeniedException ex = new AccessDeniedException("Access is denied");

        ResponseEntity<ApiResponse<Void>> response = handler.handleAccessDenied(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Access denied");
    }

    // ────────────────────────────────────────────────────────────────────────
    // handleGeneral
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("handleGeneral should return 500 with generic message")
    void handleGeneral_returns500WithGenericMessage() {
        Exception ex = new RuntimeException("Something went terribly wrong");

        ResponseEntity<ApiResponse<Void>> response = handler.handleGeneral(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
    }
}
