package com.carddemo.mapper;

import com.carddemo.dto.AccountDto;
import com.carddemo.entity.Account;

/**
 * Manual mapper for Account entity ↔ AccountDto conversions.
 * Replaces MapStruct with explicit static conversion methods.
 */
public class AccountMapper {
    
    public static AccountDto toDto(Account entity) {
        if (entity == null) {
            return null;
        }
        return new AccountDto(
            entity.getId(),
            entity.getCreditLimit(),
            entity.getCurrentBalance(),
            entity.getActiveStatus(),
            entity.getReissueDate(),
            entity.getOpenDate(),
            entity.getExpirationDate(),
            entity.getCashCreditLimit(),
            entity.getCurrentCycleDebit(),
            entity.getCurrentCycleCredit(),
            entity.getGroupId()
        );
    }
    
    public static Account toEntity(AccountDto dto) {
        if (dto == null) {
            return null;
        }
        Account account = new Account();
        account.setId(dto.getId());
        account.setCreditLimit(dto.getCreditLimit());
        account.setCurrentBalance(dto.getCurrentBalance());
        account.setActiveStatus(dto.getActiveStatus());
        account.setReissueDate(dto.getReissueDate());
        account.setOpenDate(dto.getOpenDate());
        account.setExpirationDate(dto.getExpirationDate());
        account.setCashCreditLimit(dto.getCashCreditLimit());
        account.setCurrentCycleDebit(dto.getCurrentCycleDebit());
        account.setCurrentCycleCredit(dto.getCurrentCycleCredit());
        account.setGroupId(dto.getGroupId());
        return account;
    }
}
