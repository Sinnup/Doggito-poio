package com.espert.dogedex.auth.auth

import com.espert.dogedex.core.api.ApiService
import com.espert.dogedex.core.api.dto.LoginDTO
import com.espert.dogedex.core.api.dto.SignUpDTO
import com.espert.dogedex.core.api.dto.UserDTOMapper
import com.espert.dogedex.core.api.makeNetworkCall
import com.espert.dogedex.core.api.responses.ResponseStatus
import com.espert.dogedex.core.model.User
import javax.inject.Inject


interface AuthTasks {

    suspend fun login(
        email: String,
        password: String
    ): ResponseStatus<User>

    suspend fun signUp(
        email: String,
        password: String,
        passwordConfirmation: String
    ): ResponseStatus<User>
}

class AuthRepository @Inject constructor(
    private val apiService: ApiService
) : AuthTasks {

    override suspend fun login(
        email: String,
        password: String
    ): ResponseStatus<User> {
        return makeNetworkCall {
            val loginDTO = LoginDTO(email, password)
            val loginResponse = apiService.login(loginDTO)

            if (!loginResponse.isSuccess) {
                throw Exception(loginResponse.message)
            }

            val userDTO = loginResponse.data.user
            val userDTOMapper = UserDTOMapper()
            userDTOMapper.fromDogDTOToDogDomain(userDTO)
        }
    }

    override suspend fun signUp(
        email: String,
        password: String,
        passwordConfirmation: String
    ): ResponseStatus<User> {
        return makeNetworkCall {
            val signUpDTO = SignUpDTO(email, password, passwordConfirmation)
            val signUpResponse = apiService.signUp(signUpDTO)

            if (!signUpResponse.isSuccess) {
                throw Exception(signUpResponse.message)
            }

            val userDTO = signUpResponse.data.user
            val userDTOMapper = UserDTOMapper()
            userDTOMapper.fromDogDTOToDogDomain(userDTO)
        }
    }
}