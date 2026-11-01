package app.service;

import app.domain.UserService;
import app.generated.*;
import app.mapper.GrpcMapper;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {


    @Autowired
    private final UserService userService;

    @Autowired
    private final GrpcMapper mapper;


    @Override
    public void register(RegisterRequest request, StreamObserver<TokenResponse> response) {

        response.onNext(
            mapper.mapToTokenResponse(
                userService.createUser(
                    mapper.mapToUserDto(request)
                )
            )
        );
        response.onCompleted();
    }

    @Override
    public void login(LoginRequest request, StreamObserver<TokenResponse> response) {
        response.onNext(
            mapper.mapToTokenResponse(
                userService.loginUser(
                    mapper.mapToCredentialsDto(request)
                )
            )
        );
        response.onCompleted();
    }

    @Override
    public void changePassword(ChangePasswordRequest request, StreamObserver<TokenResponse> response) {
        response.onNext(
            mapper.mapToTokenResponse(
                userService.changePassword(
                    mapper.mapToChangePasswordDto(request)
                )
            )
        );
        response.onCompleted();
    }

    @Override
    public void resetPassword(ResetPasswordRequest request, StreamObserver<ResetPasswordResponse> response) {
        response.onNext(
            mapper.mapToResetPasswordResponse(
                userService.resetPassword(mapper.mapToResetPasswordDto(request)
                )
            )
        );
        response.onCompleted();
    }

    @Override
    public void validateToken(Empty request, StreamObserver<isValidTokenResponse> response) {
        response.onNext(mapper.mapToisValidTokenResponse(true));
        response.onCompleted();
    }
}
