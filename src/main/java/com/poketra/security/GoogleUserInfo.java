package com.poketra.security;

public record GoogleUserInfo(String googleId, String email, String firstName, String lastName, String pictureUrl) {
}
