package app.interceptor;


import app.domain.TokenService;
import app.exceptions.InvalidTokenException;
import io.grpc.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@GlobalServerInterceptor
public class AuthorizationInterceptor implements ServerInterceptor {

    public static final Context.Key<Object> USER_ID_CONTEXT_KEY = Context.key("userId");
    private static final Metadata.Key<String> AUTHORIZATION_METADATA_KEY = Metadata.Key.of("authorization",
        Metadata.ASCII_STRING_MARSHALLER);
    private static final Set<String> PUBLIC_METHODS = Set.of(
        "AuthService/Register",
        "AuthService/Login",
        "AuthService/ResetPassword"
    );

    private final TokenService tokenService;

    public AuthorizationInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
                                                                 ServerCallHandler<ReqT, RespT> next) {
        if (PUBLIC_METHODS.contains(call.getMethodDescriptor().getFullMethodName())) {
            return next.startCall(call, headers);
        }
        String authorization = headers.get(AUTHORIZATION_METADATA_KEY);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            call.close(
                    Status.UNAUTHENTICATED.withDescription("Invalid token"),
                    new Metadata()
            );
            return new ServerCall.Listener<ReqT>() {};
        }
        String token = authorization.substring(7);
        if (token.isBlank()) {
            call.close(
                    Status.UNAUTHENTICATED.withDescription("Invalid token"),
                    new Metadata()
            );
            return new ServerCall.Listener<ReqT>() {};
        }
        try {
            Claims claims = tokenService.validateToken(token);
            String userId = claims.getSubject();
            Context ctx = Context.current().withValue(USER_ID_CONTEXT_KEY, userId);
            return Contexts.interceptCall(ctx, call, headers, next);
        } catch (JwtException | InvalidTokenException e) {
            call.close(
                    Status.UNAUTHENTICATED.withDescription("Invalid token"),
                    new Metadata()
            );
            return new ServerCall.Listener<ReqT>() {};
        } catch (Exception e) {
            call.close(
                    Status.INTERNAL.withDescription("Internal server error"),
                    new Metadata()
            );
            return new ServerCall.Listener<ReqT>() {};
        }
    }
}
