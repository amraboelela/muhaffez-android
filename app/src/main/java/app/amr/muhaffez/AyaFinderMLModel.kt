package app.amr.muhaffez

import android.content.Context
import org.tensorflow.lite.Interpreter
import org.json.JSONObject
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.exp

/**
 * AyaFinderMLModel - TensorFlow Lite model wrapper for Quran ayah prediction
 *
 * Matches the iOS AyaFinderMLModel functionality using TensorFlow Lite.
 * The model predicts the most likely ayah index from normalized Arabic text input.
 */
class AyaFinderMLModel(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var vocabulary: Map<String, Int> = emptyMap()
    private val maxLength = 60

    init {
        loadModel()
        loadVocabulary()
    }

    /**
     * Load the TensorFlow Lite model from assets
     */
    private fun loadModel() {
        try {
            val modelBuffer = loadModelFile("aya_finder.tflite")
            val options = Interpreter.Options().apply {
                setNumThreads(4)
                setUseNNAPI(true)  // Use Android Neural Networks API if available
            }
            interpreter = Interpreter(modelBuffer, options)
            println("AyaFinder model loaded successfully")
        } catch (e: Exception) {
            println("Error loading AyaFinder model: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Load vocabulary from JSON file in assets
     */
    private fun loadVocabulary() {
        try {
            val jsonString = context.assets.open("vocabulary.json").bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            val charToToken = jsonObject.getJSONObject("char_to_token")

            vocabulary = buildMap {
                val keys = charToToken.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    put(key, charToToken.getInt(key))
                }
            }
            println("Vocabulary loaded: ${vocabulary.size} tokens")
        } catch (e: Exception) {
            println("Error loading vocabulary: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Load model file from assets
     */
    @Throws(IOException::class)
    private fun loadModelFile(filename: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Predict the ayah index from normalized Arabic text
     *
     * @param text Normalized Arabic text (should already be normalized)
     * @return Triple of (ayahIndex, probability, top5) or null if prediction fails
     */
    fun predict(text: String): Prediction? {
        val interpreter = this.interpreter ?: return null

        // Tokenize the input
        val tokens = tokenize(text)

        // Create input buffer (1 batch, 60 tokens)
        val inputBuffer = ByteBuffer.allocateDirect(1 * maxLength * 4).apply {
            order(ByteOrder.nativeOrder())
        }

        // Fill input buffer with tokens
        for (token in tokens) {
            inputBuffer.putInt(token)
        }
        inputBuffer.rewind()

        // Create output buffer (1 batch, 6203 classes)
        val outputBuffer = ByteBuffer.allocateDirect(1 * 6203 * 4).apply {
            order(ByteOrder.nativeOrder())
        }

        // Run inference
        try {
            interpreter.run(inputBuffer, outputBuffer)
        } catch (e: Exception) {
            println("Error running inference: ${e.message}")
            return null
        }

        outputBuffer.rewind()

        // Read logits and apply softmax
        val logits = FloatArray(6203)
        for (i in 0 until 6203) {
            logits[i] = outputBuffer.float
        }

        // Apply softmax for numerical stability
        val maxLogit = logits.maxOrNull() ?: 0f
        val expLogits = logits.map { exp((it - maxLogit).toDouble()).toFloat() }
        val sumExp = expLogits.sum()
        val probabilities = expLogits.map { it / sumExp }

        // Create array with indices and probabilities (1-indexed for ayah numbers)
        val indexedProbs = probabilities.mapIndexed { index, prob ->
            Pair(index + 1, prob.toDouble())
        }

        // Sort by probability descending
        val sorted = indexedProbs.sortedByDescending { it.second }

        val topPrediction = sorted[0]
        val top5 = sorted.take(5)

        return Prediction(
            ayahIndex = topPrediction.first,
            probability = topPrediction.second,
            top5 = top5
        )
    }

    /**
     * Tokenize text using vocabulary
     *
     * @param text Input text (should be normalized Arabic)
     * @return Array of token IDs (padded to maxLength)
     */
    private fun tokenize(text: String): IntArray {
        val padToken = vocabulary["<PAD>"] ?: 0
        val unkToken = vocabulary["<UNK>"] ?: 1

        val tokens = mutableListOf<Int>()

        // Take first 60 characters
        val prefix = text.take(maxLength)

        for (char in prefix) {
            val token = vocabulary[char.toString()] ?: unkToken
            tokens.add(token)
        }

        // Pad to 60
        while (tokens.size < maxLength) {
            tokens.add(padToken)
        }

        return tokens.take(maxLength).toIntArray()
    }

    /**
     * Clean up resources
     */
    fun close() {
        interpreter?.close()
        interpreter = null
    }

    /**
     * Prediction result containing ayah index, probability, and top 5 predictions
     */
    data class Prediction(
        val ayahIndex: Int,
        val probability: Double,
        val top5: List<Pair<Int, Double>>
    )
}
