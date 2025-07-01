package com.svtKvt.sitpass.service;

import com.svtKvt.sitpass.model.AccountRequest;

import java.util.List;
import java.util.Map;

public interface AuthServiceInterface {
    Map<String, String> login(String email, String password);  // Izmenjena metoda za login
    List<AccountRequest> findAllPendingRequests();
    AccountRequest send(AccountRequest accountRequest);
    AccountRequest handle(AccountRequest accountRequest);
}
