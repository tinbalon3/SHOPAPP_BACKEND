package com.project.shopapp.repositories;

import com.project.shopapp.models.Token;
import com.project.shopapp.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
@Repository
public interface TokenRepository extends JpaRepository<Token,Long> {
    List<Token> findByUser(User user);

    Token findByToken(String token);
    Optional<Token> findByRefreshToken(String refreshToken);

    void deleteByExpirationDateBefore(LocalDateTime date);
    @Query("SELECT u.refreshExpirationDate FROM Token u WHERE u.refreshToken = :token")
    Optional<LocalDateTime> findRefreshExpirationDateByToken(String token);
}
