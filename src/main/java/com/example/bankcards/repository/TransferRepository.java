package com.example.bankcards.repository;

import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.enums.TransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    @Query("SELECT t FROM Transfer t " +
            "WHERE t.fromCard.user.id = :userId OR t.toCard.user.id = :userId")
    Page<Transfer> findByUserId(
            @Param("userId") Long userId,
            Pageable pageable);

    @Query("SELECT t FROM Transfer t " +
            "WHERE t.fromCard.user.id = :userId")
    Page<Transfer> findOutgoingTransfersByUserId(
            @Param("userId") Long userId,
            Pageable pageable);

    @Query("SELECT t FROM Transfer t " +
            "WHERE t.toCard.user.id = :userId")
    Page<Transfer> findIncomingTransfersByUserId(
            @Param("userId") Long userId,
            Pageable pageable);

    Page<Transfer> findByStatus(TransferStatus status, Pageable pageable);

    @Query("SELECT t FROM Transfer t " +
            "WHERE t.transferDate BETWEEN :startDate AND :endDate")
    Page<Transfer> findByTransferDateBetween(
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate,
            Pageable pageable);

    @Query("SELECT SUM(t.amount) FROM Transfer t " +
            "WHERE t.fromCard.user.id = :userId " +
            "AND t.status = 'COMPLETED' " +
            "AND t.transferDate BETWEEN :startDate " +
            "AND :endDate")
    Long getTotalOutgoingAmountByUserIdAndPeriod(
            @Param("userId") Long userId,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate);

}
