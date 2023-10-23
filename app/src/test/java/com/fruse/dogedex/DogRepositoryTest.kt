package com.fruse.dogedex

import api.ApiService
import api.dto.AddDogTOUserDTO
import api.dto.DogDTO
import api.dto.LoginDTO
import api.dto.SignUpDTO
import api.responses.ApiResponseStatus
import api.responses.AuthApiResponse
import api.responses.DefaultResponse
import api.responses.DogApiResponse
import api.responses.DogListApiResponse
import api.responses.DogListResponse
import api.responses.DogResponse
import com.fruse.dogedex.dogList.DogRepository
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Test
import java.net.UnknownHostException

class DogRepositoryTest {

    @Test
    fun testGetDogCollectionSuccess(): Unit = runBlocking {

        class FakeApiService : api.ApiService {
            override suspend fun getAllDogs(): api.responses.DogListApiResponse {
                return api.responses.DogListApiResponse(
                    message = "",
                    isSuccess = true,
                    data = api.responses.DogListResponse(
                        dogs = listOf(
                            api.dto.DogDTO(
                                1, index = 1, "Warturtle", "", "", "",
                                "", "", "", "", ""
                            ),
                            api.dto.DogDTO(
                                19, index = 2, "Charmeleon", "", "", "",
                                "", "", "", "", ""
                            )
                        )
                    )
                )
            }

            override suspend fun signUp(signUpDTO: api.dto.SignUpDTO): api.responses.AuthApiResponse {
                TODO("Not yet implemented")
            }

            override suspend fun login(loginDTO: api.dto.LoginDTO): api.responses.AuthApiResponse {
                TODO("Not yet implemented")
            }

            override suspend fun addDogToUser(addDogTOUserDTO: api.dto.AddDogTOUserDTO): api.responses.DefaultResponse {
                TODO("Not yet implemented")
            }

            override suspend fun getUserDogs(): api.responses.DogListApiResponse {
                return api.responses.DogListApiResponse(
                    message = "",
                    isSuccess = true,
                    data = api.responses.DogListResponse(
                        dogs = listOf(
                            api.dto.DogDTO(
                                19, index = 2, "Charmeleon", "", "", "",
                                "", "", "", "", ""
                            )
                        )
                    )
                )
            }

            override suspend fun getDogBYMlId(mlId: String): api.responses.DogApiResponse {
                TODO("Not yet implemented")
            }

        }

        val dogRepository = DogRepository(
            apiService = FakeApiService(),
            dispatcher = TestCoroutineDispatcher()
        )


        val apiResponseStatus = dogRepository.getDogCollection()

        assert(apiResponseStatus is api.responses.ApiResponseStatus.Success)
        val dogCollection = (apiResponseStatus as api.responses.ApiResponseStatus.Success).data
        assertEquals(2, dogCollection.size)
        assertEquals("Charmeleon", dogCollection[1].name)
        assertEquals("", dogCollection[0].name)
    }

    @Test
    fun testGetAllDogError(): Unit = runBlocking {

        class FakeApiService : api.ApiService {
            override suspend fun getAllDogs(): api.responses.DogListApiResponse {
                throw UnknownHostException()
            }

            override suspend fun signUp(signUpDTO: api.dto.SignUpDTO): api.responses.AuthApiResponse {
                TODO("Not yet implemented")
            }

            override suspend fun login(loginDTO: api.dto.LoginDTO): api.responses.AuthApiResponse {
                TODO("Not yet implemented")
            }

            override suspend fun addDogToUser(addDogTOUserDTO: api.dto.AddDogTOUserDTO): api.responses.DefaultResponse {
                TODO("Not yet implemented")
            }

            override suspend fun getUserDogs(): api.responses.DogListApiResponse {
                return api.responses.DogListApiResponse(
                    message = "",
                    isSuccess = true,
                    data = api.responses.DogListResponse(
                        dogs = listOf(
                            api.dto.DogDTO(
                                19, index = 2, "Charmeleon", "", "", "",
                                "", "", "", "", ""
                            )
                        )
                    )
                )
            }

            override suspend fun getDogBYMlId(mlId: String): api.responses.DogApiResponse {
                TODO("Not yet implemented")
            }

        }

        val dogRepository = DogRepository(
            apiService = FakeApiService(),
            dispatcher = TestCoroutineDispatcher()
        )


        val apiResponseStatus = dogRepository.getDogCollection()

        assert(apiResponseStatus is api.responses.ApiResponseStatus.Error)
        assertEquals(
            R.string.unknown_host_error,
            (apiResponseStatus as api.responses.ApiResponseStatus.Error).messageId
        )

    }

