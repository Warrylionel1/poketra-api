package com.poketra.controller;

import com.poketra.dto.request.CreateAccountRequest;
import com.poketra.dto.request.UpdateAccountRequest;
import com.poketra.dto.response.AccountResponse;
import com.poketra.exception.AccountNotFoundException;
import com.poketra.exception.DuplicateAccountException;
import com.poketra.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(Principal principal, @Valid @RequestBody CreateAccountRequest request) throws DuplicateAccountException {
        return accountService.create(currentUserId(principal), request);
    }

    @GetMapping
    public List<AccountResponse> list(Principal principal) {
        return accountService.list(currentUserId(principal));
    }

    @GetMapping("/{id}")
    public AccountResponse get(Principal principal, @PathVariable UUID id) throws AccountNotFoundException {
        return accountService.get(currentUserId(principal), id);
    }

    @PutMapping("/{id}")
    public AccountResponse update(Principal principal, @PathVariable UUID id, @Valid @RequestBody UpdateAccountRequest request) throws AccountNotFoundException, DuplicateAccountException {
        return accountService.update(currentUserId(principal), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archive(Principal principal, @PathVariable UUID id) throws AccountNotFoundException {
        accountService.archive(currentUserId(principal), id);
    }

    private UUID currentUserId(Principal principal) {
        return UUID.fromString(principal.getName());
    }
}