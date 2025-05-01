package com.projects.marketmosaic.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projects.marketmosaic.common.config.ZooKeeperConfig;
import com.projects.marketmosaic.common.utils.CommonUtils;
import com.projects.marketmosaic.enums.AuthStatus;
import com.projects.marketmosaic.exception.exceptions.AuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class EmailUtils {

    private static final String EMAILJS_API_URL = "https://api.emailjs.com/api/v1.0/email/send";
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String serviceId;
    private final String templateId;
    private final String userId;
    private final String accessToken;

    @Autowired
    public EmailUtils(CloseableHttpClient httpClient, ObjectMapper objectMapper, ZooKeeperConfig zooKeeperConfig) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;

        Map<String, Object> emailConfig = CommonUtils.toMap(zooKeeperConfig.getConfigValueByKey("Email"));

        this.serviceId = emailConfig.get("serviceId").toString();
        this.templateId = emailConfig.get("templateId").toString();
        this.userId = emailConfig.get("userId").toString();
        this.accessToken = emailConfig.get("accessToken").toString();
    }

    public void sendPasswordResetEmail(String email, String resetToken, String name) {
        try {
            HttpPost httpPost = new HttpPost(EMAILJS_API_URL);
            httpPost.setHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
            httpPost.setHeader("origin", "http://localhost"); // Replace with your frontend URL in production

            Map<String, Object> templateParams = new HashMap<>();
            templateParams.put("to_email", email);
            templateParams.put("to_name", name);
            templateParams.put("reset_link", "http://localhost:3000/reset-password?token=" + resetToken);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("service_id", serviceId);
            requestBody.put("template_id", templateId);
            requestBody.put("user_id", userId);
//            requestBody.put("accessToken", accessToken);
            requestBody.put("template_params", templateParams);

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            httpPost.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));

            httpClient.execute(httpPost, response -> {
                int statusCode = response.getCode();
                if (statusCode != 200) {
                    log.error("EmailJS API returned non-200 status code: {}", statusCode);
                    throw new AuthException("Failed to send password reset email",
                            AuthStatus.AUTH_007, HttpStatus.INTERNAL_SERVER_ERROR);
                }
                return null;
            });

            log.info("Password reset email sent successfully to: {}", email);
        } catch (AuthException ae) {
            throw ae; // Re-throw AuthException as is
        } catch (Exception e) {
            log.error("Error sending password reset email to {}: {}", email, e.getMessage());
            throw new AuthException("Failed to send password reset email",
                    AuthStatus.AUTH_007, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}