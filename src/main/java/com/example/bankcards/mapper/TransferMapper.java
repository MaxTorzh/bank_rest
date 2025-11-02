package com.example.bankcards.mapper;

import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.util.CardNumberMasker;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = CardNumberMasker.class)
public interface TransferMapper {

    @Mapping(target = "fromCardId", source = "fromCard.id")
    @Mapping(target = "toCardId", source = "toCard.id")
    @Mapping(target = "fromCardMaskedNumber", source = "fromCard.cardNumber")
    @Mapping(target = "toCardMaskedNumber", source = "toCard.cardNumber")
    TransferDto toDTO(Transfer transfer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fromCard", ignore = true)
    @Mapping(target = "toCard", ignore = true)
    @Mapping(target = "transferDate", ignore = true)
    Transfer toEntity(TransferDto transferDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fromCard", ignore = true)
    @Mapping(target = "toCard", ignore = true)
    @Mapping(target = "transferDate", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    Transfer toEntityFromRequest(TransferRequest request);

    List<TransferDto> toDTOList(List<Transfer> transfers);

    default Page<TransferDto> toDTOPage(Page<Transfer> transferPage) {
        return transferPage.map(this::toDTO);
    }
}
