package com.learning.cognito.dto.request;

import lombok.Data;

@Data
public class AdminCreateUserRequestDTO {
    private String username;
    private String email;
    private String temporaryPassword;
}