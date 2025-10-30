package com.wallet_svc.wallet.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.wallet_svc.wallet.dto.response.*;
import com.wallet_svc.wallet.service.CreditPackageService;
import com.wallet_svc.wallet.service.TransactionService;
import com.wallet_svc.wallet.service.WalletService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WalletController {
    WalletService walletService;
    TransactionService transactionService;
    CreditPackageService creditPackageService;

    @GetMapping("/my")
    ApiResponse<WalletResponse> getMyWallet(@RequestHeader("X-User-Id") String userId) {
        log.info("Get wallet for user: {}", userId);
        return ApiResponse.<WalletResponse>builder()
                .result(walletService.getWalletByUserId(userId))
                .build();
    }

    @GetMapping("/my/balance")
    ApiResponse<BalanceResponse> getMyBalance(@RequestHeader("X-User-Id") String userId) {
        log.info("Get balance for user: {}", userId);
        return ApiResponse.<BalanceResponse>builder()
                .result(walletService.getBalance(userId))
                .build();
    }

    @GetMapping("/my/transactions")
    ApiResponse<Page<TransactionResponse>> getMyTransactions(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int limit) {
        log.info("Get transactions for user: {}, page: {}, limit: {}", userId, page, limit);
        Pageable pageable = PageRequest.of(page - 1, limit);
        return ApiResponse.<Page<TransactionResponse>>builder()
                .result(transactionService.getMyTransactions(userId, pageable))
                .build();
    }

    @GetMapping("/my/transactions/{id}")
    ApiResponse<TransactionResponse> getTransactionById(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("id") Long id) {
        log.info("Get transaction {} for user: {}", id, userId);
        return ApiResponse.<TransactionResponse>builder()
                .result(transactionService.getTransactionById(id))
                .build();
    }

    @GetMapping("/packages")
    ApiResponse<List<CreditPackageResponse>> getPackages() {
        return ApiResponse.<List<CreditPackageResponse>>builder()
                .result(creditPackageService.getAllActivePackages())
                .build();
    }

    @GetMapping("/packages/{id}")
    ApiResponse<CreditPackageResponse> getPackageById(@PathVariable("id") Long id) {
        return ApiResponse.<CreditPackageResponse>builder()
                .result(creditPackageService.getPackageById(id))
                .build();
    }
}
