package com.espert.dogedex.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Dog(
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
) : Parcelable, Comparable<Dog> {
    override fun compareTo(other: Dog): Int {
        return if (this.index > other.index) {
            1
        } else if (this.index == other.index) {
            0
        } else {
            -1
        }
    }
}