package com.overdevx.leafapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.overdevx.leafapp.databinding.ActivityMainBinding
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var interpreter: Interpreter
    private lateinit var labels: List<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        interpreter = Interpreter(loadModelFile())

        // Load labels
        labels = loadLabels()

        binding.btnPilih.setOnClickListener {
            if (binding.btnPilih.text == "Pilih Gambar") {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"

                startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
            } else {
                classifyImage()
            }

        }


    }

    private fun loadModelFile(): MappedByteBuffer {
        val filedescriptor = assets.openFd("model.tflite")
        val inputStream = FileInputStream(filedescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = filedescriptor.startOffset
        val declaredLength = filedescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

    }

    private fun loadLabels(): List<String> {
        val labels = mutableListOf<String>()
        try {
            val reader = assets.open("labels.txt").bufferedReader()
            reader.useLines { lines ->
                lines.forEach {
                    labels.add(it)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return labels
    }

    private fun classifyImage() {
        // Get bitmap
        val bitmap = (binding.ivImg.drawable).toBitmap()

        // Preprocess bitmap
        val processedBitmap = preprocessImage(bitmap)

        // Perform inference using the TFLite model
        val (result, confidenceArray) = performInference(processedBitmap)


        // Find index of the predicted class
        val predictedClassIndex = labels.indexOf(result)

        // Display result and confidence for the predicted class
        if (predictedClassIndex != -1) {
            val confidence = confidenceArray[predictedClassIndex]

            binding.tvHasilprediksi.text = "$result"

            //Check confidence
            if (confidence <= 0.7f) {
                binding.tvHasilConfidencec.text = "Foto Tidak Valid"
            } else {
                binding.tvHasilConfidencec.text = "$confidence"
            }

            //Check result
            when (result) {
                "Sehat" -> binding.tvHasilprediksi.setTextColor(Color.GREEN)
                "Hawar" -> binding.tvHasilprediksi.setTextColor(Color.YELLOW)
                "Powder" -> binding.tvHasilprediksi.setTextColor(Color.GRAY)
                else -> binding.tvHasilprediksi.setTextColor(Color.BLACK)
            }

            binding.btnPilih.text = "Pilih Gambar"

        } else {
            binding.tvHasilprediksi.text = "$result"
            binding.tvHasilConfidencec.text = "Tidak Diketahui"
            binding.btnPilih.text = "Pilih Gambar"
        }
    }

    private fun preprocessImage(bitmap: Bitmap): Bitmap {
        // Resize bitmap to match the input size of the model
        val resizedBitmap =
            Bitmap.createScaledBitmap(bitmap, inputImageWidth, inputImageHeight, true)
        return resizedBitmap.copy(Bitmap.Config.ARGB_8888, true)
    }

    private fun performInference(bitmap: Bitmap): Pair<String, FloatArray> {
        // Load input shape and type
        val inputShape = interpreter.getInputTensor(0).shape()
        val inputType = interpreter.getInputTensor(0).dataType()

        // Preprocess input image
        val input = convertBitmapToByteBuffer(bitmap)

        // Prepare output buffer
        val output = Array(1) { FloatArray(labels.size) }

        // Run inference
        interpreter.run(input, output)

        // Postprocess output: find  index with maximum probability
        val index = output[0].indices.maxByOrNull { output[0][it] } ?: -1

        // Return the corresponding label and confidence array
        val label = if (index != -1) {
            labels[index]
        } else {
            "Tidak Diketahui"
        }
        return Pair(label, output[0])
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer =
            ByteBuffer.allocateDirect(4 * inputImageWidth * inputImageHeight * PIXEL_SIZE)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(inputImageWidth * inputImageHeight)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until inputImageWidth) {
            for (j in 0 until inputImageHeight) {
                val value = intValues[pixel++]
                byteBuffer.putFloat(((value shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                byteBuffer.putFloat(((value shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                byteBuffer.putFloat(((value and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
            }
        }
        return byteBuffer
    }

    private fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
                val uri = data?.data
                val inputStream = contentResolver.openInputStream(uri!!)
                val selectedBitmap = BitmapFactory.decodeStream(inputStream)

                // Resize Bitmap
                val resizedBitmap = resizeBitmap(selectedBitmap, 1024, 1024)

                binding.ivImg.setImageBitmap(resizedBitmap)
                binding.btnPilih.text = "Prediksi"
            }

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        interpreter.close()
    }

    companion object {
        private const val REQUEST_CODE_PICK_IMAGE = 1
        private const val IMAGE_MEAN = 127.5f
        private const val IMAGE_STD = 127.5f
        private const val PIXEL_SIZE = 3
        private const val inputImageWidth = 150
        private const val inputImageHeight = 150
    }
}