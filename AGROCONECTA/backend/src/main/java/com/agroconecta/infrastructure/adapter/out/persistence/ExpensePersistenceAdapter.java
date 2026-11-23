package com.agroconecta.infrastructure.adapter.out.persistence;

import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.port.out.ExpenseRepositoryPort;
import com.agroconecta.domain.model.Expense;
import com.agroconecta.infrastructure.adapter.out.persistence.mapper.ExpenseJpaMapper;
import com.agroconecta.infrastructure.adapter.out.persistence.mapper.PageMapper;
import com.agroconecta.infrastructure.adapter.out.persistence.repository.ExpenseJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class ExpensePersistenceAdapter implements ExpenseRepositoryPort {

    private final ExpenseJpaRepository repository;

    public ExpensePersistenceAdapter(ExpenseJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Expense> findById(Long id) {
        return repository.findById(id).map(ExpenseJpaMapper::toDomain);
    }

    @Override
    public Expense save(Expense expense) {
        return ExpenseJpaMapper.toDomain(repository.save(ExpenseJpaMapper.toEntity(expense)));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public PageResult<Expense> findByOwnerId(Long ownerId, PageQuery pageQuery) {
        PageRequest pageable = PageRequest.of(pageQuery.page(), pageQuery.size(),
                Sort.by("expenseDate").descending());
        return PageMapper.toPageResult(repository.findByOwnerId(ownerId, pageable), ExpenseJpaMapper::toDomain);
    }

    @Override
    public BigDecimal sumAmountByOwnerId(Long ownerId) {
        BigDecimal total = repository.sumAmountByOwnerId(ownerId);
        return total == null ? BigDecimal.ZERO : total;
    }

    @Override
    public long countByOwnerId(Long ownerId) {
        return repository.countByOwnerId(ownerId);
    }
}
