package com.example.vocabapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabapp.data.FilterMode
import com.example.vocabapp.data.ItemSetMode
import com.example.vocabapp.data.LessonData
import com.example.vocabapp.data.LessonRepository
import com.example.vocabapp.data.PromptMode
import com.example.vocabapp.data.VocabItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class VocabViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = LessonRepository(application)
    private val payload = repo.loadLessons()
    
    private var savedIds by mutableStateOf(setOf<String>())

    var lessons by mutableStateOf(payload.lessons)
        private set

    var lessonIndex by mutableIntStateOf(0)
        private set

    var itemSetMode by mutableStateOf(ItemSetMode.VOCABULARY)
        private set

    var promptMode by mutableStateOf(PromptMode.CHARACTER)
        private set

    var filterMode by mutableStateOf(FilterMode.ALL)
        private set

    var currentIndex by mutableIntStateOf(0)
        private set

    var elapsedTime by mutableLongStateOf(0L)
        private set

    private var timerJob: Job? = null

    init {
        startTimer()
    }

    val currentLesson: LessonData?
        get() = lessons.getOrNull(lessonIndex)

    val visibleItems: List<VocabItem>
        get() {
            val base = when (itemSetMode) {
                ItemSetMode.VOCABULARY -> currentLesson?.vocabulary.orEmpty()
                ItemSetMode.PHRASES -> currentLesson?.phrases.orEmpty()
            }
            return when (filterMode) {
                FilterMode.ALL -> base
                FilterMode.SAVED -> base.filter { savedIds.contains(it.id.ifBlank { it.chinese + it.english }) }
            }
        }

    val currentItem: VocabItem?
        get() = visibleItems.getOrNull(currentIndex.coerceIn(0, (visibleItems.size - 1).coerceAtLeast(0)))

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                elapsedTime++
            }
        }
    }

    fun resetTimer() {
        elapsedTime = 0L
    }

    fun nextLesson() {
        if (lessons.isNotEmpty()) {
            lessonIndex = (lessonIndex + 1) % lessons.size
            currentIndex = 0
            resetTimer()
        }
    }

    fun prevLesson() {
        if (lessons.isNotEmpty()) {
            lessonIndex = if (lessonIndex == 0) lessons.lastIndex else lessonIndex - 1
            currentIndex = 0
            resetTimer()
        }
    }

    fun selectLesson(lesson: Int) {
        val idx = lessons.indexOfFirst { it.lesson == lesson }
        if (idx >= 0) {
            lessonIndex = idx
            currentIndex = 0
            resetTimer()
        }
    }

    fun updateItemSetMode(mode: ItemSetMode) {
        itemSetMode = mode
        currentIndex = 0
        resetTimer()
    }

    fun updatePromptMode(mode: PromptMode) {
        promptMode = mode
    }

    fun updateFilterMode(mode: FilterMode) {
        filterMode = mode
        currentIndex = 0
        resetTimer()
    }

    fun nextItem() {
        val size = visibleItems.size
        if (size > 0) {
            currentIndex = (currentIndex + 1) % size
            resetTimer()
        }
    }

    fun prevItem() {
        val size = visibleItems.size
        if (size > 0) {
            currentIndex = if (currentIndex == 0) size - 1 else currentIndex - 1
            resetTimer()
        }
    }

    fun toggleSaveCurrent() {
        val key = currentItemKey() ?: return
        savedIds = if (savedIds.contains(key)) {
            savedIds - key
        } else {
            savedIds + key
        }
        
        if (filterMode == FilterMode.SAVED) {
            val size = visibleItems.size
            if (size == 0) {
                currentIndex = 0
            } else if (currentIndex >= size) {
                currentIndex = size - 1
            }
        }
    }

    fun isCurrentSaved(): Boolean {
        val key = currentItemKey() ?: return false
        return savedIds.contains(key)
    }

    fun resetSaved() {
        savedIds = emptySet()
        filterMode = FilterMode.ALL
        currentIndex = 0
        resetTimer()
    }

    fun shuffle() {
        val items = visibleItems.toMutableList()
        if (items.size <= 1) return
        val shuffled = items.shuffled(Random(System.currentTimeMillis()))
        val lesson = currentLesson ?: return
        val replacement = lesson.copy(
            vocabulary = if (itemSetMode == ItemSetMode.VOCABULARY) shuffled else lesson.vocabulary,
            phrases = if (itemSetMode == ItemSetMode.PHRASES) shuffled else lesson.phrases
        )
        lessons = lessons.toMutableList().also { it[lessonIndex] = replacement }
        currentIndex = 0
        resetTimer()
    }

    private fun currentItemKey(): String? = currentItem?.let { it.id.ifBlank { it.chinese + it.english } }
}
