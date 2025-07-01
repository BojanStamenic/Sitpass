package com.svtKvt.sitpass.controller;

import com.svtKvt.sitpass.model.AccountRequest;
import com.svtKvt.sitpass.service.AccountRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/account-requests")
public class AccountRequestController {

    @Autowired
    private AccountRequestService accountRequestService;

    @GetMapping
    public List<AccountRequest> getAllAccountRequests() {
        return accountRequestService.getAllAccountRequests();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountRequest> getAccountRequestById(@PathVariable Long id) {
        Optional<AccountRequest> accountRequest = accountRequestService.getAccountRequestById(id);
        return accountRequest.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public AccountRequest createAccountRequest(@RequestBody AccountRequest accountRequest) {
        return accountRequestService.saveAccountRequest(accountRequest);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountRequest> updateAccountRequest(@PathVariable Long id, @RequestBody AccountRequest accountRequestDetails) {
        Optional<AccountRequest> accountRequest = accountRequestService.getAccountRequestById(id);
        if (accountRequest.isPresent()) {
            AccountRequest updatedAccountRequest = accountRequest.get();
            // Ažuriraj polja prema accountRequestDetails
            updatedAccountRequest.setStatus(accountRequestDetails.getStatus());
            updatedAccountRequest.setRejectionReason(accountRequestDetails.getRejectionReason());
            // Dodatna ažuriranja prema potrebi

            return ResponseEntity.ok(accountRequestService.saveAccountRequest(updatedAccountRequest));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccountRequest(@PathVariable Long id) {
        accountRequestService.deleteAccountRequest(id);
        return ResponseEntity.noContent().build();
    }
}
