package com.loansaas.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class LenderRegistrationDto {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Business name is required")
    private String businessName;

    @Email(message = "Enter a valid email")
    private String email; // optional

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+255[0-9]{9}$", message = "Phone must be in format +255XXXXXXXXX")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;

    private MultipartFile profilePhoto;
}
