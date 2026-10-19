package com.example.ui.screens.text

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.text.TextToolkitEngine
import com.example.ui.components.ZaswixTopAppBar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TextToolkitScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("नमस्ते दुनिया! Welcome to Zaswix Tools text suite.") }

    val stats by remember(inputText) {
        derivedStateOf { TextToolkitEngine.analyze(inputText) }
    }

    Scaffold(
        topBar = {
            ZaswixTopAppBar(
                title = "Text Toolkit",
                onBackClick = onBackClick,
                actions = {
                    if (inputText.isNotEmpty()) {
                        IconButton(onClick = { inputText = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear text", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
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
            // Live Statistics Chips
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "REAL-TIME METRICS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricChip(label = "Words", value = stats.wordCount.toString())
                        MetricChip(label = "Chars", value = stats.characterCount.toString())
                        MetricChip(label = "No Spaces", value = stats.characterCountNoSpaces.toString())
                        MetricChip(label = "Lines", value = stats.lineCount.toString())
                        MetricChip(label = "Sentences", value = stats.sentenceCount.toString())
                    }
                }
            }

            // Input Text Field
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Enter or paste text") },
                placeholder = { Text("Type or paste text here (Supports Hindi & Unicode)...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .testTag("text_toolkit_editor"),
                shape = RoundedCornerShape(14.dp)
            )

            // Transformations Header
            Text(
                text = "TRANSFORMATIONS",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.primary
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { inputText = TextToolkitEngine.toUpperCase(inputText) },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text("UPPERCASE")
                }

                OutlinedButton(
                    onClick = { inputText = TextToolkitEngine.toLowerCase(inputText) },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text("lowercase")
                }

                OutlinedButton(
                    onClick = { inputText = TextToolkitEngine.toTitleCase(inputText) },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text("Title Case")
                }

                OutlinedButton(
                    onClick = { inputText = TextToolkitEngine.removeExtraSpaces(inputText) },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text("Remove Extra Spaces")
                }

                OutlinedButton(
                    onClick = { inputText = TextToolkitEngine.removeDuplicateLines(inputText) },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text("Remove Duplicate Lines")
                }

                OutlinedButton(
                    onClick = { inputText = TextToolkitEngine.sortLines(inputText, ascending = true) },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text("Sort Lines (A-Z)")
                }

                OutlinedButton(
                    onClick = { inputText = TextToolkitEngine.reverseText(inputText) },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text("Reverse Text")
                }
            }

            // Copy & Share Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Text Toolkit", inputText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("copy_text_button")
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy")
                    Spacer(modifier = Modifier.padding(start = 6.dp))
                    Text("Copy")
                }

                Button(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, inputText)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Text"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                    Spacer(modifier = Modifier.padding(start = 6.dp))
                    Text("Share")
                }
            }
        }
    }
}

@Composable
private fun MetricChip(label: String, value: String) {
    SuggestionChip(
        onClick = {},
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$label: ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    )
}
