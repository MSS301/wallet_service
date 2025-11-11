package com.wallet_svc.wallet.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error"),
    INVALID_KEY(1001, "Invalid message key"),
    WALLET_NOT_FOUND(2001, "Wallet not found"),
    WALLET_ALREADY_EXISTS(2002, "Wallet already exists for this user"),
    INSUFFICIENT_BALANCE(2003, "Insufficient balance"),
    WALLET_SUSPENDED(2004, "Wallet is suspended"),
    WALLET_CLOSED(2005, "Wallet is closed"),
    TRANSACTION_NOT_FOUND(2101, "Transaction not found"),
    INVALID_TRANSACTION_TYPE(2102, "Invalid transaction type"),
    TRANSACTION_FAILED(2103, "Transaction failed"),
    HOLD_NOT_FOUND(2201, "Hold not found"),
    HOLD_EXPIRED(2202, "Hold has expired"),
    HOLD_ALREADY_RELEASED(2203, "Hold has already been released"),
    PACKAGE_NOT_FOUND(2301, "Credit package not found"),
    INVALID_AMOUNT(2401, "Invalid amount"),
    INSUFFICIENT_TOKEN(2501, "Insufficient token balance");

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private final int code;
    private final String message;
}
