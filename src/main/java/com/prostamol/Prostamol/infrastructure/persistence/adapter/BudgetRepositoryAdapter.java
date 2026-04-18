package com.prostamol.Prostamol.infrastructure.persistence.adapter;

import com.prostamol.Prostamol.domain.model.budget.Budget;
import com.prostamol.Prostamol.domain.port.out.BudgetRepositoryPort;
import com.prostamol.Prostamol.infrastructure.persistence.mapper.BudgetPersistenceMapper;
import com.prostamol.Prostamol.infrastructure.persistence.repository.BudgetJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class BudgetRepositoryAdapter implements BudgetRepositoryPort {

    private final BudgetJpaRepository jpaRepository;
    private final BudgetPersistenceMapper mapper;

    public BudgetRepositoryAdapter(BudgetJpaRepository jpaRepository, BudgetPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Budget save(Budget budget) {
        return mapper.toDomain(jpaRepository.save(mapper.toJpaEntity(budget)));
    }

    @Override
    public Optional<Budget> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Budget> findAllByUserId(UUID userId) {
        return jpaRepository.findAllByUserId(userId).stream().map(mapper::toDomain).collect(Collectors.toList());
    }
}
