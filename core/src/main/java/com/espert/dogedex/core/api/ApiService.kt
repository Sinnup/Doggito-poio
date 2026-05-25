package com.espert.dogedex.core.api


import com.espert.dogedex.core.api.dto.AddDogTOUserDTO
import com.espert.dogedex.core.api.responses.DefaultResponse
import com.espert.dogedex.core.api.responses.DogApiResponse
import com.espert.dogedex.core.api.responses.DogListApiResponse
import com.espert.dogedex.core.ADD_DOG_TO_USER_URL
import com.espert.dogedex.core.BASE_URL
import com.espert.dogedex.core.GET_ALL_DOGS_URL
import com.espert.dogedex.core.GET_DOG_BY_ML_ID
import com.espert.dogedex.core.GET_USER_DOGS_URL
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

private val okHttpClient = OkHttpClient
    .Builder()
    .addInterceptor(ApiServiceInterceptor)
    .build()

private val retrofit = Retrofit.Builder()
    .client(okHttpClient)
    .baseUrl(BASE_URL)
    .addConverterFactory(MoshiConverterFactory.create())
    .build()

interface ApiService {
    @GET(GET_ALL_DOGS_URL)
    suspend fun getAllDogs(): DogListApiResponse

    @Headers("${ApiServiceInterceptor.NEEDS_AUTH_HEADER_KEY}: true")
    @POST(ADD_DOG_TO_USER_URL)
    suspend fun addDogToUser(@Body addDogTOUserDTO: AddDogTOUserDTO): DefaultResponse

    @Headers("${ApiServiceInterceptor.NEEDS_AUTH_HEADER_KEY}: true")
    @GET(GET_USER_DOGS_URL)
    suspend fun getUserDogs(): DogListApiResponse

    @GET(GET_DOG_BY_ML_ID)
    suspend fun getDogByMlId(@Query("ml_id") mlId: String): DogApiResponse
}

object DogsApi {
    val retrofitService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
