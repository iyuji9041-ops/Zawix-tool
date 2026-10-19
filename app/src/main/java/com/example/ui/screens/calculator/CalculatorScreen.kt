package com.example.ui.screens.calculator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.calculator.CalculatorEngine
import com.example.ui.components.ZaswixTopAppBar

@Composable
fun CalculatorScreen(onBackClick: () -> Unit) {
    var expression by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun onKeyClick(key: String) {
        errorMessage = null
        when (key) {
            "C" -> {
                expression = ""
                resultText = ""
            }
            "⌫" -> {
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                }
            }
            "=" -> {
                if (expression.isNotBlank()) {
                    val eval = CalculatorEngine.evaluate(expression)
                    if (eval.isSuccess) {
                        resultText = eval.getOrNull()?.toPlainString() ?: ""
                        expression = resultText
                    } else {
                        errorMessage = eval.exceptionOrNull()?.message ?: "Calculation error"
                    }
                }
            }
            else -> {
                expression += key
            }
        }
    }

    Scaffold(
        topBar = {
            ZaswixTopAppBar(
                title = "Standard Calculator",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Display Area
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.End
                ) {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.End
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (expression.isNotEmpty()) {
                            androidx.compose.material3.IconButton(
                                onClick = { onKeyClick("⌫") },
                                modifier = Modifier.testTag("calc_backspace_button")
                            ) {
                                Text(
                                    text = "⌫",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            Box(modifier = Modifier.size(24.dp))
                        }

                        Text(
                            text = if (expression.isEmpty()) "0" else expression,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 32.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End,
                            maxLines = 3,
                            modifier = Modifier.testTag("calc_expression_display")
                        )
                    }

                    if (resultText.isNotEmpty() && expression != resultText) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "= $resultText",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = FontFamily.Monospace
                            ),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Keypad Grid: % is percent calculation, modulo is kept out of UI
            val buttonRows = listOf(
                listOf("C", "(", ")", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "−"),
                listOf("1", "2", "3", "+"),
                listOf("0", ".", "%", "=")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                buttonRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { label ->
                            val isOp = label in listOf("÷", "×", "−", "+", "=", "%")
                            val isSpecial = label in listOf("C", "(", ")")
                            val isAction = label == "="

                            Button(
                                onClick = { onKeyClick(label) },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when {
                                        isAction -> MaterialTheme.colorScheme.primary
                                        isOp -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        isSpecial -> MaterialTheme.colorScheme.surfaceVariant
                                        else -> MaterialTheme.colorScheme.surface
                                    },
                                    contentColor = when {
                                        isAction -> MaterialTheme.colorScheme.onPrimary
                                        isOp -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                ),
                                border = if (!isAction) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(62.dp)
                                    .testTag("calc_key_$label")
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 20.sp,
                                    fontWeight = if (isOp || isAction) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
