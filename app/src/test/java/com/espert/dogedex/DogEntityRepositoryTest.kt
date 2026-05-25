package com.espert.dogedex

import com.espert.dogedex.core.database.DogedexDatabase
import com.espert.dogedex.core.database.dao.DogDao
import com.espert.dogedex.core.database.dao.DogEntity
import com.espert.dogedex.core.model.Dog
import com.espert.dogedex.core.model.ResponseStatus
import com.espert.dogedex.dogList.DogRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify

@OptIn(ExperimentalCoroutinesApi::class)
class DogEntityRepositoryTest {

    private lateinit var dogRepository: DogRepository
    private val database: DogedexDatabase = mock(DogedexDatabase::class.java)
    private val dogDao: DogDao = mock(DogDao::class.java)
    private val testDispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        `when`(database.dogDao()).thenReturn(dogDao)
        dogRepository = DogRepository(testDispatcher, database)
    }

    @Test
    fun `getDogCollection returns success with dogs from database`() = runTest {
        val dogEntities = listOf(
            DogEntity(1, 1, "Dog 1", "Dog 1", "Type 1", "10", "P1", "10-12", "5", "10", "1", true)
        )
        `when`(dogDao.getAllDogs()).thenReturn(dogEntities)

        val result = dogRepository.getDogCollection()

        assertTrue(result is ResponseStatus.Success)
        assertEquals(1, (result as ResponseStatus.Success).data.size)
        assertEquals("Dog 1", result.data[0].name)
    }

    @Test
    fun `addDogToUser calls dogDao addDogToUser`() = runTest {
        val dogId = 1L
        val result = dogRepository.addDogToUser(dogId)

        verify(dogDao).addDogToUser(dogId)
        assertTrue(result is ResponseStatus.Success)
    }

    @Test
    fun `getDogByMlId returns success when dog exists in database`() = runTest {
        val mlId = "ml_1"
        val dogEntity = DogEntity(1, 1, "Dog 1", "Dog 1", "Type 1", "10", "P1", "10-12", "5", "10", "1", true)
        `when`(dogDao.getDogByMLId(mlId)).thenReturn(dogEntity)

        val result = dogRepository.getDogByMlId(mlId)

        assertTrue(result is ResponseStatus.Success)
        assertEquals("Dog 1", (result as ResponseStatus.Success).data.name)
    }

    @Test
    fun `getProbableDogs returns flow of success responses`() = runTest {
        val mlIds = listOf("ml_1", "ml_2")
        val dogEntity1 = DogEntity(1, 1, "Dog 1", "Dog 1", "Type 1", "10", "P1", "10-12", "5", "10", "1", true)
        val dogEntity2 = DogEntity(2, 2, "Dog 2", "Dog 2", "Type 2", "12", "P2", "12-14", "6", "12", "2", true)
        `when`(dogDao.getDogByMLId("ml_1")).thenReturn(dogEntity1)
        `when`(dogDao.getDogByMLId("ml_2")).thenReturn(dogEntity2)

        val results = dogRepository.getProbableDogs(mlIds).toList()

        assertEquals(2, results.size)
        assertTrue(results[0] is ResponseStatus.Success)
        assertTrue(results[1] is ResponseStatus.Success)
        assertEquals("Dog 1", (results[0] as ResponseStatus.Success).data.name)
        assertEquals("Dog 2", (results[1] as ResponseStatus.Success).data.name)
    }

    @Test
    fun `insertAllDogs calls dogDao insertDogs`() = runTest {
        val dogs = listOf(
            Dog(1, 1, "Dog 1", "Dog 1", "Type 1", "10", "P1", "10-12", "5", "10", "1", true)
        )
        dogRepository.insertAllDogs(dogs)

        verify(dogDao).insertDogs(org.mockito.kotlin.any())
    }
}
