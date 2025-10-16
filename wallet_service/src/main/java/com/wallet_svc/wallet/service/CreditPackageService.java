package com.wallet_svc.wallet.service;

import java.util.List;

import com.wallet_svc.wallet.dto.response.CreditPackageResponse;

public interface CreditPackageService {
	List<CreditPackageResponse> getAllActivePackages();

	CreditPackageResponse getPackageById(Long packageId);
}
