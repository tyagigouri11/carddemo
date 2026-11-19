package com.carddemo.controller;

import com.carddemo.dto.CardDto;
import com.carddemo.entity.Card;
import com.carddemo.mapper.CardMapper;
import com.carddemo.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cards")
@Validated
@Tag(name = "Cards", description = "Card management endpoints")
public class CardController {

    private static final Logger logger = LoggerFactory.getLogger(CardController.class);

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/{number}")
    @Operation(summary = "Get card by number", description = "Retrieve card details by card number")
    public ResponseEntity<CardDto> getCard(@PathVariable String number) {
        Card card = cardService.getCard(number);
        return ResponseEntity.ok(CardMapper.toDto(card));
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get cards by account", description = "List all cards for an account")
    public ResponseEntity<List<CardDto>> getCardsByAccount(@PathVariable Long accountId) {
        List<Card> cards = cardService.getCardsByAccount(accountId);
        return ResponseEntity.ok(cards.stream().map(CardMapper::toDto).collect(Collectors.toList()));
    }

    @PostMapping
    @Operation(summary = "Create new card", description = "Issue a new card for an account")
    public ResponseEntity<CardDto> createCard(@Valid @RequestBody CardDto cardDto) {
        Card card = cardService.issueCard(cardDto.getAccountId(), cardDto.getNumber(), cardDto.getExpiryDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(CardMapper.toDto(card));
    }
}
