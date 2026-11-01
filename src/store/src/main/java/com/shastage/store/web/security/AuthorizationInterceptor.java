package com.shastage.store.web.security;

import com.shastage.store.client.AuthGrpcClient;
import io.grpc.StatusRuntimeException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthorizationInterceptor implements HandlerInterceptor {

    private final AuthGrpcClient authGrpcClient;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        boolean requiresAuthorization =
                handlerMethod.hasMethodAnnotation(Authorize.class)
                        || AnnotatedElementUtils.hasAnnotation(
                        handlerMethod.getBeanType(),
                        Authorize.class
                );

        if (!requiresAuthorization) {
            return true;
        }

        String authorization = request.getHeader("Authorization");

        try {
            boolean valid = authGrpcClient.validateToken(authorization);

            if (!valid) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            return true;
        } catch (StatusRuntimeException ex) {
            int status = switch (ex.getStatus().getCode()) {
                case UNAUTHENTICATED -> HttpServletResponse.SC_UNAUTHORIZED;
                case UNAVAILABLE -> HttpServletResponse.SC_SERVICE_UNAVAILABLE;
                case DEADLINE_EXCEEDED -> HttpServletResponse.SC_GATEWAY_TIMEOUT;
                default -> HttpServletResponse.SC_BAD_GATEWAY;
            };

            response.setStatus(status);
            return false;
        }
    }
}
