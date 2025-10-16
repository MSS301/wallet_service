package com.wallet_svc.wallet.controller;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.wallet_svc.wallet.dto.request.AdjustmentRequest;
import com.wallet_svc.wallet.dto.response.*;
import com.wallet_svc.wallet.service.TransactionService;
import com.wallet_svc.wallet.service.WalletService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin/wallets")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminWalletController {
	WalletService walletService;
	TransactionService transactionService;

	@GetMapping("/{userId}")
	ApiResponse<WalletResponse> getWalletByUserId(@PathVariable("userId") Integer userId) {
		log.info("Admin get wallet for user: {}", userId);
		return ApiResponse.<WalletResponse>builder()
				.result(walletService.getWalletByUserId(userId))
				.build();
	}

	@PostMapping("/{userId}/adjustment")
	@ResponseStatus(HttpStatus.CREATED)
	ApiResponse<TransactionResponse> createAdjustment(
			@PathVariable("userId") Integer userId, @RequestBody @Valid AdjustmentRequest request) {
		log.info("Admin adjustment for user: {}, amount: {}", userId, request.getAmount());
		request.setUserId(userId);
		return ApiResponse.<TransactionResponse>builder()
				.result(walletService.adjustment(request))
				.build();
	}

	@GetMapping("/transactions")
	ApiResponse<Page<TransactionResponse>> getAllTransactions(
			@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int limit) {
		Pageable pageable = PageRequest.of(page - 1, limit);
		return ApiResponse.<Page<TransactionResponse>>builder()
				.result(transactionService.getAllTransactions(pageable))
				.build();
	}
}
