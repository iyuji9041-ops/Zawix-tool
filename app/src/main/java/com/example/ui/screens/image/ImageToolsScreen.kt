package com.example.ui.screens.image

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.files.StorageInspector
import com.example.domain.image.ImageMetadata
import com.example.domain.image.ImageProcessor
import com.example.domain.image.OutputFormat
import com.example.domain.image.ProcessedImageResult
import com.example.ui.components.ZaswixTopAppBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageToolsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageMetadata by remember { mutableStateOf<ImageMetadata?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }

    var targetWidthInput by remember { mutableStateOf("") }
    var targetHeightInput by remember { mutableStateOf("") }
    var qualitySlider by remember { mutableFloatStateOf(80f) }
    var selectedFormat by remember { mutableStateOf(OutputFormat.JPEG) }

    var isProcessing by remember { mutableStateOf(false) }
    var processedResult by remember { mutableStateOf<ProcessedImageResult?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            processedResult = null
            isAnalyzing = true
            coroutineScope.launch {
                try {
                    val meta = ImageProcessor.getImageMetadata(context, uri)
                    imageMetadata = meta
                    targetWidthInput = meta.width.toString()
                    targetHeightInput = meta.height.toString()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error reading image: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isAnalyzing = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            ZaswixTopAppBar(
                title = "Image Tools",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Select Image Button
            OutlinedButton(
                onClick = {
                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pick_image_button")
            ) {
                Icon(Icons.Default.Image, contentDescription = "Pick Image")
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text(if (selectedImageUri == null) "Select Image from Device" else "Change Selected Image")
            }

            if (isAnalyzing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.padding(start = 12.dp))
                    Text("Inspecting image dimensions...")
                }
            }

            if (imageMetadata != null) {
                val meta = imageMetadata!!
                // Original Info Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ORIGINAL IMAGE DETAILS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Resolution: ${meta.width} × ${meta.height} px",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Original Size: ${StorageInspector.formatBytes(meta.sizeBytes)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Format: ${meta.mimeType}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Configuration Settings
                Text(
                    text = "COMPRESSION & RESIZE OPTIONS",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.primary
                )

                // Output Format Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutputFormat.entries.forEach { format ->
                        FilterChip(
                            selected = selectedFormat == format,
                            onClick = { selectedFormat = format },
                            label = { Text(format.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                // Quality Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Output Quality", style = MaterialTheme.typography.bodyMedium)
                        Text("${qualitySlider.toInt()}%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = qualitySlider,
                        onValueChange = { qualitySlider = it },
                        valueRange = 10f..100f,
                        steps = 17,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // Dimension inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = targetWidthInput,
                        onValueChange = { targetWidthInput = it },
                        label = { Text("Width (px)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = targetHeightInput,
                        onValueChange = { targetHeightInput = it },
                        label = { Text("Height (px)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Process Button
                Button(
                    onClick = {
                        val uri = selectedImageUri ?: return@Button
                        isProcessing = true
                        coroutineScope.launch {
                            try {
                                val w = targetWidthInput.toIntOrNull()
                                val h = targetHeightInput.toIntOrNull()
                                val res = ImageProcessor.processImage(
                                    context = context,
                                    uri = uri,
                                    targetWidth = w,
                                    targetHeight = h,
                                    quality = qualitySlider.toInt(),
                                    format = selectedFormat
                                )
                                processedResult = res
                            } catch (e: Exception) {
                                Toast.makeText(context, "Processing failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isProcessing = false
                            }
                        }
                    },
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("compress_image_action_button")
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.padding(start = 8.dp))
                        Text("Compressing...")
                    } else {
                        Icon(Icons.Default.Compress, contentDescription = "Compress")
                        Spacer(modifier = Modifier.padding(start = 8.dp))
                        Text("Compress & Resize")
                    }
                }
            }

            // Processed Result Card
            if (processedResult != null && imageMetadata != null) {
                val res = processedResult!!
                val origSize = imageMetadata!!.sizeBytes
                val newSize = res.outputSizeBytes
                val savedPercent = if (origSize > 0) {
                    (((origSize - newSize).toFloat() / origSize.toFloat()) * 100).toInt()
                } else 0

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "COMPRESSED OUTPUT READY",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Output Size: ${StorageInspector.formatBytes(newSize)}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Resolution: ${res.outputWidth} × ${res.outputHeight} px (${res.format.extension.uppercase()})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (savedPercent > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Reduced file size by $savedPercent%",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                isSaving = true
                                coroutineScope.launch {
                                    try {
                                        val savedUri = ImageProcessor.saveToGallery(context, res)
                                        Toast.makeText(context, "Saved to Pictures/ZaswixTools!", Toast.LENGTH_LONG).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Save failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            },
                            enabled = !isSaving,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_image_gallery_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                            Spacer(modifier = Modifier.padding(start = 8.dp))
                            Text(if (isSaving) "Saving to MediaStore..." else "Save to Pictures Gallery")
                        }
                    }
                }
            }
        }
    }
}
