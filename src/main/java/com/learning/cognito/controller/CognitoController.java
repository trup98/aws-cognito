package com.learning.cognito.controller;

import com.learning.cognito.dto.reponse.ApiResponse;
import com.learning.cognito.dto.reponse.CognitoUserResponse;
import com.learning.cognito.dto.request.AdminCreateUserRequestDTO;
import com.learning.cognito.dto.request.LoginRequestDTO;
import com.learning.cognito.dto.request.SignUpUserDTO;
import com.learning.cognito.service.CognitoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class CognitoController {

    private final CognitoService cognitoService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signUp(@RequestBody SignUpUserDTO request) {
        String userId = cognitoService.signUp(request);
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "User Created and verification code send to email ", userId), HttpStatus.OK);

    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse> confirmSignUp(@RequestParam String username, @RequestParam String code) {
        String response = cognitoService.confirmSignUp(username, code);
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, response, Collections.emptyMap()), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> loginUser(@RequestBody LoginRequestDTO loginRequestDTO) {
        Map<String, String> tokens = cognitoService.loginUser(loginRequestDTO);
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "User logged in successfully", tokens), HttpStatus.OK);
    }

    @PostMapping("/admin/create-user")
    public ResponseEntity<ApiResponse> adminCreateUser(@RequestBody AdminCreateUserRequestDTO dto) {
        String createdUsername = cognitoService.adminCreateUser(dto);
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "Admin created user with permanent password", createdUsername), HttpStatus.OK);
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse> getAllUsers() {
        List<CognitoUserResponse> users = cognitoService.getAllUsers();
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "Users retrieved successfully", users), HttpStatus.OK);
    }

    @PostMapping("/admin/disable-user")
    public ResponseEntity<ApiResponse> disableUser(@RequestParam String username) {
        this.cognitoService.disableUser(username);
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "User disabled successfully", null), HttpStatus.OK);
    }

    @PostMapping("/admin/enable-user")
    public ResponseEntity<ApiResponse> enableUser(@RequestParam String username) {
        this.cognitoService.enableUser(username);
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "User enabled successfully", null), HttpStatus.OK);
    }

    @DeleteMapping("/admin/delete-user")
    public ResponseEntity<ApiResponse> deleteUser(@RequestParam String username) {
        cognitoService.deleteUser(username);
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "User deleted successfully", null), HttpStatus.OK);
    }

}
