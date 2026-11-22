package com.poketra.service;

import com.poketra.dto.request.CreateAccountRequest;
import com.poketra.dto.request.UpdateAccountRequest;
import com.poketra.dto.response.AccountResponse;
import com.poketra.exception.AccountNotFoundException;
import com.poketra.exception.DuplicateAccountException;
import com.poketra.model.Account;
import com.poketra.repository.AccountRepository;
import com.poketra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional
    public AccountResponse create(UUID userId, CreateAccountRequest request) throws DuplicateAccountException {
        String name = request.name().trim();
        if (accountRepository.existsByUserIdAndNameIgnoreCaseAndArchivedFalse(userId, name)) {
            throw new DuplicateAccountException("An account named '" + name + "' already exists");
        }

        BigDecimal initialBalance = request.initialBalance() == null ? BigDecimal.ZERO : request.initialBalance();

        Account account = Account.builder().user(userRepository.getReferenceById(userId)).name(name).type(request.type()).currency(request.currency()).initialBalance(initialBalance).build();

        return toResponse(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> list(UUID userId) {
        return accountRepository.findByUserIdAndArchivedFalseOrderByNameAsc(userId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse get(UUID userId, UUID accountId) throws AccountNotFoundException {
        return toResponse(findActive(userId, accountId));
    }

    @Transactional
    public AccountResponse update(UUID userId, UUID accountId, UpdateAccountRequest request) throws AccountNotFoundException, DuplicateAccountException {
        Account account = findActive(userId, accountId);
        String name = request.name().trim();

        boolean nameChanged = !account.getName().equalsIgnoreCase(name);
        if (nameChanged && accountRepository.existsByUserIdAndNameIgnoreCaseAndArchivedFalse(userId, name)) {
            throw new DuplicateAccountException("An account named '" + name + "' already exists");
        }

        account.setName(name);
        account.setType(request.type());
        account.setInitialBalance(request.initialBalance());
        return toResponse(account);
    }

    @Transactional
    public void archive(UUID userId, UUID accountId) throws AccountNotFoundException {
        Account account = findActive(userId, accountId);
        account.setArchived(true);
    }

    private Account findActive(UUID userId, UUID accountId) throws AccountNotFoundException {
        return accountRepository.findByIdAndUserId(accountId, userId).filter(account -> !account.isArchived()).orElseThrow(() -> new AccountNotFoundException("Account not found"));
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(account.getId(), account.getName(), account.getType(), account.getCurrency(), account.getInitialBalance(), account.isArchived(), account.getCreatedAt());
    }
}