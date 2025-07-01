package com.svtKvt.sitpass.repository;

import com.svtKvt.sitpass.model.AccountRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AccountRequestRepository extends JpaRepository<AccountRequest, Long> {
    Optional<AccountRequest> findByEmail(String email);

    @Query("Select r from AccountRequest r where r.status = 'PENDING'")
    List<AccountRequest> findAllPending();
}
