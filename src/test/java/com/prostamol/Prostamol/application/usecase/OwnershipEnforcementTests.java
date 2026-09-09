package com.prostamol.Prostamol.application.usecase;

import com.prostamol.Prostamol.application.usecase.account.GetAccountBalanceService;
import com.prostamol.Prostamol.application.usecase.budget.GetBudgetSummaryService;
import com.prostamol.Prostamol.application.usecase.savings.AddSavingsContributionService;
import com.prostamol.Prostamol.application.usecase.transaction.DeleteTransactionService;
import com.prostamol.Prostamol.application.usecase.transaction.GetTransactionsService;
import com.prostamol.Prostamol.application.usecase.transaction.RecordTransactionService;
import com.prostamol.Prostamol.application.usecase.transaction.RecordTransferService;
import com.prostamol.Prostamol.application.usecase.transaction.UpdateTransactionService;
import com.prostamol.Prostamol.domain.model.account.Account;
import com.prostamol.Prostamol.domain.model.account.AccountType;
import com.prostamol.Prostamol.domain.model.budget.Budget;
import com.prostamol.Prostamol.domain.model.budget.BudgetLine;
import com.prostamol.Prostamol.domain.model.budget.BudgetStatus;
import com.prostamol.Prostamol.domain.model.category.Category;
import com.prostamol.Prostamol.domain.model.category.CategoryType;
import com.prostamol.Prostamol.domain.model.savings.SavingsGoal;
import com.prostamol.Prostamol.domain.model.savings.SavingsGoalStatus;
import com.prostamol.Prostamol.domain.model.shared.DateRange;
import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.domain.model.transaction.Transaction;
import com.prostamol.Prostamol.domain.model.transaction.TransactionType;
import com.prostamol.Prostamol.domain.port.in.transaction.RecordTransactionUseCase;
import com.prostamol.Prostamol.domain.port.in.transaction.RecordTransferUseCase;
import com.prostamol.Prostamol.domain.port.in.transaction.UpdateTransactionUseCase;
import com.prostamol.Prostamol.domain.port.in.transaction.DeleteTransactionUseCase;
import com.prostamol.Prostamol.domain.port.out.AccountRepositoryPort;
import com.prostamol.Prostamol.domain.port.out.BudgetRepositoryPort;
import com.prostamol.Prostamol.domain.port.out.CategoryRepositoryPort;
import com.prostamol.Prostamol.domain.port.out.SavingsGoalRepositoryPort;
import com.prostamol.Prostamol.domain.port.out.TransactionRepositoryPort;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OwnershipEnforcementTests {

    private final UUID userId = UUID.randomUUID();
    private final UUID otherUserId = UUID.randomUUID();
    private final Money amount = Money.of(BigDecimal.TEN, "EUR");

    @Test
    void accountBalanceRejectsAnotherUsersAccountBeforeReadingTransactions() {
        AccountRepositoryPort accounts = mock(AccountRepositoryPort.class);
        TransactionRepositoryPort transactions = mock(TransactionRepositoryPort.class);
        Account account = account(otherUserId);
        when(accounts.findById(account.getId())).thenReturn(Optional.of(account));

        var service = new GetAccountBalanceService(accounts, transactions);

        assertThrows(AccessDeniedException.class, () -> service.execute(userId, account.getId()));
        verify(transactions, never()).findAllByAccountId(any());
    }

    @Test
    void accountHistoryRejectsAnotherUsersAccountBeforeReadingTransactions() {
        AccountRepositoryPort accounts = mock(AccountRepositoryPort.class);
        TransactionRepositoryPort transactions = mock(TransactionRepositoryPort.class);
        Account account = account(otherUserId);
        when(accounts.findById(account.getId())).thenReturn(Optional.of(account));

        var service = new GetTransactionsService(transactions, accounts);

        assertThrows(AccessDeniedException.class,
            () -> service.execute(userId, account.getId(), null, null));
        verify(transactions, never()).findAllByAccountId(any());
    }

    @Test
    void budgetSummaryRejectsAnotherUsersBudgetBeforeReadingItsDetails() {
        BudgetRepositoryPort budgets = mock(BudgetRepositoryPort.class);
        TransactionRepositoryPort transactions = mock(TransactionRepositoryPort.class);
        CategoryRepositoryPort categories = mock(CategoryRepositoryPort.class);
        Budget budget = budget(otherUserId);
        when(budgets.findById(budget.getId())).thenReturn(Optional.of(budget));

        var service = new GetBudgetSummaryService(budgets, transactions, categories);

        assertThrows(AccessDeniedException.class, () -> service.execute(userId, budget.getId()));
        verify(categories, never()).findById(any());
    }

    @Test
    void budgetSummaryRejectsLegacyLineReferencingAnotherUsersCategory() {
        BudgetRepositoryPort budgets = mock(BudgetRepositoryPort.class);
        TransactionRepositoryPort transactions = mock(TransactionRepositoryPort.class);
        CategoryRepositoryPort categories = mock(CategoryRepositoryPort.class);
        Category foreignCategory = category(otherUserId, false);
        Budget budget = new Budget(UUID.randomUUID(), userId, "Budget",
            new DateRange(LocalDate.now(), LocalDate.now().plusDays(1)), BudgetStatus.ACTIVE,
            List.of(new BudgetLine(UUID.randomUUID(), foreignCategory.getId(), amount)));
        when(budgets.findById(budget.getId())).thenReturn(Optional.of(budget));
        when(categories.findById(foreignCategory.getId())).thenReturn(Optional.of(foreignCategory));

        var service = new GetBudgetSummaryService(budgets, transactions, categories);

        assertThrows(IllegalArgumentException.class, () -> service.execute(userId, budget.getId()));
        verify(transactions, never()).findAllByUserIdAndCategoryIdAndDateBetween(any(), any(), any(), any());
    }

    @Test
    void contributionRejectsAnotherUsersGoalWithoutChangingOrSavingIt() {
        SavingsGoalRepositoryPort goals = mock(SavingsGoalRepositoryPort.class);
        SavingsGoal goal = new SavingsGoal(UUID.randomUUID(), otherUserId, "Goal", amount,
            Money.zero("EUR"), LocalDate.now().plusDays(1), SavingsGoalStatus.ACTIVE);
        when(goals.findById(goal.getId())).thenReturn(Optional.of(goal));

        var service = new AddSavingsContributionService(goals);

        assertThrows(AccessDeniedException.class, () -> service.execute(userId, goal.getId(), amount));
        assertEquals(BigDecimal.ZERO, goal.getCurrentAmount().amount());
        verify(goals, never()).save(any());
    }

    @Test
    void transactionCreationRejectsAnotherUsersAccount() {
        AccountRepositoryPort accounts = mock(AccountRepositoryPort.class);
        CategoryRepositoryPort categories = mock(CategoryRepositoryPort.class);
        TransactionRepositoryPort transactions = mock(TransactionRepositoryPort.class);
        Account account = account(otherUserId);
        when(accounts.findById(account.getId())).thenReturn(Optional.of(account));

        var service = new RecordTransactionService(transactions, accounts, categories);

        assertThrows(AccessDeniedException.class,
            () -> service.execute(recordCommand(account.getId(), UUID.randomUUID())));
        verify(categories, never()).findById(any());
        verify(transactions, never()).save(any());
    }

    @Test
    void transactionCreationRejectsAnotherUsersCategory() {
        AccountRepositoryPort accounts = mock(AccountRepositoryPort.class);
        CategoryRepositoryPort categories = mock(CategoryRepositoryPort.class);
        TransactionRepositoryPort transactions = mock(TransactionRepositoryPort.class);
        Account account = account(userId);
        Category category = category(otherUserId, false);
        when(accounts.findById(account.getId())).thenReturn(Optional.of(account));
        when(categories.findById(category.getId())).thenReturn(Optional.of(category));

        var service = new RecordTransactionService(transactions, accounts, categories);

        assertThrows(AccessDeniedException.class,
            () -> service.execute(recordCommand(account.getId(), category.getId())));
        verify(transactions, never()).save(any());
    }

    @Test
    void transferRejectsWhenEitherAccountBelongsToAnotherUser() {
        AccountRepositoryPort accounts = mock(AccountRepositoryPort.class);
        TransactionRepositoryPort transactions = mock(TransactionRepositoryPort.class);
        Account source = account(userId);
        Account destination = account(otherUserId);
        when(accounts.findById(source.getId())).thenReturn(Optional.of(source));
        when(accounts.findById(destination.getId())).thenReturn(Optional.of(destination));

        var service = new RecordTransferService(transactions, accounts);
        var command = new RecordTransferUseCase.Command(userId, source.getId(), destination.getId(),
            amount, LocalDate.now(), "Transfer");

        assertThrows(AccessDeniedException.class, () -> service.execute(command));
        verify(transactions, never()).saveAll(any());
    }

    @Test
    void transactionUpdateRejectsAnotherUsersCategory() {
        TransactionRepositoryPort transactions = mock(TransactionRepositoryPort.class);
        CategoryRepositoryPort categories = mock(CategoryRepositoryPort.class);
        Transaction transaction = new Transaction(UUID.randomUUID(), userId, UUID.randomUUID(),
            TransactionType.EXPENSE, amount, LocalDate.now(), "Expense", UUID.randomUUID(),
            null, false, null);
        Category category = category(otherUserId, false);
        when(transactions.findById(transaction.getId())).thenReturn(Optional.of(transaction));
        when(categories.findById(category.getId())).thenReturn(Optional.of(category));

        AccountRepositoryPort accounts = mock(AccountRepositoryPort.class);
        when(accounts.findById(transaction.getAccountId())).thenReturn(Optional.of(
            new Account(transaction.getAccountId(), userId, "Account", AccountType.CHECKING, Money.zero("EUR"))));
        var service = new UpdateTransactionService(transactions, categories, accounts);
        var command = new UpdateTransactionUseCase.Command(userId, transaction.getId(), null, null,
            null, null, category.getId(), null, null);

        assertThrows(AccessDeniedException.class, () -> service.execute(command));
        verify(transactions, never()).save(any());
    }

    @Test
    void transactionDeletionDoesNotDeleteLinkedTransactionOwnedByAnotherUser() {
        TransactionRepositoryPort transactions = mock(TransactionRepositoryPort.class);
        UUID linkedId = UUID.randomUUID();
        Transaction transaction = transaction(userId, linkedId);
        Transaction linked = new Transaction(linkedId, otherUserId, UUID.randomUUID(),
            TransactionType.TRANSFER_IN, amount, LocalDate.now(), "Transfer", null,
            transaction.getId(), false, null);
        when(transactions.findById(transaction.getId())).thenReturn(Optional.of(transaction));
        when(transactions.findById(linkedId)).thenReturn(Optional.of(linked));

        var service = new DeleteTransactionService(transactions);
        var command = new DeleteTransactionUseCase.Command(userId, transaction.getId());

        assertThrows(AccessDeniedException.class, () -> service.execute(command));
        verify(transactions, never()).deleteById(any());
    }

    private RecordTransactionUseCase.Command recordCommand(UUID accountId, UUID categoryId) {
        return new RecordTransactionUseCase.Command(userId, accountId, TransactionType.EXPENSE,
            amount, LocalDate.now(), "Expense", categoryId, false, null);
    }

    private Account account(UUID owner) {
        return new Account(UUID.randomUUID(), owner, "Account", AccountType.CHECKING, Money.zero("EUR"));
    }

    private Category category(UUID owner, boolean system) {
        return new Category(UUID.randomUUID(), owner, "Category", CategoryType.EXPENSE, system);
    }

    private Budget budget(UUID owner) {
        return new Budget(UUID.randomUUID(), owner, "Budget",
            new DateRange(LocalDate.now(), LocalDate.now().plusDays(1)),
            BudgetStatus.ACTIVE, List.of());
    }

    private Transaction transaction(UUID owner, UUID linkedId) {
        return new Transaction(UUID.randomUUID(), owner, UUID.randomUUID(),
            TransactionType.TRANSFER_OUT, amount, LocalDate.now(), "Transfer", null,
            linkedId, false, null);
    }
}
