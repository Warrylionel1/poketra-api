package com.poketra.repository;

import com.poketra.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findByUserIdAndArchivedFalseOrderByNameAsc(UUID userId);

    Optional<Account> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndNameIgnoreCaseAndArchivedFalse(UUID userId, String name);
}