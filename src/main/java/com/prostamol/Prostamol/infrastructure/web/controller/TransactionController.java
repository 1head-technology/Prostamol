package com.prostamol.Prostamol.infrastructure.web.controller;

import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.domain.port.in.transaction.GetTransactionsUseCase;
import com.prostamol.Prostamol.domain.port.in.transaction.RecordTransactionUseCase;
import com.prostamol.Prostamol.domain.port.in.transaction.RecordTransferUseCase;
import com.prostamol.Prostamol.infrastructure.web.dto.request.RecordTransactionRequest;
import com.prostamol.Prostamol.infrastructure.web.dto.request.RecordTransferRequest;
import com.prostamol.Prostamol.infrastructure.web.dto.response.TransactionResponse;
import com.prostamol.Prostamol.infrastructure.web.mapper.TransactionWebMapper;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class TransactionController {

    private final RecordTransactionUseCase recordTransaction;
    private final RecordTransferUseCase recordTransfer;
    private final GetTransactionsUseCase getTransactions;
    private final TransactionWebMapper mapper;

    public TransactionController(
        RecordTransactionUseCase recordTransaction,
        RecordTransferUseCase recordTransfer,
        GetTransactionsUseCase getTransactions,
        TransactionWebMapper mapper
    ) {
        this.recordTransaction = recordTransaction;
        this.recordTransfer = recordTransfer;
        this.getTransactions = getTransactions;
        this.mapper = mapper;
    }

    @PostMapping("/users/{userId}/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse record(
        @PathVariable UUID userId,
        @Valid @RequestBody RecordTransactionRequest request
    ) {
        return mapper.toResponse(recordTransaction.execute(new RecordTransactionUseCase.Command(
            userId,
            request.accountId(),
            request.type(),
            Money.of(request.amount(), request.currency()),
            request.date(),
            request.description(),
            request.categoryId(),
            request.recurring(),
            request.recurrenceFrequency()
        )));
    }

    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    public List<TransactionResponse> transfer(@Valid @RequestBody RecordTransferRequest request) {
        return recordTransfer.execute(new RecordTransferUseCase.Command(
            request.userId(),
            request.sourceAccountId(),
            request.destinationAccountId(),
            Money.of(request.amount(), request.currency()),
            request.date(),
            request.description()
        ))
            .stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public List<TransactionResponse> byAccount(
        @PathVariable UUID accountId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return getTransactions.execute(accountId, from, to)
            .stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());
    }

    @GetMapping("/users/{userId}/transactions")
    public List<TransactionResponse> byUser(@PathVariable UUID userId) {
        return getTransactions.execute(userId)
            .stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());
    }
}
