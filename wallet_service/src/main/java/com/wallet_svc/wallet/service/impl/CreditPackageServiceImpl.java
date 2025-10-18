package com.wallet_svc.wallet.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.wallet_svc.wallet.dto.response.CreditPackageResponse;
import com.wallet_svc.wallet.entity.CreditPackage;
import com.wallet_svc.wallet.exception.AppException;
import com.wallet_svc.wallet.exception.ErrorCode;
import com.wallet_svc.wallet.mapper.WalletMapper;
import com.wallet_svc.wallet.repository.CreditPackageRepository;
import com.wallet_svc.wallet.service.CreditPackageService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CreditPackageServiceImpl implements CreditPackageService {
    CreditPackageRepository packageRepository;
    WalletMapper walletMapper;

    @Override
    public List<CreditPackageResponse> getAllActivePackages() {
        List<CreditPackage> packages = packageRepository.findByIsActiveTrueOrderByDisplayOrder();
        return packages.stream().map(walletMapper::toCreditPackageResponse).collect(Collectors.toList());
    }

    @Override
    public CreditPackageResponse getPackageById(Long packageId) {
        CreditPackage creditPackage =
                packageRepository.findById(packageId).orElseThrow(() -> new AppException(ErrorCode.PACKAGE_NOT_FOUND));
        return walletMapper.toCreditPackageResponse(creditPackage);
    }
}
