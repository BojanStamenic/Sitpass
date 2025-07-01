package com.svtKvt.sitpass.service;

import com.svtKvt.sitpass.model.AccountRequest;
import com.svtKvt.sitpass.repository.AccountRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountRequestService {

    private final PasswordEncoder passwordEncoder;

    private final AccountRequestRepository accountRequestRepository;

    public List<AccountRequest> getAllAccountRequests() {
        return accountRequestRepository.findAll();
    }

    public Optional<AccountRequest> getAccountRequestById(Long id) {
        return accountRequestRepository.findById(id);
    }

    public AccountRequest saveAccountRequest(AccountRequest accountRequest) {

        return accountRequestRepository.save(accountRequest);
    }

    public void deleteAccountRequest(Long id) {
        accountRequestRepository.deleteById(id);
    }
}
