package com.espert.dogedex.core.api.dto

import com.squareup.moshi.Json

data class AddDogTOUserDTO(@field:Json(name = "dog_id") val dogId: Long)