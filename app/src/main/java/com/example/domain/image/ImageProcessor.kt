package com.example.domain.image

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

enum class OutputFormat(val extension: String, val mimeType: String, val compressFormat: Bitmap.CompressFormat) {
    JPEG("jpg", "image/jpeg", Bitmap.CompressFormat.JPEG),
    PNG("png", "image/png", Bitmap.CompressFormat.PNG),
    WEBP("webp", "image/webp", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSY else Bitmap.CompressFormat.WEBP)
}

data class ImageMetadata(
    val width: Int,
    val height: Int,
    val mimeType: String,
    val sizeBytes: Long
)

data class ProcessedImageResult(
    val outputBytes: ByteArray,
    val outputWidth: Int,
    val outputHeight: Int,
    val outputSizeBytes: Long,
    val format: OutputFormat
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ProcessedImageResult
        return outputSizeBytes == other.outputSizeBytes && outputWidth == other.outputWidth && outputHeight == other.outputHeight
    }

    override fun hashCode(): Int {
        var result = outputWidth
        result = 31 * result + outputHeight
        result = 31 * result + outputSizeBytes.hashCode()
        return result
    }
}

object ImageProcessor {

    suspend fun getImageMetadata(context: Context, uri: Uri): ImageMetadata = withContext(Dispatchers.IO) {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }

        val sizeBytes = try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                pfd.statSize
            } ?: 0L
        } catch (e: Exception) {
            0L
        }

        ImageMetadata(
            width = options.outWidth,
            height = options.outHeight,
            mimeType = options.outMimeType ?: "image/unknown",
            sizeBytes = sizeBytes
        )
    }

    suspend fun processImage(
        context: Context,
        uri: Uri,
        targetWidth: Int?,
        targetHeight: Int?,
        quality: Int,
        format: OutputFormat
    ): ProcessedImageResult = withContext(Dispatchers.IO) {
        val boundedQuality = quality.coerceIn(1, 100)

        // 1. Read EXIF orientation to correct camera rotation
        val rotationDegrees = try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
        } catch (e: Exception) {
            0f
        }

        // 2. Decode bounds
        val boundsOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOptions)
        }
        var origW = boundsOptions.outWidth
        var origH = boundsOptions.outHeight
        if (origW <= 0 || origH <= 0) {
            throw IllegalArgumentException("Could not read image dimensions or format unsupported")
        }

        // If rotated 90 or 270, swap dimensions for destination aspect calculations
        if (rotationDegrees == 90f || rotationDegrees == 270f) {
            val temp = origW
            origW = origH
            origH = temp
        }

        // Determine destination dimensions preserving aspect ratio
        var destW = origW
        var destH = origH
        if (targetWidth != null && targetHeight != null && targetWidth > 0 && targetHeight > 0) {
            val ratio = origW.toFloat() / origH.toFloat()
            if (targetWidth / ratio <= targetHeight) {
                destW = targetWidth
                destH = (targetWidth / ratio).toInt().coerceAtLeast(1)
            } else {
                destH = targetHeight
                destW = (targetHeight * ratio).toInt().coerceAtLeast(1)
            }
        } else if (targetWidth != null && targetWidth > 0) {
            val ratio = origW.toFloat() / origH.toFloat()
            destW = targetWidth
            destH = (targetWidth / ratio).toInt().coerceAtLeast(1)
        } else if (targetHeight != null && targetHeight > 0) {
            // (b) Height-only resize with aspect-ratio preservation
            val ratio = origW.toFloat() / origH.toFloat()
            destH = targetHeight
            destW = (targetHeight * ratio).toInt().coerceAtLeast(1)
        }

        // 3. Decode with sub-sampling to prevent OutOfMemory on high-resolution camera images
        var sampleSize = 1
        while (origW / (sampleSize * 2) >= destW && origH / (sampleSize * 2) >= destH) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val decodedBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: throw IllegalStateException("Failed to decode image")

        // Apply EXIF rotation if necessary
        val rotatedBitmap = if (rotationDegrees != 0f) {
            val matrix = Matrix().apply { postRotate(rotationDegrees) }
            Bitmap.createBitmap(decodedBitmap, 0, 0, decodedBitmap.width, decodedBitmap.height, matrix, true).also {
                if (it != decodedBitmap) decodedBitmap.recycle()
            }
        } else {
            decodedBitmap
        }

        // 4. Exact resize if dimensions differ
        val scaledBitmap = if (rotatedBitmap.width != destW || rotatedBitmap.height != destH) {
            Bitmap.createScaledBitmap(rotatedBitmap, destW, destH, true).also {
                if (it != rotatedBitmap) rotatedBitmap.recycle()
            }
        } else {
            rotatedBitmap
        }

        // 5. Fill background white when converting transparent images to JPEG (prevents black background)
        val finalBitmap = if (format == OutputFormat.JPEG && (scaledBitmap.hasAlpha() || scaledBitmap.isPremultiplied)) {
            val whiteCanvasBitmap = Bitmap.createBitmap(scaledBitmap.width, scaledBitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(whiteCanvasBitmap)
            canvas.drawColor(Color.WHITE)
            canvas.drawBitmap(scaledBitmap, 0f, 0f, null)
            scaledBitmap.recycle()
            whiteCanvasBitmap
        } else {
            scaledBitmap
        }

        // 6. Compress output
        val outputStream = ByteArrayOutputStream()
        finalBitmap.compress(format.compressFormat, boundedQuality, outputStream)
        val bytes = outputStream.toByteArray()
        val finalW = finalBitmap.width
        val finalH = finalBitmap.height
        finalBitmap.recycle()

        ProcessedImageResult(
            outputBytes = bytes,
            outputWidth = finalW,
            outputHeight = finalH,
            outputSizeBytes = bytes.size.toLong(),
            format = format
        )
    }

    suspend fun saveToGallery(
        context: Context,
        result: ProcessedImageResult,
        baseName: String = "zaswix_image_${System.currentTimeMillis()}"
    ): Uri = withContext(Dispatchers.IO) {
        val filename = "$baseName.${result.format.extension}"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ scoped MediaStore path
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, result.format.mimeType)
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ZaswixTools")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw IllegalStateException("Unable to create MediaStore entry")

            try {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(result.outputBytes)
                    os.flush()
                } ?: throw IllegalStateException("Unable to open output stream")

                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)
                uri
            } catch (e: Exception) {
                // Delete the inserted MediaStore entry if writing fails
                try {
                    context.contentResolver.delete(uri, null, null)
                } catch (_: Exception) {}
                throw e
            }
        } else {
            // Pre-Android 10 legacy path using Pictures directory and MediaScanner
            val picturesDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ZaswixTools")
            if (!picturesDir.exists()) {
                picturesDir.mkdirs()
            }
            val targetFile = File(picturesDir, filename)
            try {
                FileOutputStream(targetFile).use { fos ->
                    fos.write(result.outputBytes)
                    fos.flush()
                }
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(targetFile.absolutePath),
                    arrayOf(result.format.mimeType),
                    null
                )
                Uri.fromFile(targetFile)
            } catch (e: Exception) {
                if (targetFile.exists()) {
                    targetFile.delete()
                }
                throw e
            }
        }
    }
}
