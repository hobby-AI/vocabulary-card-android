package com.example.vocabapp.ui

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import java.io.File

private fun copyAssetIfNeeded(context: Context, assetPath: String): File {
    // The JSON paths are relative to the 'lessons' folder in assets
    val actualAssetPath = if (assetPath.startsWith("lessons/")) assetPath else "lessons/$assetPath"
    Log.d("MediaPlayers", "Opening asset: $actualAssetPath")
    
    val safeName = actualAssetPath.replace('/', '_')
    val outFile = File(context.cacheDir, safeName)
    if (!outFile.exists()) {
        try {
            context.assets.open(actualAssetPath).use { input ->
                outFile.outputStream().use { output -> input.copyTo(output) }
            }
        } catch (e: Exception) {
            Log.e("MediaPlayers", "Error copying asset $actualAssetPath", e)
            throw e
        }
    }
    return outFile
}

fun playAssetAudio(context: Context, player: ExoPlayer, assetPath: String) {
    try {
        val file = copyAssetIfNeeded(context, assetPath)
        player.setMediaItem(MediaItem.fromUri(file.toURI().toString()))
        player.prepare()
        player.playWhenReady = true
    } catch (e: Exception) {
        Log.e("MediaPlayers", "Error playing audio: $assetPath", e)
    }
}

fun prepareAssetVideo(context: Context, player: ExoPlayer, assetPath: String) {
    try {
        val file = copyAssetIfNeeded(context, assetPath)
        player.setMediaItem(MediaItem.fromUri(file.toURI().toString()))
        player.prepare()
    } catch (e: Exception) {
        Log.e("MediaPlayers", "Error preparing video: $assetPath", e)
    }
}
