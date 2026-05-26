package com.prostamol.Prostamol.infrastructure.web.controller;

import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.domain.port.in.transaction.DeleteTransactionUseCase;
import com.prostamol.Prostamol.domain.port.in.transaction.GetTransactionsUseCase;
import com.prostamol.Prostamol.domain.port.in.transaction.RecordTransactionUseCase;
import com.prostamol.Prostamol.domain.port.in.transaction.RecordTransferUseCase;
import com.prostamol.Prostamol.domain.port.in.transaction.UpdateTransactionUseCase;
import com.prostamol.Prostamol.infrastructure.web.dto.request.RecordTransactionRequest;
import com.prostamol.Prostamol.infrastructure.web.dto.request.RecordTransferRequest;
import com.prostamol.Prostamol.infrastructure.web.dto.request.UpdateTransactionRequest;
import com.prostamol.Prostamol.infrastructure.web.dto.response.TransactionResponse;
import com.prostamol.Prostamol.infrastructure.web.mapper.TransactionWebMapper;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final UpdateTransactionUseCase updateTransaction;
    private final DeleteTransactionUseCase deleteTransaction;
    private final TransactionWebMapper mapper;

    public TransactionController(
        RecordTransactionUseCase recordTransaction,
        RecordTransferUseCase recordTransfer,
        GetTransactionsUseCase getTransactions,
        UpdateTransactionUseCase updateTransaction,
        DeleteTransactionUseCase deleteTransaction,
        TransactionWebMapper mapper
    ) {
        this.recordTransaction = recordTransaction;
        this.recordTransfer = recordTransfer;
        this.getTransactions = getTransactions;
        this.updateTransaction = updateTransaction;
        this.deleteTransaction = deleteTransaction;
        this.mapper = mapper;
    }

    // ── Authenticated user endpoints ─────────────────────────────────────────

    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse record(
        @AuthenticationPrincipal UUID userId,
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
    public List<TransactionResponse> transfer(
        @AuthenticationPrincipal UUID userId,
        @Valid @RequestBody RecordTransferRequest request
    ) {
        RecordTransferUseCase.Command recordTransferCommand = new RecordTransferUseCase.Command(
            userId,
            request.sourceAccountId(),
            request.destinationAccountId(),
            Money.of(request.amount(), request.currency()),
            request.date(),
            request.description()
        );

        System.out.println(
            userId.toString()
            + request.sourceAccountId()
            + request.destinationAccountId()
            + request.amount()
            + request.currency()
            + request.date()
            + request.description()
        );

        return recordTransfer.execute(recordTransferCommand)
            .stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());
    }

    @GetMapping("/transactions")
    public List<TransactionResponse> list(@AuthenticationPrincipal UUID userId) {
        return getTransactions.execute(userId)
            .stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());
    }

    @PatchMapping("/transactions/{transactionId}")
    public TransactionResponse patch(
        @AuthenticationPrincipal UUID userId,
        @PathVariable UUID transactionId,
        @Valid @RequestBody UpdateTransactionRequest request
    ) {
        return mapper.toResponse(updateTransaction.execute(toUpdateCommand(userId, transactionId, request)));
    }

    @DeleteMapping("/transactions/{transactionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @AuthenticationPrincipal UUID userId,
        @PathVariable UUID transactionId
    ) {
        DeleteTransactionUseCase.Command deleteTransactionCommand = new DeleteTransactionUseCase.Command(userId, transactionId);

        deleteTransaction.execute(deleteTransactionCommand);
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

    // ── Admin endpoints ──────────────────────────────────────────────────────

    @PostMapping("/admin/users/{userId}/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse recordByAdmin(
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

    @GetMapping("/admin/users/{userId}/transactions")
    public List<TransactionResponse> listByAdmin(@PathVariable UUID userId) {
        return getTransactions.execute(userId)
            .stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());
    }

    @PatchMapping("/admin/users/{userId}/transactions/{transactionId}")
    public TransactionResponse patchByAdmin(
        @PathVariable UUID userId,
        @PathVariable UUID transactionId,
        @Valid @RequestBody UpdateTransactionRequest request
    ) {
        return mapper.toResponse(updateTransaction.execute(toUpdateCommand(userId, transactionId, request)));
    }

    @DeleteMapping("/admin/users/{userId}/transactions/{transactionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByAdmin(
        @PathVariable UUID userId,
        @PathVariable UUID transactionId
    ) {
        deleteTransaction.execute(new DeleteTransactionUseCase.Command(userId, transactionId));
    }

    private UpdateTransactionUseCase.Command toUpdateCommand(
        UUID userId,
        UUID transactionId,
        UpdateTransactionRequest request
    ) {
        return new UpdateTransactionUseCase.Command(
            userId,
            transactionId,
            request.amount(),
            request.currency(),
            request.date(),
            request.description(),
            request.categoryId(),
            request.recurring(),
            request.recurrenceFrequency()
        );
    }
}
