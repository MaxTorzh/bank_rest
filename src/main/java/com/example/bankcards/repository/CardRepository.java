package com.example.bankcards.repository;

import com.example.bankcards.entity.BankCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<BankCard, Long> {

    Page<BankCard> findByUserId(Long userId, Pageable pageable);

    Optional<BankCard> findByIdAndUserId(Long id, Long userId);

    boolean existsByCardNumber(String cardNumber);

    @Query("SELECT c FROM BankCard c WHERE c.expirationDate < CURRENT_DATE OR c.status = 'EXPIRED'")
    List<BankCard> findExpiredCardsByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(c.balance) FROM BankCard c WHERE c.user.id = :userId AND c.status = 'ACTIVE'")
    Long getTotalBalanceByUserId(@Param("userId") Long userId);
}
