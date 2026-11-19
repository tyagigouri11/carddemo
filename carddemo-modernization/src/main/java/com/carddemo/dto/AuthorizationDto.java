package com.carddemo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.carddemo.entity.Authorization;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for Authorization entity in API requests/responses.
 * 
 * REQUEST fields: accountId, cardNumber, amount, merchantId, merchantName
 * RESPONSE fields: id, accountId, cardNumber, amount, merchantId, merchantName,
 *                  requestDate, responseDate, expiryDate, status, declineReasonCode, approvedAmount
 * 
 * Per COPAUA0C rule R00013: approvedAmount and declineReasonCode are OUTPUT fields
 * computed by service decision logic, NOT client inputs.
 */
@Schema(description = "Authorization request/response")
public class AuthorizationDto {

    @Schema(description = "Authorization ID (response only)", example = "AUTH001", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;

    @NotNull(message = "Account ID is required")
    @Schema(description = "Account ID", example = "1")
    private Long accountId;

    @NotBlank(message = "Card number is required")
    @Schema(description = "Card number", example = "4111111111111111")
    private String cardNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Schema(description = "Authorization amount requested", example = "500.00")
    private BigDecimal amount;

    @NotBlank(message = "Merchant ID is required")
    @Schema(description = "Merchant ID", example = "MERCHANT123")
    private String merchantId;

    @NotBlank(message = "Merchant name is required")
    @Schema(description = "Merchant name", example = "ACME Corporation")
    private String merchantName;

    @Schema(description = "Authorization request date (response only)", example = "2026-10-20", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDate requestDate;

    @Schema(description = "Authorization response date (response only)", example = "2026-10-20", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDate responseDate;

    @Schema(description = "Authorization expiry date (response only, 3 days by default)", example = "2026-10-23", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDate expiryDate;

    @Schema(description = "Authorization status (response only)", example = "APPROVED", 
            allowableValues = {"PENDING", "APPROVED", "DECLINED"}, accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Authorization.AuthorizationStatus status;

    @Schema(description = "Decline reason code if declined (response only)", example = "4100", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String declineReasonCode;

    @Schema(description = "Approved amount if approved (response only)", example = "500.00", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal approvedAmount;

    public AuthorizationDto() {
    }

    public AuthorizationDto(String id, Long accountId, String cardNumber, BigDecimal amount, String merchantId,
                           String merchantName, LocalDate requestDate, LocalDate responseDate, LocalDate expiryDate,
                           Authorization.AuthorizationStatus status, String declineReasonCode, BigDecimal approvedAmount) {
        this.id = id;
        this.accountId = accountId;
        this.cardNumber = cardNumber;
        this.amount = amount;
        this.merchantId = merchantId;
        this.merchantName = merchantName;
        this.requestDate = requestDate;
        this.responseDate = responseDate;
        this.expiryDate = expiryDate;
        this.status = status;
        this.declineReasonCode = declineReasonCode;
        this.approvedAmount = approvedAmount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDate getResponseDate() {
        return responseDate;
    }

    public void setResponseDate(LocalDate responseDate) {
        this.responseDate = responseDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Authorization.AuthorizationStatus getStatus() {
        return status;
    }

    public void setStatus(Authorization.AuthorizationStatus status) {
        this.status = status;
    }

    public String getDeclineReasonCode() {
        return declineReasonCode;
    }

    public void setDeclineReasonCode(String declineReasonCode) {
        this.declineReasonCode = declineReasonCode;
    }

    public BigDecimal getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(BigDecimal approvedAmount) {
        this.approvedAmount = approvedAmount;
    }
}
