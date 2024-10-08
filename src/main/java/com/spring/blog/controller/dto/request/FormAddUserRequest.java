package com.spring.blog.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

public record FormAddUserRequest(

        @Email
        @NotBlank
        String email,

        @NotBlank
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,16}$",
                message = "비밀번호 요구사항과 맞지 않습니다.")
        String password,

        @NotBlank
        @Length(min = 2, max = 20)
        String nickname,

        @NotBlank
        @Pattern(regexp = "^010\\d{8}$",
                message = "올바른 형식의 전화번호여야 합니다.")
        String phoneNumber
) {
}