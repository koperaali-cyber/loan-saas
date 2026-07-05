package com.loansaas.service;

import com.loansaas.dto.LenderRegistrationDto;
import com.loansaas.entity.*;
import com.loansaas.repository.LenderRepository;
import com.loansaas.repository.UserRepository;
import com.loansaas.util.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LenderService {

    private final UserRepository userRepository;
    private final LenderRepository lenderRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final ActivityService activityService;

    public boolean phoneExists(String phone) {
        return userRepository.existsByPhone(phone);
    }

    public boolean emailExists(String email) {
        return email != null && !email.isBlank() && userRepository.existsByEmail(email);
    }

    @Transactional
    public Lender register(LenderRegistrationDto dto) {
        String photoPath = fileStorageService.store(dto.getProfilePhoto(), "lenders");

        User user = User.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail() == null || dto.getEmail().isBlank() ? null : dto.getEmail())
                .phone(dto.getPhone())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Role.LENDER)
                .status(UserStatus.PENDING_APPROVAL)
                .profileImage(photoPath)
                .build();
        user = userRepository.save(user);

        Lender lender = Lender.builder()
                .user(user)
                .businessName(dto.getBusinessName())
                .approvalStatus(UserStatus.PENDING_APPROVAL)
                .build();
        lender = lenderRepository.save(lender);

        activityService.log(user.getId(), user.getFullName(),
                "Registered as a new lender (" + dto.getBusinessName() + ") - pending approval");
        return lender;
    }

    public List<Lender> findAll() {
        return lenderRepository.findAll();
    }

    public List<Lender> findByStatus(UserStatus status) {
        return lenderRepository.findByApprovalStatus(status);
    }

    public Lender findById(Long id) {
        return lenderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lender not found: " + id));
    }

    @Transactional
    public void approve(Long lenderId, String adminName) {
        Lender lender = findById(lenderId);
        lender.setApprovalStatus(UserStatus.APPROVED);
        lender.setRejectionReason(null);
        lender.getUser().setStatus(UserStatus.APPROVED);
        lenderRepository.save(lender);
        activityService.log(null, adminName,
                "Approved lender: " + lender.getBusinessName());
    }

    @Transactional
    public void reject(Long lenderId, String reason, String adminName) {
        Lender lender = findById(lenderId);
        lender.setApprovalStatus(UserStatus.REJECTED);
        lender.setRejectionReason(reason);
        lender.getUser().setStatus(UserStatus.REJECTED);
        lenderRepository.save(lender);
        activityService.log(null, adminName,
                "Rejected lender: " + lender.getBusinessName() + " (Reason: " + reason + ")");
    }

    @Transactional
    public void suspend(Long lenderId, String adminName) {
        Lender lender = findById(lenderId);
        lender.setApprovalStatus(UserStatus.SUSPENDED);
        lender.getUser().setStatus(UserStatus.SUSPENDED);
        lenderRepository.save(lender);
        activityService.log(null, adminName,
                "Suspended lender: " + lender.getBusinessName());
    }

    @Transactional
    public void activate(Long lenderId, String adminName) {
        Lender lender = findById(lenderId);
        lender.setApprovalStatus(UserStatus.APPROVED);
        lender.getUser().setStatus(UserStatus.APPROVED);
        lenderRepository.save(lender);
        activityService.log(null, adminName,
                "Activated lender: " + lender.getBusinessName());
    }

    @Transactional
    public void delete(Long lenderId, String adminName) {
        Lender lender = findById(lenderId);
        String name = lender.getBusinessName();
        // Deleting the user cascades to lender via the User.lender cascade mapping
        User user = lender.getUser();
        lenderRepository.delete(lender);
        userRepository.delete(user);
        activityService.log(null, adminName, "Deleted lender account: " + name);
    }

    @Transactional
    public void resetPassword(Long lenderId, String newPassword, String adminName) {
        Lender lender = findById(lenderId);
        lender.getUser().setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(lender.getUser());
        activityService.log(null, adminName,
                "Reset password for lender: " + lender.getBusinessName());
    }
}
