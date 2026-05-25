package com.espert.dogedex.core.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AssetCopyHelper(private val context: Context) {

    suspend fun copyAssetsFolder(assetsFolderName: String) {
        withContext(Dispatchers.IO) {
            try {
                val assetManager = context.assets
                val files = assetManager.list(assetsFolderName) ?: return@withContext

                val destinationFolder = File(context.filesDir, assetsFolderName)
                if (!destinationFolder.exists()) {
                    destinationFolder.mkdirs()
                }

                for (file in files) {
                    val sourceFile = "$assetsFolderName/$file"
                    val destFile = File(destinationFolder, file)

                    // Check if it's a directory
                    val assetsList = assetManager.list(sourceFile)
                    if (assetsList != null && assetsList.isNotEmpty()) {
                        // It's a directory, recurse
                        copyAssetsFolderRecursive(sourceFile, destFile.absolutePath)
                    } else {
                        // It's a file, copy it
                        copyAssetFile(sourceFile, destFile)
                    }
                }
            } catch (e: Exception) {
                throw Exception("Error copying assets folder: ${e.message}", e)
            }
        }
    }

    private fun copyAssetsFolderRecursive(sourceFolder: String, destPath: String) {
        try {
            val assetManager = context.assets
            val files = assetManager.list(sourceFolder) ?: return

            val destFolder = File(destPath)
            if (!destFolder.exists()) {
                destFolder.mkdirs()
            }

            for (file in files) {
                val sourceFile = "$sourceFolder/$file"
                val destFile = File(destFolder, file)

                val assetsList = assetManager.list(sourceFile)
                if (assetsList != null && assetsList.isNotEmpty()) {
                    copyAssetsFolderRecursive(sourceFile, destFile.absolutePath)
                } else {
                    copyAssetFile(sourceFile, destFile)
                }
            }
        } catch (e: Exception) {
            throw Exception("Error copying assets recursively: ${e.message}", e)
        }
    }

    private fun copyAssetFile(assetPath: String, destFile: File) {
        val assetManager = context.assets
        assetManager.open(assetPath).use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    fun getLocalFolderPath(folderName: String): String {
        return File(context.filesDir, folderName).absolutePath
    }
}