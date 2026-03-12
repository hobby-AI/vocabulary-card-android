# Vocab Offline Android App

This is a full Android Studio starter project for an offline vocabulary exercise app.

## Included
- Kotlin + Jetpack Compose UI
- Lesson selector with previous/next arrows
- Vocabulary / phrases toggle
- Prompt modes: Character, Pinyin, English, Sound
- Save / Saved / Reset / Shuffle controls
- Lesson audio playback
- Lesson video playback with Media3 PlayerView
- Local JSON loading from assets

## Asset layout
Put your exported JSON here:

app/src/main/assets/lessons/vocabulary-lessons.json

Put media files under paths referenced by the JSON, for example:
- app/src/main/assets/lessons/media/item_audio/...
- app/src/main/assets/lessons/media/lesson_audio/...
- app/src/main/assets/lessons/media/lesson_video/...

## Recommended flow
1. Run your Node exporter to create `vocabulary-lessons.json`.
2. Copy media into the matching assets folders.
3. Open this folder in Android Studio.
4. Sync Gradle and run.

## Notes
- This project does not include Gradle wrapper files.
- Item save state is in-memory only for now.
- Next upgrade: persist saved items with Room or DataStore.
