package com.carddemo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for TransactionType entity in API requests/responses.
 */
@Schema(description = "Transaction type information")
public class TransactionTypeDto {

    @NotBlank(message = "Transaction type ID is required")
    @Schema(description = "Transaction type ID", example = "PURCH")
    private String id;

    @NotBlank(message = "Description is required")
    @Schema(description = "Transaction type description", example = "Purchase Transaction")
    private String description;

    @Schema(description = "Transaction type category", example = "DEBIT")
    private String category;

    public TransactionTypeDto() {
    }

    public TransactionTypeDto(String id, String description, String category) {
        this.id = id;
        this.description = description;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
