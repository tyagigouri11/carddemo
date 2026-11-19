package com.carddemo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.carddemo.entity.Transaction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for Transaction entity in API requests/responses.
 */
@Schema(description = "Transaction information")
public class TransactionDto {

    @Schema(description = "Transaction ID", example = "TXN001")
    private String id;

    @NotNull(message = "Account ID is required")
    @Schema(description = "Account ID", example = "1")
    private Long accountId;

    @Schema(description = "Card number used", example = "4111111111111111")
    private String cardNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Schema(description = "Transaction amount", example = "150.00")
    private BigDecimal amount;

    @NotNull(message = "Transaction date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Transaction date", example = "2026-10-15")
    private LocalDate date;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Post date", example = "2026-10-15")
    private LocalDate postDate;

    @NotNull(message = "Transaction type is required")
    @Schema(description = "Transaction type", example = "PURCHASE", 
            allowableValues = {"PURCHASE", "CASH_ADVANCE", "PAYMENT", "FEE", "INTEREST"})
    private Transaction.TransactionType type;

    @NotNull(message = "Transaction status is required")
    @Schema(description = "Transaction status", example = "PENDING",
            allowableValues = {"PENDING", "POSTED", "SETTLED"})
    private Transaction.TransactionStatus status;

    @Schema(description = "Merchant ID", example = "M123")
    private String merchantId;

    @Schema(description = "Merchant name", example = "Walmart")
    private String merchantName;

    @Schema(description = "Processing code", example = "000000")
    private String processingCode;

    public TransactionDto() {
    }

    public TransactionDto(String id, Long accountId, String cardNumber, BigDecimal amount, LocalDate date,
                         LocalDate postDate, Transaction.TransactionType type, Transaction.TransactionStatus status,
                         String merchantId, String merchantName, String processingCode) {
        this.id = id;
        this.accountId = accountId;
        this.cardNumber = cardNumber;
        this.amount = amount;
        this.date = date;
        this.postDate = postDate;
        this.type = type;
        this.status = status;
        this.merchantId = merchantId;
        this.merchantName = merchantName;
        this.processingCode = processingCode;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getPostDate() {
        return postDate;
    }

    public void setPostDate(LocalDate postDate) {
        this.postDate = postDate;
    }

    public Transaction.TransactionType getType() {
        return type;
    }

    public void setType(Transaction.TransactionType type) {
        this.type = type;
    }

    public Transaction.TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(Transaction.TransactionStatus status) {
        this.status = status;
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

    public String getProcessingCode() {
        return processingCode;
    }

    public void setProcessingCode(String processingCode) {
        this.processingCode = processingCode;
    }
}
