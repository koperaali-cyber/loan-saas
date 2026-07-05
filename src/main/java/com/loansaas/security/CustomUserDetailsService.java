package com.loansaas.security;

import com.loansaas.entity.Role;
import com.loansaas.entity.User;
import com.loansaas.entity.UserStatus;
import com.loansaas.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new UsernameNotFoundException("No account found for " + phone));

        // Super admin always allowed
        if (user.getRole() == Role.SUPER_ADMIN) {
            return new CustomUserDetails(user);
        }

        // Lenders must be approved/active to log in
        UserStatus status = user.getStatus();
        if (status == UserStatus.PENDING_APPROVAL) {
            throw new DisabledException("Your account is pending Super Admin approval.");
        }
        if (status == UserStatus.REJECTED) {
            throw new DisabledException("Your registration was rejected. Please contact the administrator.");
        }
        if (status == UserStatus.SUSPENDED) {
            throw new DisabledException("Your account has been suspended. Please contact the administrator.");
        }

        return new CustomUserDetails(user);
    }
}
