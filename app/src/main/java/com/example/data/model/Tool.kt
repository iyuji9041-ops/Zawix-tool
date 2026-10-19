package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.graphics.vector.ImageVector

enum class ToolCategory(val title: String) {
    CALCULATORS("Calculators & Converters"),
    TEXT("Text Tools"),
    PRODUCTIVITY("Notes & Checklists"),
    MEDIA_FILES("QR & Media"),
    SECURITY("Security"),
    DATE_TIME("Date & Time"),
    DAILY("Daily Utilities")
}

data class Tool(
    val id: String,
    val title: String,
    val description: String,
    val category: ToolCategory,
    val route: String,
    val icon: ImageVector,
    val isFavorite: Boolean = false
)

object ToolsRegistry {
    val allTools: List<Tool> = listOf(
        Tool(
            id = "calc_standard",
            title = "Calculator",
            description = "Standard & scientific calculator with order of operations",
            category = ToolCategory.CALCULATORS,
            route = "tool_calc",
            icon = Icons.Default.Calculate
        ),
        Tool(
            id = "calc_percentage",
            title = "Percentage Calculator",
            description = "Calculate percentages, ratios, and increases/decreases",
            category = ToolCategory.CALCULATORS,
            route = "tool_percentage",
            icon = Icons.Default.Percent
        ),
        Tool(
            id = "calc_converter",
            title = "Unit Converter",
            description = "Length, weight, temp, area, volume & data storage",
            category = ToolCategory.CALCULATORS,
            route = "tool_converter",
            icon = Icons.Default.SwapHoriz
        ),
        Tool(
            id = "text_toolkit",
            title = "Text Toolkit",
            description = "Word counter, case switcher, line sorter & Unicode cleaner",
            category = ToolCategory.TEXT,
            route = "tool_text",
            icon = Icons.Default.Description
        ),
        Tool(
            id = "notes_checklist",
            title = "Notes & Checklists",
            description = "Persistent private notes with task checklists and search",
            category = ToolCategory.PRODUCTIVITY,
            route = "tool_notes",
            icon = Icons.Default.Checklist
        ),
        Tool(
            id = "qr_tools",
            title = "QR Code Tools",
            description = "Generate text, URL, Wi-Fi codes & camera scanner",
            category = ToolCategory.MEDIA_FILES,
            route = "tool_qr",
            icon = Icons.Default.QrCode
        ),
        Tool(
            id = "image_tools",
            title = "Image Tools",
            description = "Resize and compress JPEG, PNG, WEBP with preview",
            category = ToolCategory.MEDIA_FILES,
            route = "tool_image",
            icon = Icons.Default.Image
        ),
        Tool(
            id = "file_storage",
            title = "File & Storage Tools",
            description = "Storage metrics, SAF file details, open & share",
            category = ToolCategory.MEDIA_FILES,
            route = "tool_files",
            icon = Icons.Default.Folder
        ),
        Tool(
            id = "password_gen",
            title = "Password Generator",
            description = "Secure random passwords with entropy strength meter",
            category = ToolCategory.SECURITY,
            route = "tool_password",
            icon = Icons.Default.Lock
        ),
        Tool(
            id = "date_time",
            title = "Date & Time Tools",
            description = "Date difference, age calculator, countdown & stopwatch",
            category = ToolCategory.DATE_TIME,
            route = "tool_datetime",
            icon = Icons.Default.CalendarMonth
        ),
        Tool(
            id = "daily_utils",
            title = "Daily Utilities",
            description = "BMI health checker with metric/imperial, and tip splitter",
            category = ToolCategory.DAILY,
            route = "tool_daily",
            icon = Icons.Default.FitnessCenter
        )
    )
}
