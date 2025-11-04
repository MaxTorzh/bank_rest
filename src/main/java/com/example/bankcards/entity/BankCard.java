package com.example.bankcards.entity;

import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.Currency;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "bank_cards")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number", nullable = false, unique = true, length = 255)
    private String cardNumber;

    @Column(name = "card_holder_name", nullable = false, length = 100)
    private String cardHolderName;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CardStatus status = CardStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    @Builder.Default
    private Currency currency = Currency.RUB;

    @Column(nullable = false)
    @Builder.Default
    private Long balance = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    public boolean isExpired() {
        return expirationDate.isBefore(LocalDate.now());
    }

    public boolean isActive() {
        return status == CardStatus.ACTIVE && !isExpired();
    }
}
