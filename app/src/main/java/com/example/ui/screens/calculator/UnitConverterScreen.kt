package com.example.ui.screens.calculator

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.converters.ConversionUnit
import com.example.domain.converters.UnitCategory
import com.example.domain.converters.UnitConverterEngine
import com.example.ui.components.ResultCard
import com.example.ui.components.ZaswixTopAppBar
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(onBackClick: () -> Unit) {
    var selectedCategory by remember { mutableStateOf(UnitCategory.LENGTH) }
    val availableUnits = remember(selectedCategory) {
        UnitConverterEngine.unitsByCategory[selectedCategory] ?: emptyList()
    }

    var fromUnit by remember(selectedCategory) { mutableStateOf(availableUnits.first()) }
    var toUnit by remember(selectedCategory) {
        mutableStateOf(if (availableUnits.size > 1) availableUnits[1] else availableUnits.first())
    }

    var inputValue by remember { mutableStateOf("1") }
    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }

    val result = remember(inputValue, fromUnit, toUnit) {
        try {
            val bd = BigDecimal(inputValue.trim())
            UnitConverterEngine.convert(bd, fromUnit, toUnit).toPlainString()
        } catch (e: Exception) {
            null
        }
    }

    Scaffold(
        topBar = {
            ZaswixTopAppBar(
                title = "Unit Converter",
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Category selector chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UnitCategory.entries.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            selectedCategory = category
                            val newUnits = UnitConverterEngine.unitsByCategory[category] ?: emptyList()
                            fromUnit = newUnits.first()
                            toUnit = if (newUnits.size > 1) newUnits[1] else newUnits.first()
                        },
                        label = { Text(category.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Input value
            OutlinedTextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                label = { Text("Value to Convert") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("unit_converter_input")
            )

            // From / To pickers with Swap button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // From Dropdown
                ExposedDropdownMenuBox(
                    expanded = fromExpanded,
                    onExpandedChange = { fromExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = "${fromUnit.name} (${fromUnit.symbol})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("From") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = fromExpanded,
                        onDismissRequest = { fromExpanded = false }
                    ) {
                        availableUnits.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text("${unit.name} (${unit.symbol})") },
                                onClick = {
                                    fromUnit = unit
                                    fromExpanded = false
                                }
                            )
                        }
                    }
                }

                // Swap Button
                IconButton(
                    onClick = {
                        val temp = fromUnit
                        fromUnit = toUnit
                        toUnit = temp
                    },
                    modifier = Modifier.testTag("unit_swap_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Swap units",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // To Dropdown
                ExposedDropdownMenuBox(
                    expanded = toExpanded,
                    onExpandedChange = { toExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = "${toUnit.name} (${toUnit.symbol})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("To") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = toExpanded,
                        onDismissRequest = { toExpanded = false }
                    ) {
                        availableUnits.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text("${unit.name} (${unit.symbol})") },
                                onClick = {
                                    toUnit = unit
                                    toExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Result Display
            if (result != null) {
                Spacer(modifier = Modifier.height(10.dp))
                ResultCard(
                    title = "Converted Value",
                    resultText = "$result ${toUnit.symbol}",
                    subtitle = "$inputValue ${fromUnit.symbol} = $result ${toUnit.symbol}"
                )
            }
        }
    }
}
