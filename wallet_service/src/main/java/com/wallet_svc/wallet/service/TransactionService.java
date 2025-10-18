package com.wallet_svc.wallet.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.wallet_svc.wallet.dto.response.TransactionResponse;

public interface TransactionService {
    Page<TransactionResponse> getMyTransactions(String userId, Pageable pageable);

    TransactionResponse getTransactionById(Long transactionId);

    Page<TransactionResponse> getAllTransactions(Pageable pageable);
}
