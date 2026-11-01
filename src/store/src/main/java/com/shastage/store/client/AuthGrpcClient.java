package com.shastage.store.client;

import app.generated.*;
import com.google.protobuf.Empty;
import io.grpc.ClientInterceptor;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.MetadataUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class AuthGrpcClient {

    private final AuthServiceGrpc.AuthServiceBlockingStub stub;
    private static final Metadata.Key<String> AUTHORIZATION_KEY =
            Metadata.Key.of(
                    "authorization",
                    Metadata.ASCII_STRING_MARSHALLER
            );

    @Value("${auth.grpc.timeout-ms}")
    private long timeoutMs;

    public TokenResponse login(String email, String password) {
        LoginRequest request = LoginRequest.newBuilder()
                .setEmail(email)
                .setPassword(password)
                .build();

        return stub
                .withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS)
                .login(request);
    }

    public TokenResponse register(
            String email,
            String name,
            String surname,
            String number,
            String password
    ) {
        RegisterRequest request = RegisterRequest.newBuilder()
                .setEmail(email)
                .setName(name)
                .setSurname(surname)
                .setNumber(number)
                .setPassword(password)
                .build();

        return stub
                .withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS)
                .register(request);
    }

    public ResetPasswordResponse resetPassword(String email) {
        ResetPasswordRequest request = ResetPasswordRequest.newBuilder()
                .setEmail(email)
                .build();

        return stub
                .withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS)
                .resetPassword(request);
    }

    public boolean validateToken(String authorization) {
        var response = stubWithAuthorizationHeader(authorization)
                .validateToken(Empty.getDefaultInstance());

        return response.getIsValid();
    }

    public TokenResponse changePassword(
            String authorization,
            String oldPassword,
            String newPassword
    ) {
        ChangePasswordRequest request = ChangePasswordRequest.newBuilder()
                .setOldPassword(oldPassword)
                .setNewPassword(newPassword)
                .build();

        return stubWithAuthorizationHeader(authorization)
                .changePassword(request);
    }

    private AuthServiceGrpc.AuthServiceBlockingStub stubWithAuthorizationHeader(
            String authorization
    ) {
        if (authorization == null
                || !authorization.startsWith("Bearer ")
                || authorization.substring(7).isBlank()) {
            throw Status.UNAUTHENTICATED
                    .withDescription("Bearer token is required")
                    .asRuntimeException();
        }

        Metadata metadata = new Metadata();
        metadata.put(AUTHORIZATION_KEY, authorization);

        ClientInterceptor interceptorWithHeaders = MetadataUtils.newAttachHeadersInterceptor(metadata);

        return stub
                .withInterceptors(interceptorWithHeaders)
                .withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS);
    }
}
