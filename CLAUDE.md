# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Muhaffez is an Android Quran memorization app that uses Arabic speech recognition to help users memorize the Quran. It displays Quran pages in a two-page layout and highlights matched words as users recite. The app uses a TensorFlow Lite ML model for accurate ayah prediction.

## Key Commands

### Build and Run
```bash
./gradlew build
./gradlew assembleDebug
./gradlew installDebug
```

### Testing
```bash
# Run instrumented tests (Android tests)
./gradlew connectedAndroidTest

# Run specific test class
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=app.amr.muhaffez.MuhaffezViewModelTest
```

### Code Quality
```bash
# Lint check
./gradlew lint

# Clean build
./gradlew clean
```

### ML Model Setup
See [ML_MODEL_SETUP.md](ML_MODEL_SETUP.md) for instructions on converting the PyTorch model to TensorFlow Lite.

## Architecture

### Core Components

**MuhaffezViewModel** - Central state management
- Manages voice input with `textToPredict` for normalized matching
- Tracks Basmallah and A'ozo Bellah to automatically remove from voice text
- Uses fuzzy string matching (Levenshtein distance) with thresholds:
  - `matchThreshold = 0.7` for direct word matching
  - `simiMatchThreshold = 0.6` for similar word matching
  - `seekMatchThreshold = 0.95` for backward/forward search
- Implements debouncing (1s) for fallback matching
- Implements peek helper (3s delay) to show upcoming words during recording
- **Incremental word matching**: Continues from `previousVoiceWordsCount` instead of restarting
- **Incremental page rendering**: Uses `pageMatchedWordsIndex` and `pageCurrentLineIndex` to build pages incrementally
- **ML Model Integration**: Uses TensorFlow Lite model for ayah prediction with 70% similarity validation

**AyaFinderMLModel** - TensorFlow Lite ML model wrapper
- Loads `aya_finder.tflite` model from assets
- Character-level tokenization using `vocabulary.json` (60 tokens)
- Predicts top 5 most likely ayahs from 6203 classes
- Uses Android NNAPI for hardware acceleration when available
- Returns predictions with probability scores for validation

**QuranModel** (Singleton)
- Loads Quran text from `assets/quran-simple-min.txt`
- Tracks page markers, rub3 markers, and surah markers
- Must be initialized with context in MainActivity: `QuranModel.initialize(this)`
- Contains 114 surahs mapped to their starting page numbers
- Uses single `tempPage` for incremental page building

**ArabicSpeechRecognizer**
- Wraps Android SpeechRecognizer for Arabic (ar-SA)
- Auto-restarts listening after pauses (unless manually stopped)
- Uses partial results for real-time updates

**TwoPagesView** - Two-page Quran display
- Shows right (odd) and left (even) pages side-by-side
- Animates scroll between pages based on current ayah
- Each page displays: juz number, surah name, page number
- Text is RTL with matched words in black, unmatched in red

**PageModel** - Page state container
- Holds juzNumber, surahName, pageNumber, and AnnotatedString.Builder
- Provides `deepCopy()` method for creating copies
- Use `reset()` to clear all fields

### String Extensions (String.kt)

- `normalizedArabic()` - Removes diacritics, control chars, normalizes hamza variants
- `removeBasmallah()` - Removes Basmallah from text (full or partial)
- `removeA3ozoBellah()` - Removes A'ozo Bellah (5 words)
- `hasA3ozoBellah()` - Checks if text starts with A'ozo Bellah
- `similarity(String)` - Returns 0.0-1.0 based on Levenshtein distance
- `levenshteinDistance(String)` - Character-level edit distance

### Matching Algorithm

1. **Initial match**: Searches for ayahs starting with `textToPredict` (min 10 chars)
   - Also checks if text starts with normalized line (bidirectional prefix check)
   - Detects Basmallah at line 0 and sets flag
2. **ML Model Fallback**: If no exact match or text < 17 chars (delayed 1s):
   - Uses TensorFlow Lite model to predict top 5 most likely ayahs
   - Validates each prediction by comparing similarity with actual ayah text
   - Accepts prediction if similarity ≥ 70%
   - Falls back to traditional similarity search if ML model unavailable or predictions fail
3. **Traditional Fallback**: If ML model fails:
   - Compares prefixes of equal length for better accuracy
   - Searches all 6203 ayahs for best similarity match
4. **Word matching**: Incremental - continues from last matched position
   - Direct threshold match (≥0.7)
   - Similarity match (≥0.6) as fallback
   - Backward search (up to 10 words back, ≥0.95) - doesn't advance on match
   - Forward search (up to 17 words ahead, ≥0.95, only for words > 3 chars)
5. **Page updates**: Incremental rendering based on `pageMatchedWordsIndex`
   - Single `tempPage` switches between right/left
   - 🌼 marks end of ayah
   - ⭐ marks end of rub3
   - Surah name with underline at surah boundaries

## ML Model Details

- **Model**: TensorFlow Lite (Float16 quantized)
- **Input**: 60 character tokens
- **Output**: 6203 ayah probabilities
- **Size**: ~11MB
- **Accuracy**: Trained on 6-10 word prefixes with noise augmentation
- **Location**: `app/src/main/assets/aya_finder.tflite`
- **Vocabulary**: `app/src/main/assets/vocabulary.json`

## Code Style Notes

- Kotlin with Jetpack Compose for UI
- Compile/target SDK: 36, Min SDK: 24
- Use `if let handler {` not `if let handler = handler {`
- Testing: JUnit 5 for unit tests, AndroidJUnit for instrumented tests
- ViewModels use mutable state with `by mutableStateOf()`
- ML model initialized with context in MainActivity
