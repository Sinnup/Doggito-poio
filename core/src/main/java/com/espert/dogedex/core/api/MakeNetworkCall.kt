package com.espert.dogedex.core.api

import com.espert.dogedex.core.api.responses.ResponseStatus
import com.espert.dogedex.core.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.UnknownHostException

private const val UNAUTHORIZED_ERROR_CODE = 401

suspend fun <T> makeNetworkCall(
    call: suspend () -> T
): ResponseStatus<T> {
    return withContext(Dispatchers.IO) {
        try {
            ResponseStatus.Success(call())
        } catch (e: UnknownHostException) {
            ResponseStatus.Error(R.string.unknown_host_error)
        } catch (e: HttpException) {
            val errorMessage = if (e.code() == UNAUTHORIZED_ERROR_CODE) {
                R.string.wrong_user_or_password
            } else {
                R.string.unknown_error
            }
            ResponseStatus.Error(errorMessage)
        } catch (e: Exception) {
            val errorMessage = when (e.message) {
                "sign_up_error" -> R.string.error_sign_up
                "sign_in_error" -> R.string.error_sign_in
                "user_already_exists" -> R.string.user_already_exists
                "error_adding_dog" -> R.string.error_adding_dog
                else -> R.string.unknown_error
            }
            ResponseStatus.Error(errorMessage)
        }
    }
}