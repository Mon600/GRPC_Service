package app.mapper;

import app.domain.model.*;
import app.generated.*;
import org.springframework.stereotype.Component;

@Component
public class GrpcMapper {
    public UserDto mapToUserDto(RegisterRequest request) {
        return new UserDto(request.getEmail(), request.getName(), request.getSurname(), request.getNumber(), request.getPassword());
    }

    public CredentialsDto mapToCredentialsDto(LoginRequest request) {
        return new CredentialsDto(request.getEmail(), request.getPassword());
    }

    public ChangePasswordDto mapToChangePasswordDto(ChangePasswordRequest request) {
        return new ChangePasswordDto(request.getOldPassword(), request.getNewPassword());
    }

    public ResetPasswordDto mapToResetPasswordDto(ResetPasswordRequest request) {
        return new ResetPasswordDto(request.getEmail());
    }

    public TokenResponse mapToTokenResponse(TokenDto tokenDto) {
        return TokenResponse.newBuilder()
            .setTokenType(tokenDto.tokenType())
            .setToken(tokenDto.token())
            .build();
    }

    public ResetPasswordResponse mapToResetPasswordResponse(ResetPasswordResponseDto resetPasswordResponseDto) {
        return ResetPasswordResponse.newBuilder()
            .setMessage(resetPasswordResponseDto.message())
            .build();
    }

    public isValidTokenResponse mapToisValidTokenResponse(boolean isValid) {
        return isValidTokenResponse.newBuilder()
            .setIsValid(isValid)
            .build();
    }
}
