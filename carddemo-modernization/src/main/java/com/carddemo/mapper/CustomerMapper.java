package com.carddemo.mapper;

import com.carddemo.dto.CustomerDto;
import com.carddemo.entity.Customer;

/**
 * Manual mapper for Customer entity ↔ CustomerDto conversions.
 * Replaces MapStruct with explicit static conversion methods.
 */
public class CustomerMapper {
    
    public static CustomerDto toDto(Customer entity) {
        if (entity == null) {
            return null;
        }
        return new CustomerDto(
            entity.getId(),
            entity.getName(),
            entity.getContact()
        );
    }
    
    public static Customer toEntity(CustomerDto dto) {
        if (dto == null) {
            return null;
        }
        Customer customer = new Customer();
        customer.setId(dto.getId());
        customer.setName(dto.getName());
        customer.setContact(dto.getContact());
        return customer;
    }
}
