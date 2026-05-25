package com.espert.dogedex.core.api.dto

import com.espert.dogedex.core.model.User


class UserDTOMapper {

    fun fromDogDTOToDogDomain(userDTO: UserDTO): User {
        return User(
            userDTO.id,
            userDTO.email,
            userDTO.authenticationToken
        )
    }
}