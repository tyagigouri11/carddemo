package com.carddemo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for Customer entity in API requests/responses.
 */
@Schema(description = "Customer information")
public class CustomerDto {

    @Schema(description = "Customer ID", example = "1")
    private Long id;

    @NotBlank(message = "Customer name is required")
    @Schema(description = "Customer full name", example = "John Smith")
    private String name;

    @Schema(description = "Customer contact (email/phone)", example = "john.smith@example.com")
    private String contact;

    public CustomerDto() {
    }

    public CustomerDto(Long id, String name, String contact) {
        this.id = id;
        this.name = name;
        this.contact = contact;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}
