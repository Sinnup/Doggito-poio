package com.espert.dogedex.dogList

import android.content.Context
import com.espert.dogedex.core.utils.AssetCopyHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

interface ImageRepository {
    suspend fun copyImagesToLocalStorage(): String
    fun getLocalImagePath(imageName: String): String
}

class ImageRepositoryImpl @Inject constructor(
    private val assetCopyHelper: AssetCopyHelper,
    @ApplicationContext private val context: Context
) : ImageRepository {

    companion object {
        private const val IMAGES_FOLDER = "images"
    }

    override suspend fun copyImagesToLocalStorage(): String {
        assetCopyHelper.copyAssetsFolder(IMAGES_FOLDER)
        return assetCopyHelper.getLocalFolderPath(IMAGES_FOLDER)
    }

    override fun getLocalImagePath(imageName: String): String {
        return File(context.filesDir, "$IMAGES_FOLDER/$imageName").absolutePath
    }
}