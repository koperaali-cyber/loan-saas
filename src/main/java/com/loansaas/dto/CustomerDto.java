package com.loansaas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class CustomerDto {

    private Long id;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+255[0-9]{9}$", message = "Phone must be in format +255XXXXXXXXX")
    private String phone;

    @Pattern(regexp = "^(\\+255[0-9]{9})?$", message = "Alternative phone must be in format +255XXXXXXXXX")
    private String alternativePhone;

    private String nida;

    private MultipartFile photoFile;
    private String existingPhoto;
}
