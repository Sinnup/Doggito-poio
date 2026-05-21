package com.fruse.dogedex.core.api.responses

sealed class ResponseStatus<T> {
    class Success<T>(val data: T) : ResponseStatus<T>()
    class Loading<T> : ResponseStatus<T>()
    class Error<T>(val messageId: Int) : ResponseStatus<T>()
}