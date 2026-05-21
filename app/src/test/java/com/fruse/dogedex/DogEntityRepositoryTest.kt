package com.fruse.dogedex

import com.fruse.dogedex.core.api.ApiService
import com.fruse.dogedex.core.api.dto.AddDogTOUserDTO
import com.fruse.dogedex.core.api.dto.DogDTO
import com.fruse.dogedex.core.api.dto.LoginDTO
import com.fruse.dogedex.core.api.dto.SignUpDTO
import com.fruse.dogedex.api.responses.ApiResponseStatus
import com.fruse.dogedex.api.responses.AuthApiResponse
import com.fruse.dogedex.api.responses.DefaultResponse
import com.fruse.dogedex.api.responses.DogApiResponse
import com.fruse.dogedex.api.responses.DogListApiResponse
import com.fruse.dogedex.api.responses.DogListResponse
import com.fruse.dogedex.api.responses.DogResponse
import com.fruse.dogedex.dogList.DogRepository
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

            override suspend fun getDogBYMlId(mlId: String): DogApiResponse {
                TODO("Not yet implemented")
            }

        }

        val dogRepository = DogRepository(
            apiService = FakeApiService(),
            dispatcher = UnconfinedTestDispatcher()
        )


        val apiResponseStatus = dogRepository.getDogCollection()

        assert(apiResponseStatus is ApiResponseStatus.Success)
        val dogCollection = (apiResponseStatus as ApiResponseStatus.Success).data
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

            override suspend fun getDogBYMlId(mlId: String): DogApiResponse {
                TODO("Not yet implemented")
            }

        }

        val dogRepository = DogRepository(
            apiService = FakeApiService(),
            dispatcher = UnconfinedTestDispatcher()
        )


        val apiResponseStatus = dogRepository.getDogCollection()

        assert(apiResponseStatus is ApiResponseStatus.Error)

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

            override suspend fun getDogBYMlId(mlId: String): DogApiResponse {
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
        assert(apiResponseStatus is ApiResponseStatus.Success)
        assertEquals(expectedDogId, (apiResponseStatus as ApiResponseStatus.Success).data.id)

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

            override suspend fun getDogBYMlId(mlId: String): DogApiResponse {
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
        assert(apiResponseStatus is ApiResponseStatus.Error)

    }
}
