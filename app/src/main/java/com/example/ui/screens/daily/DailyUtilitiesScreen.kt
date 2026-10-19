package com.example.ui.screens.daily

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.daily.BmiCategory
import com.example.domain.daily.DailyUtilitiesEngine
import com.example.ui.components.ResultCard
import com.example.ui.components.ZaswixTopAppBar
import java.math.BigDecimal

@Composable
fun DailyUtilitiesScreen(onBackClick: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("BMI Calculator", "Tip & Bill Split")

    // BMI State
    var isMetric by remember { mutableStateOf(true) }
    var heightMetric by remember { mutableStateOf("175") }
    var weightMetric by remember { mutableStateOf("70") }

    var heightFeet by remember { mutableStateOf("5") }
    var heightInches by remember { mutableStateOf("9") }
    var weightLbs by remember { mutableStateOf("154") }

    // Tip State
    var billAmountInput by remember { mutableStateOf("85.00") }
    var tipPercentInput by remember { mutableStateOf("15") }
    var splitCountInput by remember { mutableStateOf("2") }

    Scaffold(
        topBar = {
            ZaswixTopAppBar(
                title = "Daily Utilities",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // BMI CALCULATOR
                        Text(
                            text = "Calculate Body Mass Index (BMI) and healthy range",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = isMetric,
                                onClick = { isMetric = true },
                                label = { Text("Metric (cm / kg)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                            FilterChip(
                                selected = !isMetric,
                                onClick = { isMetric = false },
                                label = { Text("Imperial (ft+in / lbs)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }

                        if (isMetric) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = heightMetric,
                                    onValueChange = { heightMetric = it },
                                    label = { Text("Height (cm)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = weightMetric,
                                    onValueChange = { weightMetric = it },
                                    label = { Text("Weight (kg)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = heightFeet,
                                    onValueChange = { heightFeet = it },
                                    label = { Text("Feet (ft)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = heightInches,
                                    onValueChange = { heightInches = it },
                                    label = { Text("Inches (in)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = weightLbs,
                                    onValueChange = { weightLbs = it },
                                    label = { Text("Weight (lbs)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1.2f)
                                )
                            }
                        }

                        val bmiResult = remember(isMetric, heightMetric, weightMetric, heightFeet, heightInches, weightLbs) {
                            try {
                                if (isMetric) {
                                    val h = BigDecimal(heightMetric.trim())
                                    val w = BigDecimal(weightMetric.trim())
                                    DailyUtilitiesEngine.calculateBmiMetric(h, w)
                                } else {
                                    val ft = BigDecimal(heightFeet.trim())
                                    val inc = BigDecimal(heightInches.trim())
                                    val lbs = BigDecimal(weightLbs.trim())
                                    DailyUtilitiesEngine.calculateBmiImperial(ft, inc, lbs)
                                }
                            } catch (e: Exception) {
                                null
                            }
                        }

                        if (bmiResult != null) {
                            val catColor = when (bmiResult.category) {
                                BmiCategory.UNDERWEIGHT -> Color(0xFFFBBF24)
                                BmiCategory.NORMAL -> Color(0xFF10B981)
                                BmiCategory.OVERWEIGHT -> Color(0xFFF97316)
                                BmiCategory.OBESE -> Color(0xFFEF4444)
                            }

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        text = "BMI RESULT",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = bmiResult.bmiValue.toPlainString(),
                                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = bmiResult.category.label,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = catColor
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Healthy weight range: ${bmiResult.healthyWeightMinKg} kg - ${bmiResult.healthyWeightMaxKg} kg",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Disclaimer: BMI is a screening metric and does not constitute medical advice or diagnosis.",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        // TIP CALCULATOR
                        Text(
                            text = "Calculate tip percentage and split the bill fairly",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = billAmountInput,
                            onValueChange = { billAmountInput = it },
                            label = { Text("Bill Amount ($)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tip_bill_input")
                        )

                        // Tip Percentage Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("10", "15", "18", "20").forEach { preset ->
                                FilterChip(
                                    selected = tipPercentInput == preset,
                                    onClick = { tipPercentInput = preset },
                                    label = { Text("$preset%") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = tipPercentInput,
                                onValueChange = { tipPercentInput = it },
                                label = { Text("Custom Tip (%)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = splitCountInput,
                                onValueChange = { splitCountInput = it },
                                label = { Text("Split (People)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        val tipResult = remember(billAmountInput, tipPercentInput, splitCountInput) {
                            try {
                                val bill = BigDecimal(billAmountInput.trim())
                                val tipPct = BigDecimal(tipPercentInput.trim())
                                val split = splitCountInput.toIntOrNull() ?: 1
                                DailyUtilitiesEngine.calculateTip(bill, tipPct, split)
                            } catch (e: Exception) {
                                null
                            }
                        }

                        if (tipResult != null) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        text = "PER PERSON BREAKDOWN",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "$${tipResult.perPersonTotal}",
                                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Per person total (Includes $${tipResult.perPersonTip} tip)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (tipResult.leftoverCents > 0) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${tipResult.leftoverCents} person(s) pay $${tipResult.perPersonAmounts.first()} (+$0.01) so totals add up exactly to $${tipResult.totalBill}.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Total Tip", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("$${tipResult.tipAmount}", fontWeight = FontWeight.SemiBold)
                                        }
                                        Column {
                                            Text("Total Bill", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("$${tipResult.totalBill}", fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
