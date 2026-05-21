package com.fruse.dogedex.core.database.dao

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

    fun fromDogDomainListTODogEntityList(dogList: List<Dog>): List<DogEntity> {
        return dogList.map { fromDogDomainToDogEntity(it) }
    }

    fun fromDogDomainToDogEntity(dog: Dog): DogEntity {
        return DogEntity(
            dog.id,
            dog.index,
            dog.name,
            dog.type,
            dog.heightFemale,
            dog.heightMale,
            dog.imageUrl,
            dog.lifeExpectancy,
            dog.temperament,
            dog.weightFemale,
            dog.weightMale
        )
    }

    fun fromDogEntityListTODogDomainList(dogEntityList: List<DogEntity>): List<Dog> {
        return dogEntityList.map { fromDogEntityToDogDomain(it) }
    }
}