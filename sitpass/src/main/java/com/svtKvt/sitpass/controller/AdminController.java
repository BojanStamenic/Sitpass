package com.svtKvt.sitpass.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.svtKvt.sitpass.model.AccountRequest;
import com.svtKvt.sitpass.model.RequestStatus;
import com.svtKvt.sitpass.model.User;
import com.svtKvt.sitpass.service.AccountRequestService;
import com.svtKvt.sitpass.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    @Autowired
    private AccountRequestService accountRequestService;

    @Autowired
    private UserService userService;


    private final PasswordEncoder passwordEncoder;

    @PutMapping("/approve/{id}")
    public ResponseEntity<String> approveAccountRequest(@PathVariable Long id) {
        Optional<AccountRequest> accountRequest = accountRequestService.getAccountRequestById(id);
        System.out.println("ID1" + accountRequest.get().getPassword());
        if (accountRequest.isPresent()) {
            AccountRequest request = accountRequest.get();


            System.out.println("ID2" + accountRequest.get().getPassword());
            // Kreiramo novog korisnika u tabeli User samo sa potrebnim atributima
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(accountRequest.get().getPassword());
            user.setCreatedAt(request.getCreatedAt());
            user.setAddress(request.getAddress());
            request.setStatus(RequestStatus.ACCEPTED);
            accountRequestService.saveAccountRequest(request);
            try {
                userService.saveUser(user);
                return ResponseEntity.ok("User approved and created successfully!");
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error saving user: " + e.getMessage());
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<String> rejectAccountRequest(@PathVariable Long id, @RequestBody String rejectionReasonJson) {
        Optional<AccountRequest> accountRequest = accountRequestService.getAccountRequestById(id);
        if (accountRequest.isPresent()) {
            AccountRequest request = accountRequest.get();
            request.setStatus(RequestStatus.REJECTED);

            try {
                // Koristimo ObjectMapper da parsiramo JSON string
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(rejectionReasonJson);

                // Ekstraktujemo vrednost polja rejectionReason iz JSON-a
                String rejectionReason = jsonNode.get("rejectionReason").asText();

                // Postavljamo rejectionReason u model
                request.setRejectionReason(rejectionReason);
            } catch (Exception e) {
                return ResponseEntity.status(400).body("Invalid rejection reason format: " + e.getMessage());
            }

            accountRequestService.saveAccountRequest(request);

            return ResponseEntity.ok("User request rejected with reason: " + request.getRejectionReason());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
