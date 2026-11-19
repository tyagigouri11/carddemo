package com.carddemo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for Account entity in API requests/responses.
 */
@Schema(description = "Account information")
public class AccountDto {

    @Schema(description = "Account ID", example = "1")
    private Long id;

    @NotNull(message = "Credit limit is required")
    @Positive(message = "Credit limit must be positive")
    @Schema(description = "Account credit limit", example = "50000.00")
    private BigDecimal creditLimit;

    @NotNull(message = "Current balance is required")
    @Schema(description = "Current account balance", example = "25000.00")
    private BigDecimal currentBalance;

    @NotNull(message = "Active status is required")
    @Schema(description = "Account active status", example = "true")
    private Boolean activeStatus;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Card reissue date", example = "2026-06-01")
    private LocalDate reissueDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Account open date", example = "2020-01-15")
    private LocalDate openDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Account expiration date", example = "2028-01-15")
    private LocalDate expirationDate;

    @Schema(description = "Cash credit limit", example = "10000.00")
    private BigDecimal cashCreditLimit;

    @Schema(description = "Current cycle debit", example = "2500.00")
    private BigDecimal currentCycleDebit;

    @Schema(description = "Current cycle credit", example = "0.00")
    private BigDecimal currentCycleCredit;

    @Schema(description = "Account group ID", example = "GROUP_A")
    private String groupId;

    public AccountDto() {
    }

    public AccountDto(Long id, BigDecimal creditLimit, BigDecimal currentBalance, Boolean activeStatus,
                      LocalDate reissueDate, LocalDate openDate, LocalDate expirationDate,
                      BigDecimal cashCreditLimit, BigDecimal currentCycleDebit, BigDecimal currentCycleCredit,
                      String groupId) {
        this.id = id;
        this.creditLimit = creditLimit;
        this.currentBalance = currentBalance;
        this.activeStatus = activeStatus;
        this.reissueDate = reissueDate;
        this.openDate = openDate;
        this.expirationDate = expirationDate;
        this.cashCreditLimit = cashCreditLimit;
        this.currentCycleDebit = currentCycleDebit;
        this.currentCycleCredit = currentCycleCredit;
        this.groupId = groupId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public Boolean getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(Boolean activeStatus) {
        this.activeStatus = activeStatus;
    }

    public LocalDate getReissueDate() {
        return reissueDate;
    }

    public void setReissueDate(LocalDate reissueDate) {
        this.reissueDate = reissueDate;
    }

    public LocalDate getOpenDate() {
        return openDate;
    }

    public void setOpenDate(LocalDate openDate) {
        this.openDate = openDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public BigDecimal getCashCreditLimit() {
        return cashCreditLimit;
    }

    public void setCashCreditLimit(BigDecimal cashCreditLimit) {
        this.cashCreditLimit = cashCreditLimit;
    }

    public BigDecimal getCurrentCycleDebit() {
        return currentCycleDebit;
    }

    public void setCurrentCycleDebit(BigDecimal currentCycleDebit) {
        this.currentCycleDebit = currentCycleDebit;
    }

    public BigDecimal getCurrentCycleCredit() {
        return currentCycleCredit;
    }

    public void setCurrentCycleCredit(BigDecimal currentCycleCredit) {
        this.currentCycleCredit = currentCycleCredit;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
