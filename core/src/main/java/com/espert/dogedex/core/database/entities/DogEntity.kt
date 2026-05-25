package com.espert.dogedex.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dog")
data class DogEntity(
    @PrimaryKey
    val id: Long,
    val index: Int,
    val name: String,
    val type: String,
    val heightFemale: String,
    val heightMale: String,
    val imageUrl: String,
    val lifeExpectancy: String,
    val temperament: String,
    val weightFemale: String,
    val weightMale: String,
    val inCollection: Boolean = false
)