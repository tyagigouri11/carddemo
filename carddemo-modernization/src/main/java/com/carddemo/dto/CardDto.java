package com.carddemo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.carddemo.entity.Card;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO for Card entity in API requests/responses.
 */
@Schema(description = "Card information")
public class CardDto {

    @Schema(description = "Card ID", example = "1")
    private Long id;

    @NotBlank(message = "Card number is required")
    @Schema(description = "Card number (PAN)", example = "4111111111111111")
    private String number;

    @NotNull(message = "Account ID is required")
    @Schema(description = "Account ID", example = "1")
    private Long accountId;

    @NotNull(message = "Card status is required")
    @Schema(description = "Card status", example = "ACTIVE", allowableValues = {"ACTIVE", "EXPIRED", "BLOCKED"})
    private Card.CardStatus status;

    @NotNull(message = "Expiry date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Card expiry date", example = "2028-12-31")
    private LocalDate expiryDate;

    public CardDto() {
    }

    public CardDto(Long id, String number, Long accountId, Card.CardStatus status, LocalDate expiryDate) {
        this.id = id;
        this.number = number;
        this.accountId = accountId;
        this.status = status;
        this.expiryDate = expiryDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Card.CardStatus getStatus() {
        return status;
    }

    public void setStatus(Card.CardStatus status) {
        this.status = status;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }
}
