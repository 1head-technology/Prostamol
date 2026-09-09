package com.prostamol.Prostamol.application.usecase;

import com.prostamol.Prostamol.application.usecase.account.GetAccountBalanceService;
import com.prostamol.Prostamol.application.usecase.account.UpdateAccountService;
import com.prostamol.Prostamol.application.usecase.budget.GetBudgetSummaryService;
import com.prostamol.Prostamol.application.usecase.savings.AddSavingsContributionService;
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
import com.prostamol.Prostamol.domain.port.in.account.UpdateAccountUseCase;
import com.prostamol.Prostamol.domain.port.in.transaction.RecordTransactionUseCase;
import com.prostamol.Prostamol.domain.port.in.transaction.RecordTransferUseCase;
import com.prostamol.Prostamol.domain.port.in.transaction.UpdateTransactionUseCase;
import com.prostamol.Prostamol.domain.port.out.AccountRepositoryPort;
import com.prostamol.Prostamol.domain.port.out.BudgetRepositoryPort;
import com.prostamol.Prostamol.domain.port.out.CategoryRepositoryPort;
import com.prostamol.Prostamol.domain.port.out.SavingsGoalRepositoryPort;
import com.prostamol.Prostamol.domain.port.out.TransactionRepositoryPort;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MonetaryInvariantTests {

    private final UUID userId = UUID.randomUUID();

    @Test
    void moneyRequiresIsoCurrencyAndDatabaseSafePrecisionAndScale() {
        assertThrows(IllegalArgumentException.class, () -> Money.of(BigDecimal.ONE, "eur"));
        assertThrows(IllegalArgumentException.class, () -> Money.of(BigDecimal.ONE, "BTC"));
        assertThrows(IllegalArgumentException.class, () -> Money.of(new BigDecimal("1.00001"), "EUR"));
        assertThrows(IllegalArgumentException.class,
            () -> Money.of(new BigDecimal("1000000000000000.0000"), "EUR"));

        assertDoesNotThrow(() -> Money.of(new BigDecimal("999999999999999.9999"), "EUR"));
        assertDoesNotThrow(() -> Money.of(new BigDecimal("-1.25"), "EUR"));
    }

    @Test
    void aggregateModelsOwnTheirSignAndZeroRules() {
        assertThrows(IllegalArgumentException.class, () -> account("EUR", new BigDecimal("-0.01")));
        assertThrows(IllegalArgumentException.class, () -> transaction(
            UUID.randomUUID(), TransactionType.EXPENSE, Money.zero("EUR"), UUID.randomUUID()));
        assertThrows(IllegalArgumentException.class,
            () -> new BudgetLine(UUID.randomUUID(), UUID.randomUUID(), Money.zero("EUR")));
        assertThrows(IllegalArgumentException.class, () -> new SavingsGoal(
            UUID.randomUUID(), userId, "Goal", Money.zero("EUR"), Money.zero("EUR"),
            LocalDate.now().plusDays(1), SavingsGoalStatus.ACTIVE));
    }

    @Test
    void transactionCurrencyMustEqualItsAccountCurrency() {
        AccountRepositoryPort accounts = mock(AccountRepositoryPort.class);
        CategoryRepositoryPort categories = mock(CategoryRepositoryPort.class);
        TransactionRepositoryPort transactions = mock(TransactionRepositoryPort.class);
        Account account = account("EUR", BigDecimal.ZERO);
        when(accounts.findById(account.getId())).thenReturn(Optional.of(account));

        var service = new RecordTransactionService(transactions, accounts, categories);
        var command = new RecordTransactionUseCase.Command(userId, account.getId(), TransactionType.EXPENSE,
            Money.of(BigDecimal.ONE, "USD"), LocalDate.now(), "Expense", UUID.randomUUID(), false, null);

        assertThrows(IllegalArgumentException.class, () -> service.execute(command));
        verify(categories, never()).findById(any());
        verify(transactions, never()).save(any());
    }

    @Test
    void transactionUpdateCannotChangeCurrencyAwayFromAccountCurrency() {
        AccountRepositoryPort accounts = mock(AccountRepositoryPort.class);
        CategoryRepositoryPort categories = mock(CategoryRepositoryPort.class);
        TransactionRepositoryPort transactions = mock(TransactionRepositoryPort.class);
        Account account = account("EUR", BigDecimal.ZERO);
        Transaction transaction = transaction(account.getId(), TransactionType.EXPENSE,
            Money.of(BigDecimal.ONE, "EUR"), UUID.randomUUID());
        when(transactions.findById(transaction.getId())).thenReturn(Optional.of(transaction));
        when(accounts.findById(account.getId())).thenReturn(Optional.of(account));

        var service = new UpdateTransactionService(transactions, categories, accounts);
        var command = new UpdateTransactionUseCase.Command(userId, transaction.getId(), null, "USD",
            null, null, null, null, null);

        assertThrows(IllegalArgumentException.class, () -> service.execute(command));
        verify(transactions, never()).save(any());
    }

    @Test
    void transferRequiresDistinctAccounts() {
        AccountRepositoryPort accounts = mock(AccountRepositoryPort.class);
        TransactionRepositoryPort transactions = mock(TransactionRepositoryPort.class);
        UUID accountId = UUID.randomUUID();
        var command = new RecordTransferUseCase.Command(userId, accountId, accountId,
            Money.of(BigDecimal.ONE, "EUR"), LocalDate.now(), "Transfer");

        assertThrows(IllegalArgumentException.class,
            () -> new RecordTransferService(transactions, accounts).execute(command));
        verify(accounts, never()).findById(any());
    }

    @Test
    void crossCurrencyTransferIsRejected() {
        AccountRepositoryPort accounts = mock(AccountRepositoryPort.class);
        TransactionRepositoryPort transactions = mock(TransactionRepositoryPort.class);
        Account source = account("EUR", BigDecimal.ZERO);
        Account destination = account("USD", BigDecimal.ZERO);
        when(accounts.findById(source.getId())).thenReturn(Optional.of(source));
        when(accounts.findById(destination.getId())).thenReturn(Optional.of(destination));
        var command = new RecordTransferUseCase.Command(userId, source.getId(), destination.getId(),
            Money.of(BigDecimal.ONE, "EUR"), LocalDate.now(), "Transfer");

        assertThrows(IllegalArgumentException.class,
            () -> new RecordTransferService(transactions, accounts).execute(command));
        verify(transactions, never()).saveAll(any());
    }

    @Test
    void transferAmountMustMatchBothSameCurrencyAccounts() {
        AccountRepositoryPort accounts = mock(AccountRepositoryPort.class);
        TransactionRepositoryPort transactions = mock(TransactionRepositoryPort.class);
        Account source = account("EUR", BigDecimal.ZERO);
        Account destination = account("EUR", BigDecimal.ZERO);
        when(accounts.findById(source.getId())).thenReturn(Optional.of(source));
        when(accounts.findById(destination.getId())).thenReturn(Optional.of(destination));
        var command = new RecordTransferUseCase.Command(userId, source.getId(), destination.getId(),
            Money.of(BigDecimal.ONE, "USD"), LocalDate.now(), "Transfer");

        assertThrows(IllegalArgumentException.class,
            () -> new RecordTransferService(transactions, accounts).execute(command));
        verify(transactions, never()).saveAll(any());
    }

    @Test
    void budgetLinesUseOneCurrency() {
        Budget budget = budget(List.of(new BudgetLine(
            UUID.randomUUID(), UUID.randomUUID(), Money.of(BigDecimal.TEN, "EUR"))));

        assertThrows(IllegalArgumentException.class, () -> budget.addLine(new BudgetLine(
            UUID.randomUUID(), UUID.randomUUID(), Money.of(BigDecimal.TEN, "USD"))));
        assertEquals(1, budget.getLines().size());
    }

    @Test
    void budgetSummaryRefusesUnlikeCurrencyCategorySpending() {
        BudgetRepositoryPort budgets = mock(BudgetRepositoryPort.class);
        TransactionRepositoryPort transactions = mock(TransactionRepositoryPort.class);
        CategoryRepositoryPort categories = mock(CategoryRepositoryPort.class);
        UUID categoryId = UUID.randomUUID();
        Budget budget = budget(List.of(new BudgetLine(
            UUID.randomUUID(), categoryId, Money.of(new BigDecimal("100"), "EUR"))));
        Category category = new Category(categoryId, userId, "Food", CategoryType.EXPENSE, false);
        Transaction usdExpense = transaction(UUID.randomUUID(), TransactionType.EXPENSE,
            Money.of(BigDecimal.TEN, "USD"), categoryId);
        when(budgets.findById(budget.getId())).thenReturn(Optional.of(budget));
        when(categories.findById(categoryId)).thenReturn(Optional.of(category));
        when(transactions.findAllByUserIdAndCategoryIdAndDateBetween(any(), any(), any(), any()))
            .thenReturn(List.of(usdExpense));

        assertThrows(IllegalArgumentException.class,
            () -> new GetBudgetSummaryService(budgets, transactions, categories)
                .execute(userId, budget.getId()));
    }

    @Test
    void budgetSummaryKeepsTheBudgetCurrencyForZeroAndDerivedAmounts() {
        BudgetRepositoryPort budgets = mock(BudgetRepositoryPort.class);
        TransactionRepositoryPort transactions = mock(TransactionRepositoryPort.class);
        CategoryRepositoryPort categories = mock(CategoryRepositoryPort.class);
        UUID categoryId = UUID.randomUUID();
        Budget budget = budget(List.of(new BudgetLine(
            UUID.randomUUID(), categoryId, Money.of(new BigDecimal("100"), "EUR"))));
        Category category = new Category(categoryId, userId, "Food", CategoryType.EXPENSE, false);
        when(budgets.findById(budget.getId())).thenReturn(Optional.of(budget));
        when(categories.findById(categoryId)).thenReturn(Optional.of(category));
        when(transactions.findAllByUserIdAndCategoryIdAndDateBetween(any(), any(), any(), any()))
            .thenReturn(List.of());

        var summary = new GetBudgetSummaryService(budgets, transactions, categories)
            .execute(userId, budget.getId()).lines().getFirst();

        assertEquals("EUR", summary.planned().currency());
        assertEquals("EUR", summary.spent().currency());
        assertEquals("EUR", summary.remaining().currency());
    }

    @Test
    void contributionMustBePositiveAndMatchGoalCurrency() {
        SavingsGoalRepositoryPort goals = mock(SavingsGoalRepositoryPort.class);
        SavingsGoal goal = new SavingsGoal(UUID.randomUUID(), userId, "Goal",
            Money.of(new BigDecimal("100"), "EUR"), Money.zero("EUR"),
            LocalDate.now().plusDays(1), SavingsGoalStatus.ACTIVE);
        when(goals.findById(goal.getId())).thenReturn(Optional.of(goal));
        var service = new AddSavingsContributionService(goals);

        assertThrows(IllegalArgumentException.class,
            () -> service.execute(userId, goal.getId(), Money.of(BigDecimal.ONE, "USD")));
        assertThrows(IllegalArgumentException.class,
            () -> service.execute(userId, goal.getId(), Money.zero("EUR")));
        assertEquals(BigDecimal.ZERO, goal.getCurrentAmount().amount());
        verify(goals, never()).save(any());
    }

    @Test
    void accountBalanceRefusesLegacyMismatchedTransactions() {
        AccountRepositoryPort accounts = mock(AccountRepositoryPort.class);
        TransactionRepositoryPort transactions = mock(TransactionRepositoryPort.class);
        Account account = account("EUR", BigDecimal.ZERO);
        Transaction legacy = transaction(account.getId(), TransactionType.INCOME,
            Money.of(BigDecimal.ONE, "USD"), UUID.randomUUID());
        when(accounts.findById(account.getId())).thenReturn(Optional.of(account));
        when(transactions.findAllByAccountId(account.getId())).thenReturn(List.of(legacy));

        assertThrows(IllegalArgumentException.class,
            () -> new GetAccountBalanceService(accounts, transactions).execute(userId, account.getId()));
    }

    @Test
    void accountCurrencyCannotChangeWhileTransactionsExist() {
        AccountRepositoryPort accounts = mock(AccountRepositoryPort.class);
        TransactionRepositoryPort transactions = mock(TransactionRepositoryPort.class);
        Account account = account("EUR", BigDecimal.ZERO);
        when(accounts.findById(account.getId())).thenReturn(Optional.of(account));
        when(transactions.findAllByAccountId(account.getId())).thenReturn(List.of(
            transaction(account.getId(), TransactionType.INCOME, Money.of(BigDecimal.ONE, "EUR"), UUID.randomUUID())));
        var command = new UpdateAccountUseCase.Command(
            userId, account.getId(), null, null, null, "USD");

        assertThrows(IllegalArgumentException.class,
            () -> new UpdateAccountService(accounts, transactions).execute(command));
        verify(accounts, never()).save(any());
    }

    private Account account(String currency, BigDecimal initialBalance) {
        return new Account(UUID.randomUUID(), userId, "Account", AccountType.CHECKING,
            Money.of(initialBalance, currency));
    }

    private Transaction transaction(UUID accountId, TransactionType type, Money amount, UUID categoryId) {
        return new Transaction(UUID.randomUUID(), userId, accountId, type, amount, LocalDate.now(),
            "Transaction", categoryId, null, false, null);
    }

    private Budget budget(List<BudgetLine> lines) {
        return new Budget(UUID.randomUUID(), userId, "Budget",
            new DateRange(LocalDate.now(), LocalDate.now().plusDays(30)), BudgetStatus.ACTIVE, lines);
    }
}
