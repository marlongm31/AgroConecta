package com.agroconecta.application.usecase;

import com.agroconecta.application.command.ExpenseCommand;
import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.port.in.ExpenseUseCase;
import com.agroconecta.application.port.out.CropRepositoryPort;
import com.agroconecta.application.port.out.ExpenseRepositoryPort;
import com.agroconecta.application.port.out.ParcelRepositoryPort;
import com.agroconecta.domain.exception.BusinessRuleException;
import com.agroconecta.domain.exception.ForbiddenException;
import com.agroconecta.domain.exception.NotFoundException;
import com.agroconecta.domain.model.Crop;
import com.agroconecta.domain.model.Expense;

import java.util.Objects;

public class ExpenseService implements ExpenseUseCase {

    private final ExpenseRepositoryPort expenseRepository;
    private final ParcelRepositoryPort parcelRepository;
    private final CropRepositoryPort cropRepository;

    public ExpenseService(ExpenseRepositoryPort expenseRepository, ParcelRepositoryPort parcelRepository,
                          CropRepositoryPort cropRepository) {
        this.expenseRepository = expenseRepository;
        this.parcelRepository = parcelRepository;
        this.cropRepository = cropRepository;
    }

    @Override
    public PageResult<Expense> list(Long ownerId, PageQuery pageQuery) {
        return expenseRepository.findByOwnerId(ownerId, pageQuery);
    }

    @Override
    public Expense get(Long ownerId, Long expenseId) {
        return requireOwnedExpense(ownerId, expenseId);
    }

    @Override
    public Expense create(Long ownerId, ExpenseCommand command) {
        validateRelations(ownerId, command.parcelId(), command.cropId());
        Expense expense = Expense.create(ownerId, command.parcelId(), command.cropId(), command.concept(),
                command.amount(), command.expenseDate());
        return expenseRepository.save(expense);
    }

    @Override
    public Expense update(Long ownerId, Long expenseId, ExpenseCommand command) {
        Expense expense = requireOwnedExpense(ownerId, expenseId);
        validateRelations(ownerId, command.parcelId(), command.cropId());
        Expense updated = expense.update(command.parcelId(), command.cropId(), command.concept(),
                command.amount(), command.expenseDate());
        return expenseRepository.save(updated);
    }

    @Override
    public void delete(Long ownerId, Long expenseId) {
        requireOwnedExpense(ownerId, expenseId);
        expenseRepository.deleteById(expenseId);
    }

    private Expense requireOwnedExpense(Long ownerId, Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new NotFoundException("Gasto", expenseId));
        if (!Objects.equals(expense.ownerId(), ownerId)) {
            throw new ForbiddenException("No tienes permiso para acceder a este gasto");
        }
        return expense;
    }

    private void validateRelations(Long ownerId, Long parcelId, Long cropId) {
        if (parcelId == null && cropId == null) {
            throw new BusinessRuleException("Selecciona una parcela o un cultivo para el gasto");
        }
        if (parcelId != null && !parcelRepository.belongsToOwner(parcelId, ownerId)) {
            throw new ForbiddenException("La parcela indicada no existe o no te pertenece");
        }
        if (cropId != null) {
            Crop crop = cropRepository.findById(cropId)
                    .orElseThrow(() -> new NotFoundException("Cultivo", cropId));
            if (!cropRepository.belongsToOwner(cropId, ownerId)) {
                throw new ForbiddenException("El cultivo indicado no existe o no te pertenece");
            }
            if (parcelId != null && !Objects.equals(crop.parcelId(), parcelId)) {
                throw new BusinessRuleException("El cultivo no pertenece a la parcela seleccionada");
            }
        }
    }
}
