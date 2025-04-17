package com.learning.cognito.dto.request;

import lombok.Data;

@Data
public class SignUpUserDTO {

    private String userName;
    private String passWord;
    private String email;
    private String phoneNumber;
}
