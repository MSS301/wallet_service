package com.wallet_svc.wallet.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "credit_packages")
public class CreditPackage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	@Column(length = 50, unique = true, nullable = false)
	String code; // "BASIC_1000", "PRO_5000"

	@Column(length = 100, nullable = false)
	String name;

	@Column(name = "name_en", length = 100)
	String nameEn;

	@Column(columnDefinition = "TEXT")
	String description;

	@Column(name = "credits_amount", nullable = false)
	Integer creditsAmount;

	@Column(name = "bonus_credits")
	@Builder.Default
	Integer bonusCredits = 0;

	@Column(name = "is_popular")
	@Builder.Default
	Boolean isPopular = false;

	@Column(name = "is_active")
	@Builder.Default
	Boolean isActive = true;

	@Column(name = "display_order")
	Integer displayOrder;

	@Column(name = "valid_from")
	LocalDate validFrom;

	@Column(name = "valid_to")
	LocalDate validTo;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	String metadata;

	@Column(name = "created_at", updatable = false)
	@Builder.Default
	LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "updated_at")
	@Builder.Default
	LocalDateTime updatedAt = LocalDateTime.now();

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	@PrePersist
	public void prePersist() {
		if (this.createdAt == null) {
			this.createdAt = LocalDateTime.now();
		}
		this.updatedAt = LocalDateTime.now();
	}
}
