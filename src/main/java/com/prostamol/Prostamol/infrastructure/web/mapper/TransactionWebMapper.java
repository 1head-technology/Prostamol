package com.prostamol.Prostamol.infrastructure.web.mapper;

import com.prostamol.Prostamol.domain.model.transaction.Transaction;
import com.prostamol.Prostamol.infrastructure.web.dto.response.TransactionResponse;
import org.springframework.stereotype.Component;

@Component
public class TransactionWebMapper {

    public TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(), t.getUserId(), t.getAccountId(), t.getType(),
                t.getAmount().amount(), t.getAmount().currency(),
                t.getDate(), t.getDescription(), t.getCategoryId(),
                t.getLinkedTransactionId(), t.isRecurring(), t.getRecurrenceFrequency()
        );
    }
}
