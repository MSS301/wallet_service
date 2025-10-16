package com.wallet_svc.wallet.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.wallet_svc.event.WalletEventProducer;
import com.wallet_svc.wallet.constant.TransactionStatus;
import com.wallet_svc.wallet.constant.TransactionType;
import com.wallet_svc.wallet.constant.WalletStatus;
import com.wallet_svc.wallet.dto.request.*;
import com.wallet_svc.wallet.dto.response.*;
import com.wallet_svc.wallet.entity.Wallet;
import com.wallet_svc.wallet.entity.WalletHold;
import com.wallet_svc.wallet.entity.WalletTransaction;
import com.wallet_svc.wallet.exception.AppException;
import com.wallet_svc.wallet.exception.ErrorCode;
import com.wallet_svc.wallet.mapper.WalletMapper;
import com.wallet_svc.wallet.repository.WalletHoldRepository;
import com.wallet_svc.wallet.repository.WalletRepository;
import com.wallet_svc.wallet.repository.WalletTransactionRepository;
import com.wallet_svc.wallet.service.WalletService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WalletServiceImpl implements WalletService {
	WalletRepository walletRepository;
	WalletTransactionRepository transactionRepository;
	WalletHoldRepository holdRepository;
	WalletMapper walletMapper;
	WalletEventProducer walletEventProducer;

	@Value("${app.wallet.initial-balance:0.00}")
	BigDecimal initialBalance;

	@Value("${app.wallet.default-currency:VND}")
	String defaultCurrency;

	@Value("${app.wallet.hold-expiration-minutes:30}")
	Integer holdExpirationMinutes;

	@Override
	@Transactional
	public WalletResponse createWallet(Integer userId) {
		if (walletRepository.existsByUserId(userId)) {
			throw new AppException(ErrorCode.WALLET_ALREADY_EXISTS);
		}

		Wallet wallet = Wallet.builder()
				.userId(userId)
				.balance(new BigDecimal(initialBalance.toString()))
				.currency(defaultCurrency)
				.status(WalletStatus.ACTIVE)
				.build();

		wallet = walletRepository.save(wallet);
		log.info("Created wallet for user: {}", userId);

		// Publish wallet created event
		walletEventProducer.publishWalletCreatedEvent(wallet.getId(), userId);

		return walletMapper.toWalletResponse(wallet);
	}

	@Override
	public WalletResponse getWalletByUserId(Integer userId) {
		Wallet wallet =
				walletRepository.findByUserId(userId).orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
		return walletMapper.toWalletResponse(wallet);
	}

	@Override
	public BalanceResponse getBalance(Integer userId) {
		Wallet wallet =
				walletRepository.findByUserId(userId).orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

		BigDecimal availableBalance = wallet.getBalance().subtract(wallet.getLockedBalance());

		return BalanceResponse.builder()
				.userId(userId)
				.balance(wallet.getBalance())
				.lockedBalance(wallet.getLockedBalance())
				.availableBalance(availableBalance)
				.currency(wallet.getCurrency())
				.status(wallet.getStatus())
				.build();
	}

	@Override
	@Transactional
	public TransactionResponse holdCredits(HoldRequest request) {
		Wallet wallet = walletRepository
				.findByUserId(request.getUserId())
				.orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

		validateWalletStatus(wallet);

		BigDecimal availableBalance = wallet.getBalance().subtract(wallet.getLockedBalance());
		if (availableBalance.compareTo(request.getAmount()) < 0) {
			throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
		}

		// Create hold
		Integer expirationMinutes =
				request.getExpirationMinutes() != null ? request.getExpirationMinutes() : holdExpirationMinutes;
		LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);

		WalletHold hold = WalletHold.builder()
				.walletId(wallet.getId())
				.amount(request.getAmount())
				.reason(request.getReason())
				.referenceType(request.getReferenceType())
				.referenceId(request.getReferenceId())
				.status("ACTIVE")
				.expiresAt(expiresAt)
				.build();

		hold = holdRepository.save(hold);

		// Update wallet locked balance
		wallet.setLockedBalance(wallet.getLockedBalance().add(request.getAmount()));
		walletRepository.save(wallet);

		// Create transaction
		WalletTransaction transaction = WalletTransaction.builder()
				.walletId(wallet.getId())
				.transactionType(TransactionType.HOLD)
				.amount(request.getAmount())
				.referenceType(request.getReferenceType())
				.referenceId(request.getReferenceId())
				.description(request.getReason())
				.status(TransactionStatus.SUCCESS)
				.balanceBefore(wallet.getBalance())
				.balanceAfter(wallet.getBalance())
				.metadata("{\"hold_id\":" + hold.getId() + "}")
				.processedAt(LocalDateTime.now())
				.build();

		transaction = transactionRepository.save(transaction);
		log.info("Held {} credits for user: {}", request.getAmount(), request.getUserId());

		return walletMapper.toTransactionResponse(transaction);
	}

	@Override
	@Transactional
	public TransactionResponse releaseHold(ReleaseHoldRequest request) {
		WalletHold hold =
				holdRepository.findById(request.getHoldId()).orElseThrow(() -> new AppException(ErrorCode.HOLD_NOT_FOUND));

		if ("RELEASED".equals(hold.getStatus())) {
			throw new AppException(ErrorCode.HOLD_ALREADY_RELEASED);
		}

		if ("EXPIRED".equals(hold.getStatus())) {
			throw new AppException(ErrorCode.HOLD_EXPIRED);
		}

		Wallet wallet =
				walletRepository.findById(hold.getWalletId()).orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

		// Update hold status
		hold.setStatus("RELEASED");
		hold.setReleasedAt(LocalDateTime.now());
		holdRepository.save(hold);

		// Release locked balance
		wallet.setLockedBalance(wallet.getLockedBalance().subtract(hold.getAmount()));
		walletRepository.save(wallet);

		// Create transaction
		WalletTransaction transaction = WalletTransaction.builder()
				.walletId(wallet.getId())
				.transactionType(TransactionType.RELEASE)
				.amount(hold.getAmount())
				.referenceType(hold.getReferenceType())
				.referenceId(hold.getReferenceId())
				.description("Released hold")
				.status(TransactionStatus.SUCCESS)
				.balanceBefore(wallet.getBalance())
				.balanceAfter(wallet.getBalance())
				.metadata("{\"hold_id\":" + hold.getId() + "}")
				.processedAt(LocalDateTime.now())
				.build();

		transaction = transactionRepository.save(transaction);
		log.info("Released hold {} for wallet: {}", hold.getId(), wallet.getId());

		return walletMapper.toTransactionResponse(transaction);
	}

	@Override
	@Transactional
	public TransactionResponse charge(ChargeRequest request) {
		Wallet wallet = walletRepository
				.findByUserId(request.getUserId())
				.orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

		validateWalletStatus(wallet);

		BigDecimal balanceBefore = wallet.getBalance();
		WalletHold hold = null;

		// If charging from a hold
		if (request.getHoldId() != null) {
			hold = holdRepository
					.findById(request.getHoldId())
					.orElseThrow(() -> new AppException(ErrorCode.HOLD_NOT_FOUND));

			if (!"ACTIVE".equals(hold.getStatus())) {
				throw new AppException(ErrorCode.HOLD_ALREADY_RELEASED);
			}

			// Release the hold and deduct from balance
			wallet.setLockedBalance(wallet.getLockedBalance().subtract(hold.getAmount()));
			wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));

			hold.setStatus("RELEASED");
			hold.setReleasedAt(LocalDateTime.now());
			holdRepository.save(hold);
		} else {
			// Direct charge
			BigDecimal availableBalance = wallet.getBalance().subtract(wallet.getLockedBalance());
			if (availableBalance.compareTo(request.getAmount()) < 0) {
				throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
			}

			wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
		}

		wallet.setTotalSpent(wallet.getTotalSpent().add(request.getAmount()));
		walletRepository.save(wallet);

		// Create transaction
		WalletTransaction transaction = WalletTransaction.builder()
				.walletId(wallet.getId())
				.transactionType(TransactionType.CHARGE)
				.amount(request.getAmount())
				.referenceType(request.getReferenceType())
				.referenceId(request.getReferenceId())
				.description(request.getDescription())
				.status(TransactionStatus.SUCCESS)
				.balanceBefore(balanceBefore)
				.balanceAfter(wallet.getBalance())
				.metadata(request.getMetadata())
				.processedAt(LocalDateTime.now())
				.build();

		transaction = transactionRepository.save(transaction);
		log.info("Charged {} credits from user: {}", request.getAmount(), request.getUserId());

		return walletMapper.toTransactionResponse(transaction);
	}

	@Override
	@Transactional
	public TransactionResponse refund(RefundRequest request) {
		Wallet wallet = walletRepository
				.findByUserId(request.getUserId())
				.orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

		validateWalletStatus(wallet);

		BigDecimal balanceBefore = wallet.getBalance();
		wallet.setBalance(wallet.getBalance().add(request.getAmount()));
		wallet.setTotalRefunded(wallet.getTotalRefunded().add(request.getAmount()));
		walletRepository.save(wallet);

		// Create transaction
		WalletTransaction transaction = WalletTransaction.builder()
				.walletId(wallet.getId())
				.transactionType(TransactionType.REFUND)
				.amount(request.getAmount())
				.referenceType(request.getReferenceType())
				.referenceId(request.getReferenceId())
				.description(request.getDescription())
				.status(TransactionStatus.SUCCESS)
				.balanceBefore(balanceBefore)
				.balanceAfter(wallet.getBalance())
				.metadata(request.getMetadata())
				.relatedTransactionId(request.getOriginalTransactionId())
				.processedAt(LocalDateTime.now())
				.build();

		transaction = transactionRepository.save(transaction);
		log.info("Refunded {} credits to user: {}", request.getAmount(), request.getUserId());

		return walletMapper.toTransactionResponse(transaction);
	}

	@Override
	@Transactional
	public TransactionResponse topUp(TopUpRequest request) {
		Wallet wallet = walletRepository
				.findByUserId(request.getUserId())
				.orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

		validateWalletStatus(wallet);

		BigDecimal balanceBefore = wallet.getBalance();
		wallet.setBalance(wallet.getBalance().add(request.getAmount()));
		wallet.setTotalEarned(wallet.getTotalEarned().add(request.getAmount()));
		walletRepository.save(wallet);

		// Create transaction
		WalletTransaction transaction = WalletTransaction.builder()
				.walletId(wallet.getId())
				.transactionType(TransactionType.TOP_UP)
				.amount(request.getAmount())
				.referenceType(request.getReferenceType())
				.referenceId(request.getReferenceId())
				.description(request.getDescription())
				.status(TransactionStatus.SUCCESS)
				.balanceBefore(balanceBefore)
				.balanceAfter(wallet.getBalance())
				.metadata(request.getMetadata())
				.processedAt(LocalDateTime.now())
				.build();

		transaction = transactionRepository.save(transaction);
		log.info("Topped up {} credits to user: {}", request.getAmount(), request.getUserId());

		return walletMapper.toTransactionResponse(transaction);
	}

	@Override
	@Transactional
	public TransactionResponse adjustment(AdjustmentRequest request) {
		Wallet wallet = walletRepository
				.findByUserId(request.getUserId())
				.orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

		BigDecimal balanceBefore = wallet.getBalance();
		wallet.setBalance(wallet.getBalance().add(request.getAmount()));

		if (request.getAmount().compareTo(BigDecimal.ZERO) > 0) {
			wallet.setTotalEarned(wallet.getTotalEarned().add(request.getAmount()));
		} else {
			wallet.setTotalSpent(wallet.getTotalSpent().add(request.getAmount().abs()));
		}

		walletRepository.save(wallet);

		// Create transaction
		WalletTransaction transaction = WalletTransaction.builder()
				.walletId(wallet.getId())
				.transactionType(TransactionType.ADJUSTMENT)
				.amount(request.getAmount().abs())
				.description(request.getDescription())
				.status(TransactionStatus.SUCCESS)
				.balanceBefore(balanceBefore)
				.balanceAfter(wallet.getBalance())
				.metadata(request.getMetadata())
				.processedBy(request.getProcessedBy())
				.processedAt(LocalDateTime.now())
				.build();

		transaction = transactionRepository.save(transaction);
		log.info("Adjusted {} credits for user: {} by admin: {}", request.getAmount(), request.getUserId(), request.getProcessedBy());

		return walletMapper.toTransactionResponse(transaction);
	}

	@Override
	public boolean validateBalance(Integer userId, BigDecimal amount) {
		Wallet wallet =
				walletRepository.findByUserId(userId).orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

		BigDecimal availableBalance = wallet.getBalance().subtract(wallet.getLockedBalance());
		return availableBalance.compareTo(amount) >= 0;
	}

	private void validateWalletStatus(Wallet wallet) {
		if (WalletStatus.SUSPENDED.equals(wallet.getStatus())) {
			throw new AppException(ErrorCode.WALLET_SUSPENDED);
		}
		if (WalletStatus.CLOSED.equals(wallet.getStatus())) {
			throw new AppException(ErrorCode.WALLET_CLOSED);
		}
	}
}
