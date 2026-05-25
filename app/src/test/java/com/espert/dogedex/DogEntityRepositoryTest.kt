package com.espert.dogedex

import com.espert.dogedex.core.api.ApiService
import com.espert.dogedex.core.api.dto.AddDogTOUserDTO
import com.espert.dogedex.core.api.dto.DogDTO
import com.espert.dogedex.core.api.dto.LoginDTO
import com.espert.dogedex.core.api.dto.SignUpDTO
import com.espert.dogedex.core.api.responses.ResponseStatus
import com.espert.dogedex.core.api.responses.AuthApiResponse
import com.espert.dogedex.core.api.responses.DefaultResponse
import com.espert.dogedex.core.api.responses.DogApiResponse
import com.espert.dogedex.core.api.responses.DogListApiResponse
import com.espert.dogedex.core.api.responses.DogListResponse
import com.espert.dogedex.core.api.responses.DogResponse
import com.espert.dogedex.dogList.DogRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)

class DogEntityRepositoryTest {

    @Test
    fun testGetDogCollectionSuccess(): Unit = runTest {

        class FakeApiService : ApiService {
            override suspend fun getAllDogs(): DogListApiResponse {
                return DogListApiResponse(
                    message = "",
                    isSuccess = true,
                    data = DogListResponse(
                        dogs = listOf(
                            DogDTO(
                                1, index = 1, "Warturtle", "", "", "",
                                "", "", "", "", ""
                            ),
                            DogDTO(
                                19, index = 2, "Charmeleon", "", "", "",
                                "", "", "", "", ""
                            )
                        )
                    )
                )
            }

            override suspend fun signUp(signUpDTO: SignUpDTO): AuthApiResponse {
                TODO("Not yet implemented")
            }

            override suspend fun login(loginDTO: LoginDTO): AuthApiResponse {
                TODO("Not yet implemented")
            }

            override suspend fun addDogToUser(addDogTOUserDTO: AddDogTOUserDTO): DefaultResponse {
                TODO("Not yet implemented")
            }

            override suspend fun getUserDogs(): DogListApiResponse {
                return DogListApiResponse(
                    message = "",
                    isSuccess = true,
                    data = DogListResponse(
                        dogs = listOf(
                            DogDTO(
                                19, index = 2, "Charmeleon", "", "", "",
                                "", "", "", "", ""
                            )
                        )
                    )
                )
            }

            override suspend fun getDogByMlId(mlId: String): DogApiResponse {
                TODO("Not yet implemented")
            }

        }

        val dogRepository = DogRepository(
            apiService = FakeApiService(),
            dispatcher = UnconfinedTestDispatcher()
        )


        val apiResponseStatus = dogRepository.getDogCollection()

        assert(apiResponseStatus is ResponseStatus.Success)
        val dogCollection = (apiResponseStatus as ResponseStatus.Success).data
        assertEquals(2, dogCollection.size)
        assertEquals("Charmeleon", dogCollection[1].name)
        assertEquals("", dogCollection[0].name)
    }

    @Test
    fun testGetAllDogError(): Unit = runTest {

        class FakeApiService : ApiService {
            override suspend fun getAllDogs(): DogListApiResponse {
                throw java.net.UnknownHostException()
            }

            override suspend fun signUp(signUpDTO: SignUpDTO): AuthApiResponse {
                TODO("Not yet implemented")
            }

            override suspend fun login(loginDTO: LoginDTO): AuthApiResponse {
                TODO("Not yet implemented")
            }

            override suspend fun addDogToUser(addDogTOUserDTO: AddDogTOUserDTO): DefaultResponse {
                TODO("Not yet implemented")
            }

            override suspend fun getUserDogs(): DogListApiResponse {
                return DogListApiResponse(
                    message = "",
                    isSuccess = true,
                    data = DogListResponse(
                        dogs = listOf(
                            DogDTO(
                                19, index = 2, "Charmeleon", "", "", "",
                                "", "", "", "", ""
                            )
                        )
                    )
                )
            }

            override suspend fun getDogByMlId(mlId: String): DogApiResponse {
                TODO("Not yet implemented")
            }

        }

        val dogRepository = DogRepository(
            apiService = FakeApiService(),
            dispatcher = UnconfinedTestDispatcher()
        )


        val apiResponseStatus = dogRepository.getDogCollection()

        assert(apiResponseStatus is ResponseStatus.Error)

    }

    @Test
    fun getDogByMLSuccess() = runTest {
        val expectedDogId = 19L

        class FakeApiService : ApiService {
            override suspend fun getAllDogs(): DogListApiResponse {
                TODO("Not yet implemented")
            }

            override suspend fun signUp(signUpDTO: SignUpDTO): AuthApiResponse {
                TODO("Not yet implemented")
            }

            override suspend fun login(loginDTO: LoginDTO): AuthApiResponse {
                TODO("Not yet implemented")
            }

            override suspend fun addDogToUser(addDogTOUserDTO: AddDogTOUserDTO): DefaultResponse {
                TODO("Not yet implemented")
            }

            override suspend fun getUserDogs(): DogListApiResponse {
                TODO("Not yet implemented")
            }

            override suspend fun getDogByMlId(mlId: String): DogApiResponse {
                return DogApiResponse(
                    message = "",
                    isSuccess = true,
                    data = DogResponse(
                        DogDTO(
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
                dispatcher = UnconfinedTestDispatcher()
            )

        val apiResponseStatus = dogRepository.getDogBYMlId("ja")
        assert(apiResponseStatus is ResponseStatus.Success)
        assertEquals(expectedDogId, (apiResponseStatus as ResponseStatus.Success).data.id)

    }

    @Test
    fun getDogByMLError() = runTest {
        val expectedDogId = 19L

        class FakeApiService : ApiService {
            override suspend fun getAllDogs(): DogListApiResponse {
                TODO("Not yet implemented")
            }

            override suspend fun signUp(signUpDTO: SignUpDTO): AuthApiResponse {
                TODO("Not yet implemented")
            }

            override suspend fun login(loginDTO: LoginDTO): AuthApiResponse {
                TODO("Not yet implemented")
            }

            override suspend fun addDogToUser(addDogTOUserDTO: AddDogTOUserDTO): DefaultResponse {
                TODO("Not yet implemented")
            }

            override suspend fun getUserDogs(): DogListApiResponse {
                TODO("Not yet implemented")
            }

            override suspend fun getDogByMlId(mlId: String): DogApiResponse {
                return DogApiResponse(
                    message = "Error getting dog by ml id",
                    isSuccess = false,
                    data = DogResponse(
                        DogDTO(
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
                dispatcher = UnconfinedTestDispatcher()
            )

        val apiResponseStatus = dogRepository.getDogBYMlId("ja")
        assert(apiResponseStatus is ResponseStatus.Error)

    }
}
