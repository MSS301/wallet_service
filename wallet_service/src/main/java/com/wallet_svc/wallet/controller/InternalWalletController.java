package com.wallet_svc.wallet.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.wallet_svc.wallet.dto.request.*;
import com.wallet_svc.wallet.dto.response.*;
import com.wallet_svc.wallet.service.WalletService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/internal/wallets")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class InternalWalletController {
	WalletService walletService;

	@PostMapping("/hold")
	@ResponseStatus(HttpStatus.CREATED)
	ApiResponse<TransactionResponse> holdCredits(@RequestBody @Valid HoldRequest request) {
		log.info("Hold credits request for user: {}", request.getUserId());
		return ApiResponse.<TransactionResponse>builder()
				.result(walletService.holdCredits(request))
				.build();
	}

	@PostMapping("/release")
	ApiResponse<TransactionResponse> releaseHold(@RequestBody @Valid ReleaseHoldRequest request) {
		log.info("Release hold request: {}", request.getHoldId());
		return ApiResponse.<TransactionResponse>builder()
				.result(walletService.releaseHold(request))
				.build();
	}

	@PostMapping("/charge")
	ApiResponse<TransactionResponse> chargeCredits(@RequestBody @Valid ChargeRequest request) {
		log.info("Charge credits request for user: {}", request.getUserId());
		return ApiResponse.<TransactionResponse>builder()
				.result(walletService.charge(request))
				.build();
	}

	@PostMapping("/refund")
	ApiResponse<TransactionResponse> refundCredits(@RequestBody @Valid RefundRequest request) {
		log.info("Refund credits request for user: {}", request.getUserId());
		return ApiResponse.<TransactionResponse>builder()
				.result(walletService.refund(request))
				.build();
	}

	@PostMapping("/top-up")
	ApiResponse<TransactionResponse> topUp(@RequestBody @Valid TopUpRequest request) {
		log.info("Top-up request for user: {}", request.getUserId());
		return ApiResponse.<TransactionResponse>builder()
				.result(walletService.topUp(request))
				.build();
	}

	@GetMapping("/{userId}/balance")
	ApiResponse<BalanceResponse> getBalance(@PathVariable("userId") Integer userId) {
		log.info("Get balance request for user: {}", userId);
		return ApiResponse.<BalanceResponse>builder()
				.result(walletService.getBalance(userId))
				.build();
	}
}
