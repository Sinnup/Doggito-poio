package com.fruse.dogedex.core.database.dao

import com.fruse.dogedex.core.api.dto.DogDTO
import com.fruse.dogedex.core.database.entities.DogEntity
import com.fruse.dogedex.core.model.Dog


class DogEntityMapper {

    fun fromDogEntityToDogDomain(dogEntity: DogEntity): Dog {
        return Dog(
            dogEntity.id,
            dogEntity.index,
            dogEntity.name,
            dogEntity.type,
            dogEntity.heightFemale,
            dogEntity.heightMale,
            dogEntity.imageUrl,
            dogEntity.lifeExpectancy,
            dogEntity.temperament,
            dogEntity.weightFemale,
            dogEntity.weightMale
        )
    }

    fun fromDogEntityListTODogDomainList(dogEntityList: List<DogEntity>): List<Dog> {
        return dogEntityList.map { fromDogEntityToDogDomain(it) }
    }

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