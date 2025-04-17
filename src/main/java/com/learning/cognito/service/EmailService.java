package com.learning.cognito.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final String senderEmail = "trupsavaliya23@gmail.com";

    public void sendOtpEmail(String toEmail, String otpCode) {
        String subject = "Your OTP Code";
        String body = "Your OTP code is: " + otpCode;

        Destination destination = Destination.builder()
                .toAddresses(toEmail)
                .build();

        Content subjectContent = Content.builder()
                .data(subject)
                .build();

        Content bodyContent = Content.builder()
                .data(body)
                .build();

        Body emailBody = Body.builder()
                .text(bodyContent)
                .build();

        Message message = Message.builder()
                .subject(subjectContent)
                .body(emailBody)
                .build();

        SendEmailRequest request = SendEmailRequest.builder()
                .source(senderEmail)
                .destination(destination)
                .message(message)
                .build();

        try (SesClient sesClient = SesClient.builder()
                .region(Region.AP_SOUTH_1)
                .build()) {
            sesClient.sendEmail(request);
        } catch (SesException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }
}
