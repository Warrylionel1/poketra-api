package com.poketra.controller;

import com.poketra.dto.request.CreateAccountRequest;
import com.poketra.dto.response.AccountResponse;
import com.poketra.exception.AccountNotFoundException;
import com.poketra.exception.DuplicateAccountException;
import com.poketra.exception.GlobalExceptionHandler;
import com.poketra.model.AccountType;
import com.poketra.model.Currency;
import com.poketra.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

class AccountControllerTest {

    private AccountService accountService;
    private MockMvc mockMvc;
    private UUID userId;
    private TestingAuthenticationToken authentication;

    @BeforeEach
    void setUp() {
        accountService = Mockito.mock(AccountService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AccountController(accountService)).setControllerAdvice(new GlobalExceptionHandler()).build();
        userId = UUID.randomUUID();
        authentication = new TestingAuthenticationToken(userId.toString(), null);
    }

    private AccountResponse sampleResponse() {
        return new AccountResponse(UUID.randomUUID(), "Mvola", AccountType.MOBILE_MONEY, Currency.MGA, new BigDecimal("50000.00"), false, OffsetDateTime.now());
    }

    @Test
    void createsAccountForTheAuthenticatedUser() throws Exception {
        Mockito.when(accountService.create(Mockito.eq(userId), Mockito.any(CreateAccountRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts").principal(authentication).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Mvola\",\"type\":\"MOBILE_MONEY\",\"currency\":\"MGA\",\"initialBalance\":50000}")).andExpect(MockMvcResultMatchers.status().isCreated()).andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Mvola")).andExpect(MockMvcResultMatchers.jsonPath("$.currency").value("MGA"));
    }

    @Test
    void returns400WhenNameIsBlank() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts").principal(authentication).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\",\"type\":\"CASH\",\"currency\":\"MGA\"}")).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void returns400ForUnknownCurrency() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts").principal(authentication).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Cash\",\"type\":\"CASH\",\"currency\":\"GBP\"}")).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void returns409WhenNameAlreadyExists() throws Exception {
        Mockito.when(accountService.create(Mockito.eq(userId), Mockito.any(CreateAccountRequest.class))).thenThrow(new DuplicateAccountException("An account named 'Mvola' already exists"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts").principal(authentication).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Mvola\",\"type\":\"MOBILE_MONEY\",\"currency\":\"MGA\"}")).andExpect(MockMvcResultMatchers.status().isConflict());
    }

    @Test
    void listsAccounts() throws Exception {
        Mockito.when(accountService.list(userId)).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/accounts").principal(authentication)).andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1));
    }

    @Test
    void returns404WhenAccountIsNotFound() throws Exception {
        UUID accountId = UUID.randomUUID();
        Mockito.when(accountService.get(userId, accountId)).thenThrow(new AccountNotFoundException("Account not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/accounts/" + accountId).principal(authentication)).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void archivesAccount() throws Exception {
        UUID accountId = UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/accounts/" + accountId).principal(authentication)).andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(accountService).archive(userId, accountId);
    }
}