    @Test
    fun getDogByMLSuccess() = runBlocking {
        val expectedDogId = 19L

        class FakeApiService : api.ApiService {
            override suspend fun getAllDogs(): api.responses.DogListApiResponse {
                TODO("Not yet implemented")
            }

            override suspend fun signUp(signUpDTO: api.dto.SignUpDTO): api.responses.AuthApiResponse {
                TODO("Not yet implemented")
            }

            override suspend fun login(loginDTO: api.dto.LoginDTO): api.responses.AuthApiResponse {
                TODO("Not yet implemented")
            }

            override suspend fun addDogToUser(addDogTOUserDTO: api.dto.AddDogTOUserDTO): api.responses.DefaultResponse {
                TODO("Not yet implemented")
            }

            override suspend fun getUserDogs(): api.responses.DogListApiResponse {
                TODO("Not yet implemented")
            }

            override suspend fun getDogBYMlId(mlId: String): api.responses.DogApiResponse {
                return api.responses.DogApiResponse(
                    message = "",
                    isSuccess = true,
                    data = api.responses.DogResponse(
                        api.dto.DogDTO(
                            expectedDogId, index = 2, "Charmeleon", "", "", "",
                            "", "", "", "", ""
                        )
                    )
                )
            }
        }

        val dogRepository =
            DogRepository(
                apiService = FakeApiService(),
                dispatcher = TestCoroutineDispatcher()
            )

        val apiResponseStatus = dogRepository.getDogBYMlId("ja")
        assert(apiResponseStatus is api.responses.ApiResponseStatus.Success)
        assertEquals(expectedDogId, (apiResponseStatus as api.responses.ApiResponseStatus.Success).data.id)

    }

    @Test
    fun getDogByMLError() = runBlocking {
        val expectedDogId = 19L

        class FakeApiService : api.ApiService {
            override suspend fun getAllDogs(): api.responses.DogListApiResponse {
                TODO("Not yet implemented")
            }

            override suspend fun signUp(signUpDTO: api.dto.SignUpDTO): api.responses.AuthApiResponse {
                TODO("Not yet implemented")
            }

            override suspend fun login(loginDTO: api.dto.LoginDTO): api.responses.AuthApiResponse {
                TODO("Not yet implemented")
            }

            override suspend fun addDogToUser(addDogTOUserDTO: api.dto.AddDogTOUserDTO): api.responses.DefaultResponse {
                TODO("Not yet implemented")
            }

            override suspend fun getUserDogs(): api.responses.DogListApiResponse {
                TODO("Not yet implemented")
            }

            override suspend fun getDogBYMlId(mlId: String): api.responses.DogApiResponse {
                return api.responses.DogApiResponse(
                    message = "Error getting dog by ml id",
                    isSuccess = false,
                    data = api.responses.DogResponse(
                        api.dto.DogDTO(
                            expectedDogId, index = 2, "Charmeleon", "", "", "",
                            "", "", "", "", ""
                        )
                    )
                )
            }
        }

        val dogRepository =
            DogRepository(
                apiService = FakeApiService(),
                dispatcher = TestCoroutineDispatcher()
            )

        val apiResponseStatus = dogRepository.getDogBYMlId("ja")
        assert(apiResponseStatus is api.responses.ApiResponseStatus.Error)
        assertEquals(
            R.string.unknown_error,
            (apiResponseStatus as api.responses.ApiResponseStatus.Error).messageId
        )

    }
}