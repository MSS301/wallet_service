package com.wallet_svc.wallet.mapper;

import org.mapstruct.Mapper;

import com.wallet_svc.wallet.dto.response.*;
import com.wallet_svc.wallet.entity.*;

@Mapper(componentModel = "spring")
public interface WalletMapper {
    WalletResponse toWalletResponse(Wallet wallet);

    TransactionResponse toTransactionResponse(WalletTransaction transaction);
}
