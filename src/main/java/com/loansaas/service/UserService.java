package com.loansaas.service;

import com.loansaas.entity.Lender;
import com.loansaas.entity.User;
import com.loansaas.repository.LenderRepository;
import com.loansaas.repository.UserRepository;
import com.loansaas.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final LenderRepository lenderRepository;
    private final PasswordEncoder passwordEncoder;

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails cud) {
            return userRepository.findById(cud.getId())
                    .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        }
        throw new IllegalStateException("No authenticated user");
    }

    public Lender getCurrentLender() {
        User user = getCurrentUser();
        return lenderRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("Lender profile not found for current user"));
    }

    public boolean changePassword(User user, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }
}
