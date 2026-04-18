package com.prostamol.Prostamol.infrastructure.persistence.adapter;

import com.prostamol.Prostamol.domain.model.savings.SavingsGoal;
import com.prostamol.Prostamol.domain.port.out.SavingsGoalRepositoryPort;
import com.prostamol.Prostamol.infrastructure.persistence.mapper.SavingsGoalPersistenceMapper;
import com.prostamol.Prostamol.infrastructure.persistence.repository.SavingsGoalJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class SavingsGoalRepositoryAdapter implements SavingsGoalRepositoryPort {

    private final SavingsGoalJpaRepository jpaRepository;
    private final SavingsGoalPersistenceMapper mapper;

    public SavingsGoalRepositoryAdapter(SavingsGoalJpaRepository jpaRepository, SavingsGoalPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public SavingsGoal save(SavingsGoal goal) {
        return mapper.toDomain(jpaRepository.save(mapper.toJpaEntity(goal)));
    }

    @Override
    public Optional<SavingsGoal> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<SavingsGoal> findAllByUserId(UUID userId) {
        return jpaRepository.findAllByUserId(userId).stream().map(mapper::toDomain).collect(Collectors.toList());
    }
}
