package com.example.bankcards.mapper;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.util.CardNumberMasker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = CardNumberMasker.class)
public interface CardMapper {

    @Mapping(target = "cardNumber", source = "cardNumber")
    @Mapping(target = "cardHolderName", source = "cardHolderName")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "expired", expression = "java(card.isExpired())")
    @Mapping(target = "active", expression = "java(card.isActive())")
    CardDto toDTO(BankCard card);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cardNumber", ignore = true)
    @Mapping(target = "cardHolderName", source = "cardHolderName")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BankCard toEntity(CardDto cardDTO);

    List<CardDto> toDTOList(List<BankCard> cards);

    default Page<CardDto> toDTOPage(Page<BankCard> cardPage) {
        return cardPage.map(this::toDTO);
    }
}
