package com.espert.dogedex.core.navigation

import android.net.Uri
import android.os.Bundle
import androidx.core.os.BundleCompat
import androidx.navigation.NavType
import com.espert.dogedex.core.model.Dog
import kotlinx.serialization.json.Json

val DogType = object : NavType<Dog>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): Dog? {
        return BundleCompat.getParcelable(bundle, key, Dog::class.java)
    }

    override fun parseValue(value: String): Dog {
        return Json.decodeFromString<Dog>(Uri.decode(value))
    }

    override fun put(bundle: Bundle, key: String, value: Dog) {
        bundle.putParcelable(key, value)
    }

    override fun serializeAsValue(value: Dog): String {
        return Uri.encode(Json.encodeToString(value))
    }
}
