package com.poketra.service;


import com.poketra.dto.response.AuthResponse;
import com.poketra.dto.response.UserResponse;
import com.poketra.exception.InvalidGoogleTokenException;
import com.poketra.model.Currency;
import com.poketra.model.User;
import com.poketra.repository.UserRepository;
import com.poketra.security.GoogleTokenVerifier;
import com.poketra.security.GoogleUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final GoogleTokenVerifier googleTokenVerifier;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse authenticateWithGoogle(String idToken) throws InvalidGoogleTokenException {
        GoogleUserInfo googleUser = googleTokenVerifier.verify(idToken);

        User user = userRepository.findByGoogleId(googleUser.googleId()).orElseGet(() -> createUser(googleUser));

        String token = jwtService.generateToken(user.getId(), user.getEmail());

        return new AuthResponse(token, toResponse(user));
    }

    private User createUser(GoogleUserInfo googleUser) {
        User user = User.builder().googleId(googleUser.googleId()).email(googleUser.email()).firstName(googleUser.firstName()).lastName(googleUser.lastName()).pictureUrl(googleUser.pictureUrl()).currency(Currency.MGA).createdAt(java.time.OffsetDateTime.now()).build();

        User saved = userRepository.save(user);
        log.info("New user registered: {}", saved.getId());
        return saved;
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getPictureUrl(), user.getCurrency());
    }
}