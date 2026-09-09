package com.prostamol.Prostamol.application.usecase.transaction;

import com.prostamol.Prostamol.domain.model.transaction.Transaction;
import com.prostamol.Prostamol.domain.port.in.transaction.DeleteTransactionUseCase;
import com.prostamol.Prostamol.domain.port.out.TransactionRepositoryPort;
import org.springframework.security.access.AccessDeniedException;

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
            throw new AccessDeniedException("Transaction does not belong to the authenticated user");
        }

        if (transaction.getLinkedTransactionId() != null) {
            Transaction linkedTransaction = transactionRepository
                .findById(transaction.getLinkedTransactionId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Linked transaction not found: " + transaction.getLinkedTransactionId()));

            if (!linkedTransaction.getUserId().equals(command.userId())) {
                throw new AccessDeniedException("Linked transaction does not belong to the authenticated user");
            }

            transactionRepository.deleteById(linkedTransaction.getId());
        }

        transactionRepository.deleteById(transaction.getId());
    }
}
