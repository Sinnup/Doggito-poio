package com.espert.dogedex.core.database.dao

import com.espert.dogedex.core.database.entities.DogEntity
import com.espert.dogedex.core.model.Dog
import org.junit.Assert.assertEquals
import org.junit.Test

class DogEntityMapperTest {

    private val mapper = DogEntityMapper()

    @Test
    fun `fromDogEntityToDogDomain maps all fields correctly`() {
        val entity = DogEntity(
            id = 1, index = 1, name = "Dog", type = "Type",
            heightFemale = "10", heightMale = "11", imageUrl = "url",
            lifeExpectancy = "12", temperament = "Temp",
            weightFemale = "5", weightMale = "6", inCollection = true
        )

        val domain = mapper.fromDogEntityToDogDomain(entity)

        assertEquals(entity.id, domain.id)
        assertEquals(entity.index, domain.index)
        assertEquals(entity.name, domain.name)
        assertEquals(entity.type, domain.type)
        assertEquals(entity.heightFemale, domain.heightFemale)
        assertEquals(entity.heightMale, domain.heightMale)
        assertEquals(entity.imageUrl, domain.imageUrl)
        assertEquals(entity.lifeExpectancy, domain.lifeExpectancy)
        assertEquals(entity.temperament, domain.temperament)
        assertEquals(entity.weightFemale, domain.weightFemale)
        assertEquals(entity.weightMale, domain.weightMale)
        assertEquals(entity.inCollection, domain.inCollection)
    }

    @Test
    fun `fromDogDomainToDogEntity maps all fields correctly`() {
        val domain = Dog(
            id = 1, index = 1, name = "Dog", type = "Type",
            heightFemale = "10", heightMale = "11", imageUrl = "url",
            lifeExpectancy = "12", temperament = "Temp",
            weightFemale = "5", weightMale = "6", inCollection = true
        )

        val entity = mapper.fromDogDomainToDogEntity(domain)

        assertEquals(domain.id, entity.id)
        assertEquals(domain.index, entity.index)
        assertEquals(domain.name, entity.name)
        assertEquals(domain.type, entity.type)
        assertEquals(domain.heightFemale, entity.heightFemale)
        assertEquals(domain.heightMale, entity.heightMale)
        assertEquals(domain.imageUrl, entity.imageUrl)
        assertEquals(domain.lifeExpectancy, entity.lifeExpectancy)
        assertEquals(domain.temperament, entity.temperament)
        assertEquals(domain.weightFemale, entity.weightFemale)
        assertEquals(domain.weightMale, entity.weightMale)
        assertEquals(domain.inCollection, entity.inCollection)
    }
}
