package com.poketra.service;

import com.poketra.dto.request.CreateAccountRequest;
import com.poketra.dto.request.UpdateAccountRequest;
import com.poketra.dto.response.AccountResponse;
import com.poketra.exception.AccountNotFoundException;
import com.poketra.exception.DuplicateAccountException;
import com.poketra.model.Account;
import com.poketra.model.AccountType;
import com.poketra.model.Currency;
import com.poketra.model.User;
import com.poketra.repository.AccountRepository;
import com.poketra.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

class AccountServiceTest {

    private AccountRepository accountRepository;
    private AccountService accountService;
    private UUID userId;
    private User owner;

    @BeforeEach
    void setUp() {
        accountRepository = Mockito.mock(AccountRepository.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        accountService = new AccountService(accountRepository, userRepository);

        userId = UUID.randomUUID();
        owner = User.builder().id(userId).googleId("g-1").email("a@example.com").firstName("A").build();

        Mockito.when(userRepository.getReferenceById(userId)).thenReturn(owner);
        Mockito.when(accountRepository.save(Mockito.any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Account buildAccount(String name) {
        return Account.builder().id(UUID.randomUUID()).user(owner).name(name).type(AccountType.CASH).currency(Currency.MGA).build();
    }

    @Test
    void createsAccountWithTrimmedNameAndDefaultBalance() throws DuplicateAccountException {
        CreateAccountRequest request = new CreateAccountRequest("  Mvola  ", AccountType.MOBILE_MONEY, Currency.MGA, null);

        AccountResponse response = accountService.create(userId, request);

        Assertions.assertEquals("Mvola", response.name());
        Assertions.assertEquals(AccountType.MOBILE_MONEY, response.type());
        Assertions.assertEquals(Currency.MGA, response.currency());
        Assertions.assertEquals(BigDecimal.ZERO, response.initialBalance());
        Assertions.assertFalse(response.archived());
    }

    @Test
    void rejectsDuplicateNameOnCreate() {
        Mockito.when(accountRepository.existsByUserIdAndNameIgnoreCaseAndArchivedFalse(userId, "Mvola")).thenReturn(true);
        CreateAccountRequest request = new CreateAccountRequest("Mvola", AccountType.MOBILE_MONEY, Currency.MGA, null);

        Assertions.assertThrows(DuplicateAccountException.class, () -> accountService.create(userId, request));

        Mockito.verify(accountRepository, Mockito.never()).save(Mockito.any(Account.class));
    }

    @Test
    void doesNotFindAnotherUsersAccount() {
        UUID accountId = UUID.randomUUID();
        Mockito.when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.empty());

        Assertions.assertThrows(AccountNotFoundException.class, () -> accountService.get(userId, accountId));
    }

    @Test
    void treatsArchivedAccountAsNotFound() {
        Account archived = buildAccount("Old");
        archived.setArchived(true);
        Mockito.when(accountRepository.findByIdAndUserId(archived.getId(), userId)).thenReturn(Optional.of(archived));

        Assertions.assertThrows(AccountNotFoundException.class, () -> accountService.get(userId, archived.getId()));
    }

    @Test
    void updatesNameTypeAndBalanceButKeepsCurrency() throws Exception {
        Account account = buildAccount("Cash");
        Mockito.when(accountRepository.findByIdAndUserId(account.getId(), userId)).thenReturn(Optional.of(account));
        UpdateAccountRequest request = new UpdateAccountRequest("Espèces", AccountType.OTHER, new BigDecimal("1500.50"));

        AccountResponse response = accountService.update(userId, account.getId(), request);

        Assertions.assertEquals("Espèces", response.name());
        Assertions.assertEquals(AccountType.OTHER, response.type());
        Assertions.assertEquals(new BigDecimal("1500.50"), response.initialBalance());
        Assertions.assertEquals(Currency.MGA, response.currency());
    }

    @Test
    void rejectsRenamingToAnExistingName() {
        Account account = buildAccount("Cash");
        Mockito.when(accountRepository.findByIdAndUserId(account.getId(), userId)).thenReturn(Optional.of(account));
        Mockito.when(accountRepository.existsByUserIdAndNameIgnoreCaseAndArchivedFalse(userId, "Mvola")).thenReturn(true);
        UpdateAccountRequest request = new UpdateAccountRequest("Mvola", AccountType.CASH, BigDecimal.ZERO);

        Assertions.assertThrows(DuplicateAccountException.class, () -> accountService.update(userId, account.getId(), request));
    }

    @Test
    void archivesAccount() throws AccountNotFoundException {
        Account account = buildAccount("Cash");
        Mockito.when(accountRepository.findByIdAndUserId(account.getId(), userId)).thenReturn(Optional.of(account));

        accountService.archive(userId, account.getId());

        Assertions.assertTrue(account.isArchived());
    }
}