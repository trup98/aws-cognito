package com.learning.cognito.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CognitoUserResponse {
    private String username;
    private String status;
    private Boolean enabled;
    private Map<String, String> attributes;
}