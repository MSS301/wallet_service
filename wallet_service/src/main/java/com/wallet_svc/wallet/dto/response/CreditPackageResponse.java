package com.wallet_svc.wallet.dto.response;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreditPackageResponse {
	Long id;
	String code;
	String name;

	@JsonProperty("name_en")
	String nameEn;

	String description;

	@JsonProperty("credits_amount")
	Integer creditsAmount;

	@JsonProperty("bonus_credits")
	Integer bonusCredits;

	@JsonProperty("is_popular")
	Boolean isPopular;

	@JsonProperty("is_active")
	Boolean isActive;

	@JsonProperty("display_order")
	Integer displayOrder;

	@JsonProperty("valid_from")
	LocalDate validFrom;

	@JsonProperty("valid_to")
	LocalDate validTo;

	String metadata;
}
