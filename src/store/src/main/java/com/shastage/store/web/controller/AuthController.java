package com.shastage.store.web.controller;

import app.generated.TokenResponse;
import com.shastage.store.client.AuthGrpcClient;
import com.shastage.store.web.dto.request.ChangePasswordRequest;
import com.shastage.store.web.dto.request.LoginRequest;
import com.shastage.store.web.dto.request.RegisterRequest;
import com.shastage.store.web.dto.request.ResetPasswordRequest;
import com.shastage.store.web.dto.response.LoginResponse;
import com.shastage.store.web.dto.response.ResetPasswordResponse;
import com.shastage.store.web.dto.response.ValidateTokenResponse;
import com.shastage.store.web.mapper.AuthWebMapper;
import com.shastage.store.web.security.Authorize;
import io.grpc.StatusRuntimeException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthGrpcClient authGrpcClient;
    private final AuthWebMapper authWebMapper;

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        TokenResponse tokenResponse = authGrpcClient.login(
                request.email(),
                request.password()
        );

        return authWebMapper.toLoginResponse(tokenResponse);
    }

    @PostMapping("/register")
    public LoginResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {

        TokenResponse tokenResponse = authGrpcClient.register(
                request.email(),
                request.name(),
                request.surname(),
                request.number(),
                request.password()
        );

        return authWebMapper.toLoginResponse(tokenResponse);
    }

    @PostMapping("/reset")
    public ResetPasswordResponse resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {

        var resetPasswordResponse = authGrpcClient.resetPassword(
                request.email()
        );

        return new ResetPasswordResponse(resetPasswordResponse.getMessage());
    }

    @PostMapping("/validate")
    public ValidateTokenResponse validateToken(
            @RequestHeader(
                    name = "Authorization",
                    required = false
            ) String authorization
    ) {
        boolean valid = authGrpcClient.validateToken(authorization);

        return new ValidateTokenResponse(valid);
    }

    @Authorize
    @PostMapping("/change-password")
    public LoginResponse changePassword(
            @RequestHeader(name = "Authorization", required = false)
            String authorization,
            @Valid
            @RequestBody
            ChangePasswordRequest request
    ) {

        TokenResponse tokenResponse = authGrpcClient.changePassword(
                authorization,
                request.oldPassword(),
                request.newPassword()
        );

        return authWebMapper.toLoginResponse(tokenResponse);
    }

    @ExceptionHandler(StatusRuntimeException.class)
    public ProblemDetail handleGrpcError(StatusRuntimeException ex) {
        HttpStatus status;
        String detail;

        switch (ex.getStatus().getCode()) {
            case NOT_FOUND -> {
                status = HttpStatus.UNAUTHORIZED;
                detail = "Invalid email or password";
            }
            case UNAUTHENTICATED -> {
                status = HttpStatus.UNAUTHORIZED;
                detail = "Bearer token is missing or invalid";
            }
            case INVALID_ARGUMENT -> {
                status = HttpStatus.BAD_REQUEST;
                detail = "Invalid authentication request";
            }
            case UNAVAILABLE -> {
                status = HttpStatus.SERVICE_UNAVAILABLE;
                detail = "Authentication service is unavailable";
            }
            case DEADLINE_EXCEEDED -> {
                status = HttpStatus.GATEWAY_TIMEOUT;
                detail = "Authentication service did not respond in time";
            }
            case ALREADY_EXISTS -> {
                status = HttpStatus.CONFLICT;
                detail = "Email or phone is already registered";
            }
            default -> {
                status = HttpStatus.BAD_GATEWAY;
                detail = "Authentication service request failed";
            }
        }

        return ProblemDetail.forStatusAndDetail(status, detail);
    }
}