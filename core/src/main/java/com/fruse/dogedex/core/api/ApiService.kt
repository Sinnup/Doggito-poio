package com.fruse.dogedex.core.api


import com.fruse.dogedex.core.api.dto.AddDogTOUserDTO
import com.fruse.dogedex.core.api.dto.LoginDTO
import com.fruse.dogedex.core.api.dto.SignUpDTO
import com.fruse.dogedex.core.api.responses.AuthApiResponse
import com.fruse.dogedex.core.api.responses.DefaultResponse
import com.fruse.dogedex.core.api.responses.DogApiResponse
import com.fruse.dogedex.core.api.responses.DogListApiResponse
import com.fruse.dogedex.core.ADD_DOG_TO_USER_URL
import com.fruse.dogedex.core.BASE_URL
import com.fruse.dogedex.core.GET_ALL_DOGS_URL
import com.fruse.dogedex.core.GET_DOG_BY_ML_ID
import com.fruse.dogedex.core.GET_USER_DOGS_URL
import com.fruse.dogedex.core.SIGN_IN_URL
import com.fruse.dogedex.core.SIGN_UP_URL
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

    @POST(SIGN_UP_URL)
    suspend fun signUp(@Body signUpDTO: SignUpDTO): AuthApiResponse

    @POST(SIGN_IN_URL)
    suspend fun login(@Body loginDTO: LoginDTO): AuthApiResponse

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