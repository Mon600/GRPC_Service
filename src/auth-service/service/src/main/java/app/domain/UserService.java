package app.domain;

import app.datalayer.model.UserEntity;
import app.datalayer.repository.UserRepository;
import app.domain.model.*;
import app.exceptions.UserAlreadyExistsException;
import app.exceptions.UserNotFoundException;
import app.util.ContextHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class UserService {
    @Autowired
    private final PasswordService passwordService;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final TokenService tokenService;

    @Transactional(rollbackFor = Exception.class)
    public TokenDto createUser(UserDto user) {
        if (userRepository.existsByEmail(user.email()) || userRepository.existsByPhone(user.phone())) {
            throw new UserAlreadyExistsException("Registration failed. Use another email or phone.");
        }
        String hashedPassword = passwordService.hashPassword(user.password());
        UserEntity newUser = UserEntity.builder()
            .email(user.email())
            .name(user.name())
            .surname(user.surname())
            .phone(user.phone())
            .password(hashedPassword)
            .build();
        userRepository.save(newUser);
        String token = tokenService.generateToken(newUser.getId(), newUser.getEmail());
        return new TokenDto("Bearer", token);
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public TokenDto loginUser(CredentialsDto credentials) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(credentials.email());
        String passwordToCheck = userOpt.map(UserEntity::getPassword).orElseGet(
            () -> passwordService.hashPassword("123")
        );
        boolean passwordCheck = passwordService.verifyPassword(credentials.password(), passwordToCheck);
        if (userOpt.isEmpty() || !passwordCheck) {
            throw new UserNotFoundException("Login failed. Check your email and password.");
        }
        UserEntity user = userOpt.get();
        String token = tokenService.generateToken(user.getId(), user.getEmail());
        return new TokenDto("Bearer", token);
    }

    @Transactional(rollbackFor = Exception.class)
    public TokenDto changePassword(ChangePasswordDto changePasswordDto) {
        UUID userId = UUID.fromString(ContextHelper.getUserIdFromContext());
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found."));
        if (!passwordService.verifyPassword(changePasswordDto.oldPassword(), user.getPassword())) {
            throw new UserNotFoundException("Change password failed. Check your old password.");
        }

        String hashedPassword = passwordService.hashPassword(changePasswordDto.newPassword());
        user.setPassword(hashedPassword);
        userRepository.save(user);
        String token = tokenService.generateToken(user.getId(), user.getEmail());
        return new TokenDto("Bearer", token);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResetPasswordResponseDto resetPassword(ResetPasswordDto resetPasswordDto) {
        String message = String.format("If the email %s is exists, a password reset link will be sent to you.",
            resetPasswordDto.email());

        Optional<UserEntity> userOpt = userRepository.findByEmail(resetPasswordDto.email());
        if (userOpt.isPresent()) {
            String newPassword = passwordService.generatePassword();

            System.out.println("New password: " + newPassword);
            UserEntity user = userOpt.get();
            user.setPassword(passwordService.hashPassword(newPassword));
            userRepository.save(user);
            return new ResetPasswordResponseDto(message);
        } else {
            String newPassword = passwordService.generatePassword();
            passwordService.hashPassword(newPassword);
        }
        return new ResetPasswordResponseDto(message);
    }
}
