package com.example.domain.qr

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.net.URI

object QrEngine {

    fun generateQrBitmap(
        content: String,
        width: Int = 512,
        height: Int = 512,
        foregroundHex: Int = Color.BLACK,
        backgroundHex: Int = Color.WHITE
    ): Bitmap {
        require(content.isNotBlank()) { "Content cannot be empty" }

        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 1
        )

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints)

        val matrixWidth = bitMatrix.width
        val matrixHeight = bitMatrix.height
        val pixels = IntArray(matrixWidth * matrixHeight)

        for (y in 0 until matrixHeight) {
            val offset = y * matrixWidth
            for (x in 0 until matrixWidth) {
                pixels[offset + x] = if (bitMatrix.get(x, y)) foregroundHex else backgroundHex
            }
        }

        val bitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, matrixWidth, 0, 0, matrixWidth, matrixHeight)
        return bitmap
    }

    fun buildWifiConfigString(ssid: String, password: String, securityType: String, isHidden: Boolean = false): String {
        val escapedSsid = escapeWifiString(ssid)
        val escapedPass = escapeWifiString(password)
        val sec = when (securityType.uppercase()) {
            "NONE", "NOPASS" -> "nopass"
            "WEP" -> "WEP"
            else -> "WPA"
        }
        val hiddenFlag = if (isHidden) "H:true;" else ""
        return "WIFI:S:$escapedSsid;T:$sec;P:$escapedPass;${hiddenFlag};"
    }

    private fun escapeWifiString(str: String): String {
        return str.replace("\\", "\\\\")
            .replace(";", "\\;")
            .replace(",", "\\,")
            .replace(":", "\\:")
            .replace("\"", "\\\"")
    }

    fun decodeQrFromBitmap(bitmap: Bitmap): String? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val reader = MultiFormatReader()
            val result = reader.decode(binaryBitmap)
            result.text
        } catch (e: Exception) {
            null
        }
    }

    fun isValidWebUrl(text: String): Boolean {
        return try {
            val uri = URI(text.trim())
            val scheme = uri.scheme?.lowercase()
            scheme == "http" || scheme == "https"
        } catch (e: Exception) {
            false
        }
    }
}
