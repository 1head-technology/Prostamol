package com.prostamol.Prostamol.application.usecase.transaction;

import com.prostamol.Prostamol.domain.model.transaction.Transaction;
import com.prostamol.Prostamol.domain.model.transaction.TransactionType;
import com.prostamol.Prostamol.domain.port.in.transaction.RecordTransferUseCase;
import com.prostamol.Prostamol.domain.port.out.AccountRepositoryPort;
import com.prostamol.Prostamol.domain.port.out.TransactionRepositoryPort;

import java.util.List;
import java.util.UUID;

public class RecordTransferService implements RecordTransferUseCase {

    private final TransactionRepositoryPort transactionRepository;
    private final AccountRepositoryPort accountRepository;

    public RecordTransferService(
        TransactionRepositoryPort transactionRepository,
        AccountRepositoryPort accountRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public List<Transaction> execute(Command command) {
        accountRepository
            .findById(command.sourceAccountId())
            .orElseThrow(() -> new IllegalArgumentException("Source account not found: " + command.sourceAccountId()));

        accountRepository
            .findById(command.destinationAccountId())
            .orElseThrow(() -> new IllegalArgumentException("Destination account not found: " + command.destinationAccountId()));

        // Generate both IDs upfront so each transaction already references the other
        UUID outId = UUID.randomUUID();
        UUID inId = UUID.randomUUID();

        Transaction transferOut = new Transaction(
            outId,
            command.userId(),
            command.sourceAccountId(),
            TransactionType.TRANSFER_OUT,
            command.amount(),
            command.date(),
            command.description(),
            null,
            inId,
            false,
            null
        );
        Transaction transferIn = new Transaction(
            inId,
            command.userId(),
            command.destinationAccountId(),
            TransactionType.TRANSFER_IN,
            command.amount(),
            command.date(),
            command.description(),
            null,
            outId,
            false,
            null
        );
        return transactionRepository.saveAll(List.of(transferOut, transferIn));
    }
}
