package com.example.ui.screens.datetime

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.datetime.DateTimeEngine
import com.example.ui.components.ResultCard
import com.example.ui.components.ZaswixTopAppBar
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale

import androidx.compose.runtime.saveable.rememberSaveable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeScreen(onBackClick: () -> Unit) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Date Difference", "Age Calculator", "Add / Subtract", "Stopwatch & Timer")

    // Date Difference State
    var diffStart by remember { mutableStateOf(LocalDate.now().minusMonths(6)) }
    var diffEnd by remember { mutableStateOf(LocalDate.now()) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    // Age Calculator State
    var birthDate by remember { mutableStateOf(LocalDate.of(2000, 1, 1)) }
    var showBirthPicker by remember { mutableStateOf(false) }

    // Add / Subtract State
    var baseDate by remember { mutableStateOf(LocalDate.now()) }
    var showBasePicker by remember { mutableStateOf(false) }
    var addAmount by remember { mutableStateOf("30") }
    var isAdd by remember { mutableStateOf(true) }
    var selectedUnit by remember { mutableStateOf(ChronoUnit.DAYS) }

    // Stopwatch State survives rotation and configuration changes via rememberSaveable
    var isStopwatchRunning by rememberSaveable { mutableStateOf(false) }
    var stopwatchMillis by rememberSaveable { mutableLongStateOf(0L) }

    LaunchedEffect(isStopwatchRunning) {
        var lastTime = System.currentTimeMillis()
        while (isStopwatchRunning) {
            delay(50)
            val now = System.currentTimeMillis()
            stopwatchMillis += (now - lastTime)
            lastTime = now
        }
    }

    Scaffold(
        topBar = {
            ZaswixTopAppBar(
                title = "Date & Time Tools",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 16.dp
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
                        // Date Difference
                        Text("Calculate duration between two dates", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { showStartPicker = true }, modifier = Modifier.weight(1f)) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Start Date", style = MaterialTheme.typography.labelSmall)
                                    Text(diffStart.format(DateTimeEngine.dateFormatter), fontWeight = FontWeight.Bold)
                                }
                            }
                            OutlinedButton(onClick = { showEndPicker = true }, modifier = Modifier.weight(1f)) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("End Date", style = MaterialTheme.typography.labelSmall)
                                    Text(diffEnd.format(DateTimeEngine.dateFormatter), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        val diffResult = remember(diffStart, diffEnd) {
                            DateTimeEngine.calculateDateDifference(diffStart, diffEnd)
                        }

                        ResultCard(
                            title = "Difference",
                            resultText = "${diffResult.years} yrs, ${diffResult.months} mos, ${diffResult.days} days",
                            subtitle = "Total: ${diffResult.totalDays} days (${diffResult.totalWeeks} weeks)"
                        )
                    }
                    1 -> {
                        // Age Calculator
                        Text("Calculate exact chronological age and next birthday", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        OutlinedButton(onClick = { showBirthPicker = true }, modifier = Modifier.fillMaxWidth()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Select Date of Birth", style = MaterialTheme.typography.labelSmall)
                                Text(birthDate.format(DateTimeEngine.dateFormatter), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }

                        val ageResult = remember(birthDate) {
                            try {
                                DateTimeEngine.calculateAge(birthDate)
                            } catch (e: Exception) {
                                null
                            }
                        }

                        if (ageResult != null) {
                            ResultCard(
                                title = "Your Exact Age",
                                resultText = "${ageResult.years} Years, ${ageResult.months} Months, ${ageResult.days} Days",
                                subtitle = "Lived for ${ageResult.totalDays} total days\nNext Birthday in ${ageResult.nextBirthdayDays} days (${ageResult.nextBirthdayDayOfWeek})"
                            )
                        }
                    }
                    2 -> {
                        // Add or Subtract Days
                        Text("Add or subtract time units from a base date", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        OutlinedButton(onClick = { showBasePicker = true }, modifier = Modifier.fillMaxWidth()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Base Date", style = MaterialTheme.typography.labelSmall)
                                Text(baseDate.format(DateTimeEngine.dateFormatter), fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = isAdd,
                                onClick = { isAdd = true },
                                label = { Text("Add (+)") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary)
                            )
                            FilterChip(
                                selected = !isAdd,
                                onClick = { isAdd = false },
                                label = { Text("Subtract (−)") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary)
                            )
                        }

                        OutlinedTextField(
                            value = addAmount,
                            onValueChange = { addAmount = it },
                            label = { Text("Amount of Days") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        val calculatedDate = remember(baseDate, addAmount, isAdd) {
                            val amt = addAmount.toLongOrNull() ?: 0L
                            DateTimeEngine.addOrSubtract(baseDate, amt, ChronoUnit.DAYS, isAdd)
                        }

                        ResultCard(
                            title = "Calculated Date",
                            resultText = calculatedDate.format(DateTimeEngine.dateFormatter),
                            subtitle = "${if (isAdd) "Added" else "Subtracted"} $addAmount days from ${baseDate.format(DateTimeEngine.dateFormatter)}"
                        )
                    }
                    3 -> {
                        // Stopwatch
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val minutes = (stopwatchMillis / 60000)
                                val seconds = (stopwatchMillis % 60000) / 1000
                                val millisHundredths = (stopwatchMillis % 1000) / 10
                                val timeFormatted = String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, millisHundredths)

                                Text(
                                    text = timeFormatted,
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Button(
                                        onClick = { isStopwatchRunning = !isStopwatchRunning },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.testTag("stopwatch_toggle_button")
                                    ) {
                                        Icon(
                                            imageVector = if (isStopwatchRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.padding(start = 6.dp))
                                        Text(if (isStopwatchRunning) "Pause" else "Start")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            isStopwatchRunning = false
                                            stopwatchMillis = 0L
                                        },
                                        modifier = Modifier.testTag("stopwatch_reset_button")
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null)
                                        Spacer(modifier = Modifier.padding(start = 6.dp))
                                        Text("Reset")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Date Picker Dialogs
    if (showStartPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = diffStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        diffStart = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showStartPicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = diffEnd.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        diffEnd = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showEndPicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showBirthPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = birthDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showBirthPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        birthDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showBirthPicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showBasePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = baseDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showBasePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        baseDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showBasePicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
