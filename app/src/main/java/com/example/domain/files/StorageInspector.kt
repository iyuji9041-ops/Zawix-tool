package com.example.domain.files

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.provider.OpenableColumns
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DeviceStorageInfo(
    val totalBytes: Long,
    val freeBytes: Long,
    val usedBytes: Long,
    val usedPercentage: Int
)

data class SelectedFileInfo(
    val uri: Uri,
    val fileName: String,
    val extension: String,
    val mimeType: String,
    val sizeBytes: Long,
    val formattedSize: String,
    val lastModified: String
)

object StorageInspector {

    fun getDeviceStorageInfo(): DeviceStorageInfo {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val freeBytes = availableBlocks * blockSize
            val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0)
            val percent = if (totalBytes > 0) ((usedBytes * 100) / totalBytes).toInt() else 0

            DeviceStorageInfo(totalBytes, freeBytes, usedBytes, percent)
        } catch (e: Exception) {
            DeviceStorageInfo(0, 0, 0, 0)
        }
    }

    fun inspectUri(context: Context, uri: Uri): SelectedFileInfo {
        var name = "unknown_file"
        var size = 0L
        var lastModifiedMs: Long? = null

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            // Query real file modified timestamp provided by Storage Access Framework DocumentsContract
            val modifiedIndex = cursor.getColumnIndex(android.provider.DocumentsContract.Document.COLUMN_LAST_MODIFIED)
            if (cursor.moveToFirst()) {
                if (nameIndex != -1) name = cursor.getString(nameIndex) ?: name
                if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
                if (modifiedIndex != -1 && !cursor.isNull(modifiedIndex)) {
                    val mod = cursor.getLong(modifiedIndex)
                    if (mod > 0L) {
                        lastModifiedMs = mod
                    }
                }
            }
        }

        val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
        val ext = name.substringAfterLast('.', "")

        val lastModifiedStr = if (lastModifiedMs != null) {
            SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(lastModifiedMs))
        } else {
            "Not reported by provider"
        }

        return SelectedFileInfo(
            uri = uri,
            fileName = name,
            extension = ext,
            mimeType = mimeType,
            sizeBytes = size,
            formattedSize = formatBytes(size),
            lastModified = lastModifiedStr
        )
    }

    fun createViewIntent(uri: Uri, mimeType: String): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun createShareIntent(uri: Uri, mimeType: String): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format(Locale.US, "%.1f MB", mb)
        val gb = mb / 1024.0
        return String.format(Locale.US, "%.2f GB", gb)
    }
}
