package com.example.ui.screens.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.calculator.PercentageEngine
import com.example.ui.components.ResultCard
import com.example.ui.components.ZaswixTopAppBar
import java.math.BigDecimal

@Composable
fun PercentageScreen(onBackClick: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("What is X% of Y", "X is what % of Y", "% Change")

    // Tab 1 state: X% of Y
    var percent1 by remember { mutableStateOf("15") }
    var value1 by remember { mutableStateOf("250") }

    // Tab 2 state: X is what % of Y
    var part2 by remember { mutableStateOf("45") }
    var total2 by remember { mutableStateOf("180") }

    // Tab 3 state: % Change from X to Y
    var initial3 by remember { mutableStateOf("80") }
    var final3 by remember { mutableStateOf("120") }

    Scaffold(
        topBar = {
            ZaswixTopAppBar(
                title = "Percentage Calculator",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // What is X% of Y
                        Text("Calculate a percentage portion of an amount", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = percent1,
                                onValueChange = { percent1 = it },
                                label = { Text("Percentage (%)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("percent_input_x")
                            )
                            OutlinedTextField(
                                value = value1,
                                onValueChange = { value1 = it },
                                label = { Text("Total Amount") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("percent_input_y")
                            )
                        }

                        val res1 = remember(percent1, value1) {
                            try {
                                val p = BigDecimal(percent1.trim())
                                val v = BigDecimal(value1.trim())
                                PercentageEngine.percentageOf(p, v).toPlainString()
                            } catch (e: Exception) {
                                null
                            }
                        }

                        if (res1 != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            ResultCard(
                                title = "Result",
                                resultText = res1,
                                subtitle = "$percent1% of $value1 is $res1"
                            )
                        }
                    }
                    1 -> {
                        // X is what % of Y
                        Text("Find what percentage one number is of another", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = part2,
                                onValueChange = { part2 = it },
                                label = { Text("Part (X)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("part_input_x")
                            )
                            OutlinedTextField(
                                value = total2,
                                onValueChange = { total2 = it },
                                label = { Text("Total (Y)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("total_input_y")
                            )
                        }

                        val res2 = remember(part2, total2) {
                            try {
                                val part = BigDecimal(part2.trim())
                                val total = BigDecimal(total2.trim())
                                "${PercentageEngine.whatPercentage(part, total).toPlainString()}%"
                            } catch (e: Exception) {
                                null
                            }
                        }

                        if (res2 != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            ResultCard(
                                title = "Result",
                                resultText = res2,
                                subtitle = "$part2 is $res2 of $total2"
                            )
                        }
                    }
                    2 -> {
                        // % Change
                        Text("Calculate the percentage increase or decrease", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = initial3,
                                onValueChange = { initial3 = it },
                                label = { Text("Initial Value") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("initial_input_x")
                            )
                            OutlinedTextField(
                                value = final3,
                                onValueChange = { final3 = it },
                                label = { Text("Final Value") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("final_input_y")
                            )
                        }

                        val res3 = remember(initial3, final3) {
                            try {
                                val init = BigDecimal(initial3.trim())
                                val fin = BigDecimal(final3.trim())
                                val change = PercentageEngine.percentageChange(init, fin)
                                val sign = if (change >= BigDecimal.ZERO) "+" else ""
                                "$sign${change.toPlainString()}%"
                            } catch (e: Exception) {
                                null
                            }
                        }

                        if (res3 != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            ResultCard(
                                title = "Result",
                                resultText = res3,
                                subtitle = "From $initial3 to $final3 is a change of $res3"
                            )
                        }
                    }
                }
            }
        }
    }
}
