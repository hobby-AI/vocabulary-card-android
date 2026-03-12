package com.example.vocabapp.data

import kotlinx.serialization.Serializable

@Serializable
data class LessonsPayload(
    val generated: String? = null,
    val mediaIndex: MediaIndex? = null,
    val lessons: List<LessonData> = emptyList()
)

@Serializable
data class MediaIndex(
    val itemAudio: List<String> = emptyList(),
    val lessonAudio: List<String> = emptyList(),
    val lessonVideo: List<String> = emptyList()
)

@Serializable
data class LessonData(
    val lesson: Int,
    val vocabulary: List<VocabItem> = emptyList(),
    val phrases: List<VocabItem> = emptyList(),
    val media: LessonMedia = LessonMedia()
)

@Serializable
data class LessonMedia(
    val mainAudio: String? = null,
    val mainVideo: String? = null
)

@Serializable
data class VocabItem(
    val id: String = "",
    val chinese: String = "",
    val pinyin: String = "",
    val english: String = "",
    val sound: String = ""
)

enum class PromptMode { CHARACTER, PINYIN, ENGLISH, SOUND }
enum class ItemSetMode { VOCABULARY, PHRASES }
enum class FilterMode { ALL, SAVED }
