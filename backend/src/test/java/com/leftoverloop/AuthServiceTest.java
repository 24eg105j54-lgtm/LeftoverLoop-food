package com.leftoverloop;

import com.leftoverloop.dto.AuthResponse;
import com.leftoverloop.dto.LoginRequest;
import com.leftoverloop.dto.RegisterRequest;
import com.leftoverloop.entity.Role;
import com.leftoverloop.exception.BadRequestException;
import com.leftoverloop.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    void testUserRegistrationAndLoginSuccess() {
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setFullName("Test Donor");
        registerReq.setEmail("testdonor@example.com");
        registerReq.setPassword("secret123");
        registerReq.setRole(Role.ROLE_DONOR);
        registerReq.setPhone("1234567890");

        AuthResponse regResponse = authService.register(registerReq);
        assertNotNull(regResponse.getToken());
        assertEquals("testdonor@example.com", regResponse.getUser().getEmail());
        assertEquals(Role.ROLE_DONOR, regResponse.getUser().getRole());

        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("testdonor@example.com");
        loginReq.setPassword("secret123");

        AuthResponse loginResponse = authService.login(loginReq);
        assertNotNull(loginResponse.getToken());
        assertEquals("Test Donor", loginResponse.getUser().getFullName());
    }

    @Test
    void testDuplicateEmailRegistrationFails() {
        RegisterRequest req1 = new RegisterRequest();
        req1.setFullName("User One");
        req1.setEmail("duplicate@example.com");
        req1.setPassword("password123");
        req1.setRole(Role.ROLE_RECEIVER);

        authService.register(req1);

        RegisterRequest req2 = new RegisterRequest();
        req2.setFullName("User Two");
        req2.setEmail("duplicate@example.com");
        req2.setPassword("password123");
        req2.setRole(Role.ROLE_RECEIVER);

        assertThrows(BadRequestException.class, () -> authService.register(req2));
    }
}
