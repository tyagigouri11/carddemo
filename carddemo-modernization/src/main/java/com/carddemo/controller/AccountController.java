package com.carddemo.controller;

import com.carddemo.dto.AccountDto;
import com.carddemo.dto.CardDto;
import com.carddemo.entity.Account;
import com.carddemo.entity.Card;
import com.carddemo.entity.Transaction;
import com.carddemo.mapper.AccountMapper;
import com.carddemo.mapper.CardMapper;
import com.carddemo.service.AccountService;
import com.carddemo.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for Account endpoints.
 */
@RestController
@RequestMapping("/api/accounts")
@Validated
@Tag(name = "Accounts", description = "Account management endpoints")
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    private final AccountService accountService;
    private final CardService cardService;

    public AccountController(AccountService accountService, CardService cardService) {
        this.accountService = accountService;
        this.cardService = cardService;
    }

    @GetMapping
    @Operation(summary = "List all accounts", description = "Retrieve all active accounts")
    public ResponseEntity<List<AccountDto>> listAccounts() {
        List<Account> accounts = accountService.findActiveAccounts();
        List<AccountDto> dtos = accounts.stream()
            .map(AccountMapper::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID", description = "Retrieve account details by account ID")
    public ResponseEntity<AccountDto> getAccount(@PathVariable Long id) {
        Account account = accountService.getAccount(id);
        return ResponseEntity.ok(AccountMapper.toDto(account));
    }

    @PostMapping
    @Operation(summary = "Create new account", description = "Create a new account with credit limit and initial balance")
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody AccountDto accountDto) {
        Account account = new Account();
        account.setCreditLimit(accountDto.getCreditLimit());
        account.setCurrentBalance(accountDto.getCurrentBalance());
        account.setActiveStatus(accountDto.getActiveStatus());
        account.setOpenDate(accountDto.getOpenDate());
        account.setExpirationDate(accountDto.getExpirationDate());
        
        Account created = accountService.createAccount(account);
        logger.info("Account created: id={}, creditLimit={}", created.getId(), created.getCreditLimit());
        return ResponseEntity.status(HttpStatus.CREATED).body(AccountMapper.toDto(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update account", description = "Update account balance and active status")
    public ResponseEntity<AccountDto> updateAccount(@PathVariable Long id, @Valid @RequestBody AccountDto accountDto) {
        Account account = accountService.getAccount(id);
        account.setCurrentBalance(accountDto.getCurrentBalance());
        account.setActiveStatus(accountDto.getActiveStatus());
        Account updated = accountService.getAccount(id);
        return ResponseEntity.ok(AccountMapper.toDto(updated));
    }

    @GetMapping("/{id}/transactions")
    @Operation(summary = "Get account transactions", description = "List transactions for an account within date range")
    public ResponseEntity<List<?>> getAccountTransactions(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<Transaction> transactions = accountService.getAccountTransactions(id, from, to);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/{id}/balance")
    @Operation(summary = "Post debit or credit to account", description = "Update account balance with debit/credit transaction")
    public ResponseEntity<AccountDto> postBalance(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String typeStr = request.get("type").toString();
        String merchantId = (String) request.getOrDefault("merchantId", "");
        String merchantName = (String) request.getOrDefault("merchantName", "");
        
        Transaction.TransactionType type = Transaction.TransactionType.valueOf(typeStr);
        Account updated = accountService.updateBalance(id, amount, type, merchantId, merchantName);
        return ResponseEntity.ok(AccountMapper.toDto(updated));
    }

    @PostMapping("/{id}/reissue")
    @Operation(summary = "Reissue card for account", description = "Issue a new card and block old cards")
    public ResponseEntity<CardDto> reissueCard(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String newCardNumber = request.get("cardNumber");
        LocalDate expiryDate = LocalDate.parse(request.getOrDefault("expiryDate", ""));
        
        Card newCard = accountService.reissueCard(id, newCardNumber, expiryDate);
        return ResponseEntity.status(HttpStatus.CREATED).body(CardMapper.toDto(newCard));
    }

    @GetMapping("/{id}/cards")
    @Operation(summary = "Get account cards", description = "List all cards for an account")
    public ResponseEntity<List<CardDto>> getAccountCards(@PathVariable Long id) {
        List<Card> cards = cardService.getCardsByAccount(id);
        List<CardDto> cardDtos = cards.stream()
            .map(CardMapper::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(cardDtos);
    }
}
