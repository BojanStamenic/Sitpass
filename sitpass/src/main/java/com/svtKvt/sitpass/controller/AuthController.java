package com.svtKvt.sitpass.controller;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.svtKvt.sitpass.model.AccountRequest;
import com.svtKvt.sitpass.model.User;
import com.svtKvt.sitpass.service.AccountRequestService;
import com.svtKvt.sitpass.service.AuthService;
import com.svtKvt.sitpass.service.UserService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
//@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AccountRequestService accountRequestService;

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody AccountRequest accountRequest) {
        // Šifrovanje lozinke

        System.out.println(accountRequest.getPassword());
        accountRequest.setPassword(passwordEncoder.encode(accountRequest.getPassword()));
        System.out.println(passwordEncoder.encode(accountRequest.getPassword()));

        return ResponseEntity.ok(accountRequestService.saveAccountRequest(accountRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody AccountRequest accountRequest) throws BadRequestException {
        // Autentifikacija je već obavljena od strane Spring Security-a
        return ResponseEntity.ok(authService.login(accountRequest.getEmail(), accountRequest.getPassword()));
    }

    @PostMapping("/logout")
    public String logout() {
        return "User logged out successfully!";
    }
}
