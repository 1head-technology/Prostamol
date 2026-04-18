package com.prostamol.Prostamol.infrastructure.persistence.adapter;

import com.prostamol.Prostamol.domain.model.transaction.Transaction;
import com.prostamol.Prostamol.domain.port.out.TransactionRepositoryPort;
import com.prostamol.Prostamol.infrastructure.persistence.mapper.TransactionPersistenceMapper;
import com.prostamol.Prostamol.infrastructure.persistence.repository.TransactionJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TransactionRepositoryAdapter implements TransactionRepositoryPort {

    private final TransactionJpaRepository jpaRepository;
    private final TransactionPersistenceMapper mapper;

    public TransactionRepositoryAdapter(TransactionJpaRepository jpaRepository, TransactionPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Transaction save(Transaction transaction) {
        return mapper.toDomain(jpaRepository.save(mapper.toJpaEntity(transaction)));
    }

    @Override
    public List<Transaction> saveAll(List<Transaction> transactions) {
        return jpaRepository.saveAll(transactions.stream().map(mapper::toJpaEntity).collect(Collectors.toList()))
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findAllByAccountId(UUID accountId) {
        return jpaRepository.findAllByAccountId(accountId).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findAllByUserId(UUID userId) {
        return jpaRepository.findAllByUserId(userId).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findAllByAccountIdAndDateBetween(
        UUID accountId,
        LocalDate from,
        LocalDate to
    ) {
        return jpaRepository.findAllByAccountIdAndDateBetween(accountId, from, to)
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findAllByUserIdAndCategoryIdAndDateBetween(
        UUID userId,
        UUID categoryId,
        LocalDate from,
        LocalDate to
    ) {
        return jpaRepository.findAllByUserIdAndCategoryIdAndDateBetween(userId, categoryId, from, to)
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }
}
