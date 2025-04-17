package com.learning.cognito.service;

import com.learning.cognito.dto.reponse.CognitoUserResponse;
import com.learning.cognito.dto.request.AdminCreateUserRequestDTO;
import com.learning.cognito.dto.request.LoginRequestDTO;
import com.learning.cognito.dto.request.SignUpUserDTO;
import com.learning.cognito.enums.CommonEnum;
import com.learning.cognito.exception.CustomException;
import com.learning.cognito.utility.CognitoSecretHash;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CognitoService {

    @Value("${aws.cognito.client-id:dummyValue}")
    private String clientId;

    @Value("${aws.cognito.client-secret:dummyValue}")
    private String clientSecret;

    @Value("${aws.cognito.user-pool-id:dummyValue}")
    private String userPoolId;

    private final CognitoIdentityProviderClient cognitoClient;

    public String signUp(SignUpUserDTO signUpUserDTO) {
        try {
            String secretHash = CognitoSecretHash.calculateSecretHash(signUpUserDTO.getUserName(), clientId, clientSecret);

            AttributeType emailAttr = AttributeType.builder()
                    .name("email")
                    .value(signUpUserDTO.getEmail())
                    .build();

            AttributeType nameFormattedAttr = AttributeType.builder()
                    .name("name")
                    .value(signUpUserDTO.getUserName())
                    .build();

            SignUpRequest signUpRequest = SignUpRequest.builder()
                    .username(signUpUserDTO.getUserName())
                    .password(signUpUserDTO.getPassWord())
                    .userAttributes(emailAttr, nameFormattedAttr)
                    .clientId(clientId)
                    .secretHash(secretHash)
                    .build();

            SignUpResponse response = cognitoClient.signUp(signUpRequest);
            return response.userSub();

        } catch (CognitoIdentityProviderException e) {
            throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public String confirmSignUp(String username, String confirmationCode) {
        try {
            String secretHash = CognitoSecretHash.calculateSecretHash(username, clientId, clientSecret);

            ConfirmSignUpRequest confirmRequest = ConfirmSignUpRequest.builder()
                    .username(username)
                    .confirmationCode(confirmationCode)
                    .clientId(clientId)
                    .secretHash(secretHash)
                    .build();

            this.cognitoClient.confirmSignUp(confirmRequest);


            return "User confirmed successfully.";

        } catch (CognitoIdentityProviderException e) {
            throw new CustomException("Error confirming sign up: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public Map<String, String> loginUser(LoginRequestDTO loginRequestDTO) {
        try {
            Map<String, String> authParams = new HashMap<>();

            authParams.put(CommonEnum.USER_NAME.getValue(), loginRequestDTO.getUsername());
            authParams.put(CommonEnum.PASSWORD.getValue(), loginRequestDTO.getPassword());
            authParams.put(CommonEnum.SECRET_HASH.getValue(), CognitoSecretHash.calculateSecretHash(loginRequestDTO.getUsername(), clientId, clientSecret));

            InitiateAuthRequest initiateAuthRequest = InitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .clientId(clientId)
                    .authParameters(authParams)
                    .build();

            InitiateAuthResponse initiateAuthResponse = cognitoClient.initiateAuth(initiateAuthRequest);

            AuthenticationResultType result = initiateAuthResponse.authenticationResult();
            Map<String, String> tokens = new HashMap<>();
            tokens.put("AccessToken", result.accessToken());
            tokens.put("IdToken", result.idToken());
            tokens.put("RefreshToken", result.refreshToken());
            tokens.put("ExpiresIn", String.valueOf(result.expiresIn()));
            tokens.put("TokenType", result.tokenType());

            return tokens;

        } catch (NotAuthorizedException e) {
            throw new CustomException("Incorrect username or password.", HttpStatus.UNAUTHORIZED);
        } catch (UserNotConfirmedException e) {
            throw new CustomException("User not confirmed. Please verify your email.", HttpStatus.UNAUTHORIZED);
        } catch (CognitoIdentityProviderException e) {
            throw new CustomException("Login failed: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public String adminCreateUser(AdminCreateUserRequestDTO dto) {
        AdminCreateUserRequest request = AdminCreateUserRequest.builder()
                .userPoolId(userPoolId)
                .username(dto.getUsername())
                .userAttributes(
                        AttributeType.builder().name("email").value(dto.getEmail()).build(),
                        AttributeType.builder().name("email_verified").value("true").build()
                )
                .messageAction(MessageActionType.SUPPRESS)
                .build();

        cognitoClient.adminCreateUser(request);

        AdminSetUserPasswordRequest setPasswordRequest = AdminSetUserPasswordRequest.builder()
                .userPoolId(userPoolId)
                .username(dto.getUsername())
                .password(dto.getTemporaryPassword())
                .permanent(true)
                .build();

        cognitoClient.adminSetUserPassword(setPasswordRequest);

        AdminEnableUserRequest enableUserRequest = AdminEnableUserRequest.builder()
                .userPoolId(userPoolId)
                .username(dto.getUsername())
                .build();

        cognitoClient.adminEnableUser(enableUserRequest);

        return dto.getUsername();
    }

    public List<CognitoUserResponse> getAllUsers() {
        ListUsersRequest request = ListUsersRequest.builder()
                .userPoolId(userPoolId)
                .build();

        ListUsersResponse response = cognitoClient.listUsers(request);

        return response.users().stream().map(user -> {
            Map<String, String> attributesMap = user.attributes().stream()
                    .collect(Collectors.toMap(
                            AttributeType::name,
                            AttributeType::value
                    ));

            return CognitoUserResponse.builder()
                    .username(user.username())
                    .status(user.userStatusAsString())
                    .enabled(user.enabled())
                    .attributes(attributesMap)
                    .build();
        }).toList();
    }

    public void disableUser(String username) {
        AdminDisableUserRequest request = AdminDisableUserRequest.builder()
                .userPoolId(userPoolId)
                .username(username)
                .build();

        cognitoClient.adminDisableUser(request);
    }

    public void enableUser(String username) {
        AdminEnableUserRequest request = AdminEnableUserRequest.builder()
                .userPoolId(userPoolId)
                .username(username)
                .build();

        cognitoClient.adminEnableUser(request);
    }
}
