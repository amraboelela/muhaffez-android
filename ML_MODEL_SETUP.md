# ML Model Setup for Android

This document explains how to convert the PyTorch Quran Matcher model to TensorFlow Lite for use in the Android app.

## Prerequisites

Install the required Python packages:

```bash
pip install torch onnx tf2onnx tensorflow onnx-tf
```

## Step 1: Convert PyTorch Model to TensorFlow Lite

Navigate to the AI directory and run the conversion script:

```bash
cd ~/develop/swift/muhaffez/ai
python convert_to_tflite.py
```

This script will:
1. Load the trained PyTorch model (`quran_matcher_combined_6_to_10_words.pth`)
2. Export it to ONNX format
3. Convert ONNX to TensorFlow
4. Convert TensorFlow to TFLite with optimizations
5. Save the result as `aya_finder.tflite`

## Step 2: Copy Files to Android Assets

After successful conversion, copy the required files to Android assets:

```bash
# Copy the TFLite model
cp ~/develop/swift/muhaffez/ai/aya_finder.tflite ~/develop/android/muhaffez-android/app/src/main/assets/

# vocabulary.json is already copied, but verify it exists
ls -la ~/develop/android/muhaffez-android/app/src/main/assets/vocabulary.json
```

## Expected Files in Assets

Your `app/src/main/assets/` directory should contain:
- `quran-simple-min.txt` (Quran text)
- `vocabulary.json` (Character tokenization vocabulary)
- `aya_finder.tflite` (TensorFlow Lite model)

## Model Architecture

- **Input**: 60 tokens (character-level tokenization)
- **Hidden Size**: 512 nodes
- **Output**: 6203 classes (one for each Quran ayah)
- **Vocabulary**: 34 unique Arabic characters + special tokens

## How It Works

1. **Tokenization**: Input text is normalized and converted to character tokens using `vocabulary.json`
2. **Padding**: Input is padded/truncated to exactly 60 tokens
3. **Inference**: TFLite model predicts probability distribution over 6203 ayahs
4. **Validation**: Top 5 predictions are validated using similarity matching with the actual Quran text
5. **Result**: Best match with similarity ≥ 70% is accepted

## Integration Points

The ML model is integrated into the Android app through:

1. **AyaFinderMLModel.kt**: Wrapper class that handles TFLite inference
2. **MuhaffezViewModel.kt**: `tryMLModelMatch()` method that validates predictions
3. **MainActivity.kt**: Initializes ViewModel with context for ML model loading

## Performance Notes

- Model uses Float16 quantization for reduced size
- Android NNAPI acceleration is enabled when available
- Fallback to CPU if NNAPI is not supported
- Model size: ~11MB (with Float16 quantization)

## Troubleshooting

### Model not loading
- Verify `aya_finder.tflite` exists in `app/src/main/assets/`
- Check logcat for "AyaFinder model loaded successfully" message
- Ensure TensorFlow Lite dependencies are in `build.gradle.kts`

### Poor prediction accuracy
- Verify input text is properly normalized using `normalizedArabic()`
- Check that vocabulary.json matches the training vocabulary
- Ensure model file is not corrupted (should be ~11MB)

### Build errors
- Make sure TensorFlow Lite version is 2.14.0 or higher
- Verify `aaptOptions { noCompress("tflite") }` is in build.gradle.kts
- Clean and rebuild: `./gradlew clean build`

## Training New Models

To train a new model with updated parameters:

1. Edit `~/develop/swift/muhaffez/ai/train_combined_6_to_10_words.py`
2. Train: `python train_combined_6_to_10_words.py`
3. Convert using the steps above
4. Copy new model to Android assets
5. Test thoroughly before deployment

## References

- Original PyTorch training code: `~/develop/swift/muhaffez/ai/`
- Model architecture: `~/develop/swift/muhaffez/ai/model.py`
- iOS CoreML version: `~/develop/swift/muhaffez/Muhaffez/AyaFinder.mlpackage`
