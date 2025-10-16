package com.wallet_svc.wallet.service;

import com.wallet_svc.wallet.dto.request.*;
import com.wallet_svc.wallet.dto.response.*;

public interface WalletService {
	WalletResponse createWallet(Integer userId);

	WalletResponse getWalletByUserId(Integer userId);

	BalanceResponse getBalance(Integer userId);

	TransactionResponse holdCredits(HoldRequest request);

	TransactionResponse releaseHold(ReleaseHoldRequest request);

	TransactionResponse charge(ChargeRequest request);

	TransactionResponse refund(RefundRequest request);

	TransactionResponse topUp(TopUpRequest request);

	TransactionResponse adjustment(AdjustmentRequest request);

	boolean validateBalance(Integer userId, java.math.BigDecimal amount);
}
