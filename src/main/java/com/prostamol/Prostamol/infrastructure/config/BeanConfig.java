package com.prostamol.Prostamol.infrastructure.config;

import com.prostamol.Prostamol.application.usecase.account.*;
import com.prostamol.Prostamol.application.usecase.auth.AdminLoginService;
import com.prostamol.Prostamol.application.usecase.auth.LoginService;
import com.prostamol.Prostamol.application.usecase.auth.SignupService;
import com.prostamol.Prostamol.application.usecase.budget.CreateBudgetService;
import com.prostamol.Prostamol.application.usecase.budget.GetBudgetSummaryService;
import com.prostamol.Prostamol.application.usecase.budget.GetBudgetsService;
import com.prostamol.Prostamol.application.usecase.category.CreateCategoryService;
import com.prostamol.Prostamol.application.usecase.category.GetCategoriesService;
import com.prostamol.Prostamol.application.usecase.savings.AddSavingsContributionService;
import com.prostamol.Prostamol.application.usecase.savings.CreateSavingsGoalService;
import com.prostamol.Prostamol.application.usecase.savings.GetSavingsGoalsService;
import com.prostamol.Prostamol.application.usecase.transaction.GetTransactionsService;
import com.prostamol.Prostamol.application.usecase.transaction.RecordTransactionService;
import com.prostamol.Prostamol.application.usecase.transaction.RecordTransferService;
import com.prostamol.Prostamol.application.usecase.user.CreateUserService;
import com.prostamol.Prostamol.application.usecase.user.GetUserService;
import com.prostamol.Prostamol.domain.port.in.account.*;
import com.prostamol.Prostamol.domain.port.in.auth.AdminLoginUseCase;
import com.prostamol.Prostamol.domain.port.in.auth.LoginUseCase;
import com.prostamol.Prostamol.domain.port.in.auth.SignupUseCase;
import com.prostamol.Prostamol.domain.port.in.budget.CreateBudgetUseCase;
import com.prostamol.Prostamol.domain.port.in.budget.GetBudgetSummaryUseCase;
import com.prostamol.Prostamol.domain.port.in.budget.GetBudgetsUseCase;
import com.prostamol.Prostamol.domain.port.in.category.CreateCategoryUseCase;
import com.prostamol.Prostamol.domain.port.in.category.GetCategoriesUseCase;
import com.prostamol.Prostamol.domain.port.in.savings.AddSavingsContributionUseCase;
import com.prostamol.Prostamol.domain.port.in.savings.CreateSavingsGoalUseCase;
import com.prostamol.Prostamol.domain.port.in.savings.GetSavingsGoalsUseCase;
import com.prostamol.Prostamol.domain.port.in.transaction.GetTransactionsUseCase;
import com.prostamol.Prostamol.domain.port.in.transaction.RecordTransactionUseCase;
import com.prostamol.Prostamol.domain.port.in.transaction.RecordTransferUseCase;
import com.prostamol.Prostamol.domain.port.in.user.CreateUserUseCase;
import com.prostamol.Prostamol.domain.port.in.user.GetUserUseCase;
import com.prostamol.Prostamol.domain.port.out.PasswordEncoderPort;
import com.prostamol.Prostamol.domain.port.out.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    // ── Auth ─────────────────────────────────────────────────────────────────────

    @Bean
    public SignupUseCase signupUseCase(CreateUserUseCase createUserUseCase) {
        return new SignupService(createUserUseCase);
    }

    @Bean
    public LoginUseCase loginUseCase(
        UserRepositoryPort userRepository,
        PasswordEncoderPort passwordEncoder
    ) {
        return new LoginService(userRepository, passwordEncoder);
    }

    @Bean
    public AdminLoginUseCase adminLoginUseCase(UserRepositoryPort userRepository) {
        return new AdminLoginService(userRepository);
    }

    // ── User ────────────────────────────────────────────────────────────────────

    @Bean
    public CreateUserUseCase createUserUseCase(
        UserRepositoryPort userRepository,
        PasswordEncoderPort passwordEncoder
    ) {
        return new CreateUserService(userRepository, passwordEncoder);
    }

    @Bean
    public GetUserUseCase getUserUseCase(UserRepositoryPort userRepository) {
        return new GetUserService(userRepository);
    }

    // ── Account ───────────────────────────────────────────────────────────────

    @Bean
    public CreateAccountUseCase createAccountUseCase(
        AccountRepositoryPort accountRepository,
        UserRepositoryPort userRepository
    ) {
        return new CreateAccountService(accountRepository, userRepository);
    }

    @Bean
    public GetAccountsUseCase getAccountsUseCase(AccountRepositoryPort accountRepository) {
        return new GetAccountsService(accountRepository);
    }

    @Bean
    public GetAccountUseCase getAccountUseCase(AccountRepositoryPort accountRepository) {
        return new GetAccountService(accountRepository);
    }

    @Bean
    public UpdateAccountUseCase updateAccountUseCase(AccountRepositoryPort accountRepository) {
        return new UpdateAccountService(accountRepository);
    }

    @Bean
    public GetAccountBalanceUseCase getAccountBalanceUseCase(
        AccountRepositoryPort accountRepository,
        TransactionRepositoryPort transactionRepository
    ) {
        return new GetAccountBalanceService(accountRepository, transactionRepository);
    }

    @Bean
    public DeleteAccountUseCase deleteAccountUseCase(AccountRepositoryPort accountRepository) {
        return new DeleteAccountService(accountRepository);
    }

    // ── Transaction ───────────────────────────────────────────────────────────

    @Bean
    public RecordTransactionUseCase recordTransactionUseCase(
        TransactionRepositoryPort transactionRepository,
        AccountRepositoryPort accountRepository,
        CategoryRepositoryPort categoryRepository
    ) {
        return new RecordTransactionService(transactionRepository, accountRepository, categoryRepository);
    }

    @Bean
    public RecordTransferUseCase recordTransferUseCase(
        TransactionRepositoryPort transactionRepository,
        AccountRepositoryPort accountRepository
    ) {
        return new RecordTransferService(transactionRepository, accountRepository);
    }

    @Bean
    public GetTransactionsUseCase getTransactionsUseCase(TransactionRepositoryPort transactionRepository) {
        return new GetTransactionsService(transactionRepository);
    }

    // ── Category ──────────────────────────────────────────────────────────────

    @Bean
    public CreateCategoryUseCase createCategoryUseCase(CategoryRepositoryPort categoryRepository) {
        return new CreateCategoryService(categoryRepository);
    }

    @Bean
    public GetCategoriesUseCase getCategoriesUseCase(CategoryRepositoryPort categoryRepository) {
        return new GetCategoriesService(categoryRepository);
    }

    // ── Budget ────────────────────────────────────────────────────────────────

    @Bean
    public CreateBudgetUseCase createBudgetUseCase(BudgetRepositoryPort budgetRepository) {
        return new CreateBudgetService(budgetRepository);
    }

    @Bean
    public GetBudgetsUseCase getBudgetsUseCase(BudgetRepositoryPort budgetRepository) {
        return new GetBudgetsService(budgetRepository);
    }

    @Bean
    public GetBudgetSummaryUseCase getBudgetSummaryUseCase(
        BudgetRepositoryPort budgetRepository,
        TransactionRepositoryPort transactionRepository,
        CategoryRepositoryPort categoryRepository
    ) {
        return new GetBudgetSummaryService(
            budgetRepository,
            transactionRepository,
            categoryRepository
        );
    }

    // ── Savings ───────────────────────────────────────────────────────────────

    @Bean
    public CreateSavingsGoalUseCase createSavingsGoalUseCase(SavingsGoalRepositoryPort savingsGoalRepository) {
        return new CreateSavingsGoalService(savingsGoalRepository);
    }

    @Bean
    public AddSavingsContributionUseCase addSavingsContributionUseCase(SavingsGoalRepositoryPort savingsGoalRepository) {
        return new AddSavingsContributionService(savingsGoalRepository);
    }

    @Bean
    public GetSavingsGoalsUseCase getSavingsGoalsUseCase(SavingsGoalRepositoryPort savingsGoalRepository) {
        return new GetSavingsGoalsService(savingsGoalRepository);
    }
}
