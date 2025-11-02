package com.example.bankcards.mapper;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "cardsCount", expression = "java(user.getCards() != null ? user.getCards().size() : 0)")
    UserDto toDTO(User user);

    List<UserDto> toDTOList(List<User> users);

    default Page<UserDto> toDTOPage(Page<User> userPage) {
        return userPage.map(this::toDTO);
    }
}
