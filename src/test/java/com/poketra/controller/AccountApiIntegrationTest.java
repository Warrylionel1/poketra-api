package com.poketra.controller;

import com.poketra.model.Account;
import com.poketra.model.AccountType;
import com.poketra.model.Currency;
import com.poketra.model.User;
import com.poketra.repository.AccountRepository;
import com.poketra.repository.UserRepository;
import com.poketra.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    private User saveUser(String googleId, String email) {
        return userRepository.saveAndFlush(
                User.builder().googleId(googleId).email(email).firstName("Test").build());
    }

    private String tokenFor(User user) {
        return "Bearer " + jwtService.generateToken(user.getId(), user.getEmail());
    }

    @Test
    void requiresAuthentication() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/accounts"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void createsThenListsAccountsOfTheAuthenticatedUser() throws Exception {
        User user = saveUser("g-1", "one@example.com");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts")
                        .header("Authorization", tokenFor(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Mvola\",\"type\":\"MOBILE_MONEY\",\"currency\":\"MGA\",\"initialBalance\":50000}"))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/accounts")
                        .header("Authorization", tokenFor(user)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("Mvola"));
    }

    @Test
    void hidesAccountsOfOtherUsers() throws Exception {
        User owner = saveUser("g-1", "one@example.com");
        User other = saveUser("g-2", "two@example.com");
        Account account = accountRepository.saveAndFlush(Account.builder()
                .user(owner)
                .name("Cash")
                .type(AccountType.CASH)
                .currency(Currency.MGA)
                .build());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/accounts/" + account.getId())
                        .header("Authorization", tokenFor(other)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/accounts")
                        .header("Authorization", tokenFor(other)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(0));
    }
}