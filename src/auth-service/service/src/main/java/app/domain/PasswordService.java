package app.domain;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class PasswordService {
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()-_=+";
    private static final String ALL_CHARS = LOWERCASE + UPPERCASE + DIGITS + SPECIAL;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    public String hashPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Raw password cannot be null or empty");
        }
        return passwordEncoder.encode(rawPassword);
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        if (rawPassword == null || rawPassword.isEmpty() || encodedPassword == null || encodedPassword.isEmpty()) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public String generatePassword() {
        List<Character> passwordChars = new ArrayList<>();
        SecureRandom random = new SecureRandom();
        passwordChars.add(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        passwordChars.add(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        passwordChars.add(DIGITS.charAt(random.nextInt(DIGITS.length())));
        passwordChars.add(SPECIAL.charAt(random.nextInt(SPECIAL.length())));
        int length = random.nextInt(16, 21);
        for (int i = passwordChars.size(); i < length; i++) {
            passwordChars.add(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }
        Collections.shuffle(passwordChars, random);
        StringBuilder password = new StringBuilder(length);
        for (Character c : passwordChars) {
            password.append(c);
        }
        return password.toString();
    }


}
