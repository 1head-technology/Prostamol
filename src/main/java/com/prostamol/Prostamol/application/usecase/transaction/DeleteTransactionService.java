package com.prostamol.Prostamol.application.usecase.transaction;

import com.prostamol.Prostamol.domain.model.transaction.Transaction;
import com.prostamol.Prostamol.domain.port.in.transaction.DeleteTransactionUseCase;
import com.prostamol.Prostamol.domain.port.out.TransactionRepositoryPort;

public class DeleteTransactionService implements DeleteTransactionUseCase {

    private final TransactionRepositoryPort transactionRepository;

    public DeleteTransactionService(TransactionRepositoryPort transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void execute(Command command) {
        Transaction transaction = transactionRepository
            .findById(command.transactionId())
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + command.transactionId()));

        if (!transaction.getUserId().equals(command.userId())) {
            throw new IllegalArgumentException("Transaction does not belong to user: " + command.userId());
        }

        if (transaction.getLinkedTransactionId() != null) {
            transactionRepository.deleteById(transaction.getLinkedTransactionId());
        }

        transactionRepository.deleteById(transaction.getId());
    }
}
