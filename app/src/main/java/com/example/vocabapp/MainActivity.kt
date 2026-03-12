package com.example.vocabapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.vocabapp.data.FilterMode
import com.example.vocabapp.data.ItemSetMode
import com.example.vocabapp.data.PromptMode
import com.example.vocabapp.data.VocabItem
import com.example.vocabapp.ui.theme.VocabAppTheme
import com.example.vocabapp.ui.playAssetAudio
import com.example.vocabapp.ui.prepareAssetVideo
import com.example.vocabapp.viewmodel.VocabViewModel

class MainActivity : ComponentActivity() {
    private val vocabViewModel by viewModels<VocabViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VocabAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    VocabApp(vocabViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VocabApp(vm: VocabViewModel = viewModel()) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val itemAudioPlayer = remember { ExoPlayer.Builder(context).build() }
    val lessonMediaPlayer = remember { ExoPlayer.Builder(context).build() }
    
    var lessonMenuExpanded by remember { mutableStateOf(false) }
    var showLessonAudioPopup by remember { mutableStateOf(false) }
    var showLessonVideoPopup by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            itemAudioPlayer.release()
            lessonMediaPlayer.release()
        }
    }

    val lesson = vm.currentLesson
    val item = vm.currentItem

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Vocabulary Exercise") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Card: Lesson Selector and Media Controls
            Card {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Row 1: <- Lesson xx ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { vm.prevLesson() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Previous lesson")
                        }
                        
                        Box(contentAlignment = Alignment.Center) {
                            Button(onClick = { lessonMenuExpanded = true }) {
                                Text("Lesson ${lesson?.lesson ?: "-"}")
                            }
                            DropdownMenu(expanded = lessonMenuExpanded, onDismissRequest = { lessonMenuExpanded = false }) {
                                vm.lessons.forEach {
                                    DropdownMenuItem(
                                        text = { Text("Lesson ${it.lesson}") },
                                        onClick = {
                                            vm.selectLesson(it.lesson)
                                            lessonMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        IconButton(onClick = { vm.nextLesson() }) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Next lesson")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 2: Media icons on left, Index on right most
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    lesson?.media?.mainAudio?.let {
                                        prepareAssetVideo(context, lessonMediaPlayer, it)
                                        lessonMediaPlayer.playWhenReady = true
                                        showLessonAudioPopup = true
                                    }
                                },
                                enabled = !lesson?.media?.mainAudio.isNullOrBlank()
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Play lesson audio")
                            }
                            IconButton(
                                onClick = {
                                    lesson?.media?.mainVideo?.let {
                                        prepareAssetVideo(context, lessonMediaPlayer, it)
                                        lessonMediaPlayer.playWhenReady = true
                                        showLessonVideoPopup = true
                                    }
                                },
                                enabled = !lesson?.media?.mainVideo.isNullOrBlank()
                            ) {
                                Icon(Icons.Default.PlayCircle, contentDescription = "Play lesson video")
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            // Timer display
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = formatElapsedTime(vm.elapsedTime),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        Text(
                            text = "${vm.currentIndex + 1}/${vm.visibleItems.size.coerceAtLeast(1)}",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            // Exercise Card (Higher priority than controls)
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { vm.prevItem() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Prev")
                        }
                        Text(
                            "Exercise",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        IconButton(onClick = { vm.nextItem() }) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                        }
                    }

                    if (item == null) {
                        Text("No items available for this filter.", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    } else {
                        var revealedMode by remember(item, vm.promptMode) { mutableStateOf<PromptMode?>(null) }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Primary Prompt (The "Question")
                            ExercisePromptContent(vm.promptMode, item, context, itemAudioPlayer)

                            if (revealedMode != null) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 32.dp))
                                // Revealed content (The "Answer")
                                ExercisePromptContent(revealedMode!!, item, context, itemAudioPlayer)
                            }

                            // Reveal buttons in a single row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PromptMode.entries.forEach { mode ->
                                    FilterChip(
                                        selected = revealedMode == mode,
                                        onClick = { revealedMode = mode },
                                        enabled = vm.promptMode != mode,
                                        label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = { vm.toggleSaveCurrent() }) {
                                Icon(
                                    if (vm.isCurrentSaved()) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
            }

            // Choose Content / Settings Card (Moved below Exercise)
            Card {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Choose content", style = MaterialTheme.typography.titleMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = vm.itemSetMode == ItemSetMode.VOCABULARY,
                            onClick = { vm.updateItemSetMode(ItemSetMode.VOCABULARY) },
                            label = { Text("Vocabulary") }
                        )
                        FilterChip(
                            selected = vm.itemSetMode == ItemSetMode.PHRASES,
                            onClick = { vm.updateItemSetMode(ItemSetMode.PHRASES) },
                            label = { Text("Phrases") }
                        )
                    }

                    Text("Prompt mode", style = MaterialTheme.typography.titleMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PromptMode.entries.forEach { mode ->
                            FilterChip(
                                selected = vm.promptMode == mode,
                                onClick = { vm.updatePromptMode(mode) },
                                label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }

                    Text("Filter", style = MaterialTheme.typography.titleMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = vm.filterMode == FilterMode.ALL,
                            onClick = { vm.updateFilterMode(FilterMode.ALL) },
                            label = { Text("All") }
                        )
                        FilterChip(
                            selected = vm.filterMode == FilterMode.SAVED,
                            onClick = { vm.updateFilterMode(FilterMode.SAVED) },
                            label = { Text("Saved") }
                        )
                        AssistChip(
                            onClick = { vm.shuffle() },
                            label = { Text("Shuffle") },
                            leadingIcon = { Icon(Icons.Default.Shuffle, contentDescription = null) }
                        )
                        AssistChip(
                            onClick = { vm.resetSaved() },
                            label = { Text("Reset") }
                        )
                    }
                }
            }
        }
    }

    if (showLessonAudioPopup || showLessonVideoPopup) {
        val title = if (showLessonAudioPopup) "Lesson Audio" else "Lesson Video"
        Dialog(onDismissRequest = {
            showLessonAudioPopup = false
            showLessonVideoPopup = false
            lessonMediaPlayer.pause()
        }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = title, style = MaterialTheme.typography.titleLarge)
                        IconButton(onClick = {
                            showLessonAudioPopup = false
                            showLessonVideoPopup = false
                            lessonMediaPlayer.pause()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = lessonMediaPlayer
                                useController = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (showLessonVideoPopup) 200.dp else 100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                    )
                }
            }
        }
    }
}

@Composable
fun ExercisePromptContent(mode: PromptMode, item: VocabItem, context: android.content.Context, player: ExoPlayer) {
    when (mode) {
        PromptMode.CHARACTER -> {
            Text(item.chinese, style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
        }
        PromptMode.PINYIN -> {
            Text(item.pinyin, style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
        }
        PromptMode.ENGLISH -> {
            Text(item.english, style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
        }
        PromptMode.SOUND -> {
            IconButton(
                onClick = {
                    if (item.sound.isNotBlank()) {
                        playAssetAudio(context, player, item.sound)
                    }
                },
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = "Play item audio",
                    modifier = Modifier.fillMaxSize(),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun formatElapsedTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}
