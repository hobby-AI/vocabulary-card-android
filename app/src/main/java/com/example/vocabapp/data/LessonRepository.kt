package com.example.vocabapp.data

import android.content.Context
import kotlinx.serialization.json.Json

class LessonRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    fun loadLessons(): LessonsPayload {
        val raw = context.assets.open("lessons/vocabulary-lessons.json")
            .bufferedReader()
            .use { it.readText() }
        return json.decodeFromString(LessonsPayload.serializer(), raw)
    }
}
