package com.wallet_svc.wallet.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.wallet_svc.wallet.dto.response.TransactionResponse;
import com.wallet_svc.wallet.entity.Wallet;
import com.wallet_svc.wallet.entity.WalletTransaction;
import com.wallet_svc.wallet.exception.AppException;
import com.wallet_svc.wallet.exception.ErrorCode;
import com.wallet_svc.wallet.mapper.WalletMapper;
import com.wallet_svc.wallet.repository.WalletRepository;
import com.wallet_svc.wallet.repository.WalletTransactionRepository;
import com.wallet_svc.wallet.service.TransactionService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TransactionServiceImpl implements TransactionService {
    WalletRepository walletRepository;
    WalletTransactionRepository transactionRepository;
    WalletMapper walletMapper;

    @Override
    public Page<TransactionResponse> getMyTransactions(String userId, Pageable pageable) {
        Wallet wallet =
                walletRepository.findByUserId(userId).orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        Page<WalletTransaction> transactions =
                transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId(), pageable);

        return transactions.map(walletMapper::toTransactionResponse);
    }

    @Override
    public TransactionResponse getTransactionById(Long transactionId) {
        WalletTransaction transaction = transactionRepository
                .findById(transactionId)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));

        return walletMapper.toTransactionResponse(transaction);
    }

    @Override
    public Page<TransactionResponse> getAllTransactions(Pageable pageable) {
        Page<WalletTransaction> transactions = transactionRepository.findAll(pageable);
        return transactions.map(walletMapper::toTransactionResponse);
    }
}
