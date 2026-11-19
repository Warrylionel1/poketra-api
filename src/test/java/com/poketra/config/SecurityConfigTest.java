package com.poketra.config;

import com.poketra.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    void rejectsProtectedRouteWithoutToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/me"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void rejectsProtectedRouteWithInvalidToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/me")
                        .header("Authorization", "Bearer pas-un-jwt"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void acceptsProtectedRouteWithValidToken() throws Exception {
        String token = jwtService.generateToken(UUID.randomUUID(), "warry@example.com");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void loginRouteIsPublic() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"\"}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void allowsFrontendOriginOnPreflight() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.options("/api/auth/google")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header()
                        .string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    void rejectsUnknownOriginOnPreflight() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.options("/api/auth/google")
                        .header("Origin", "http://site-malveillant.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
}