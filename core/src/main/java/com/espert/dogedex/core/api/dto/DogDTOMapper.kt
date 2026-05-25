package com.espert.dogedex.core.api.dto

import com.espert.dogedex.core.database.entities.DogEntity
import com.espert.dogedex.core.model.Dog


class DogDTOMapper {

    fun fromDogDTOToDogDomain(dogDTO: DogDTO): Dog {
        return Dog(
            dogDTO.id,
            dogDTO.index,
            dogDTO.name,
            dogDTO.type,
            dogDTO.heightFemale,
            dogDTO.heightMale,
            dogDTO.imageUrl,
            dogDTO.lifeExpectancy,
            dogDTO.temperament,
            dogDTO.weightFemale,
            dogDTO.weightMale
        )
    }

    fun fromDogDTOListTODogDomainList(dogDTOList: List<DogDTO>): List<Dog> {
        return dogDTOList.map { fromDogDTOToDogDomain(it) }
    }
}