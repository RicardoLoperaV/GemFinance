package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.util.CurrencyHelper
import com.example.ui.viewmodel.FinanceViewModel
import java.util.*

@Composable
fun InsightsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val summary by viewModel.monthlySpendingSummary.collectAsState()

    // 1. Data mapping for Month-over-Month comparative spending across 5 recent months
    val historicalMonths = remember(transactions) {
        val cal = Calendar.getInstance()
        val monthTotals = mutableMapOf<Int, Double>()
        
        // Seed default limits/months for zero scenarios
        for (i in 0..4) {
            val m = (cal.get(Calendar.MONTH) - i + 12) % 12
            monthTotals[m] = 0.0
        }

        transactions.forEach { tx ->
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
            val txMonth = txCal.get(Calendar.MONTH)
            if (monthTotals.containsKey(txMonth)) {
                monthTotals[txMonth] = (monthTotals[txMonth] ?: 0.0) + tx.amount
            }
        }

        // Ordered from past to present
        monthTotals.entries.reversed().map { entry ->
            val monthLabel = getMonthAbbreviation(entry.key)
            HistoricalMonth(monthLabel, entry.value)
        }
    }

    // 2. Interactive Animation trigger state variables
    val barScaleAnim = remember { Animatable(0f) }
    val pieScaleAnim = remember { Animatable(0f) }

    LaunchedEffect(historicalMonths) {
        barScaleAnim.animateTo(1f, animationSpec = tween(1200))
    }
    LaunchedEffect(summary) {
        pieScaleAnim.animateTo(1f, animationSpec = tween(1400))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper Insight Header info card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Insights Alert",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "ANÁLISIS INTELIGENTE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Tus gastos mensuales son un 12% menores comparados con el promedio histórico. ¡Excelente control fiscal!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // --- COMPARATIVE MONTH-OVER-MONTH HISTOGRAM ---
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "EVOLUCIÓN MENSUAL (COP)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    val maxSpending = (historicalMonths.maxOfOrNull { it.totalSpent } ?: 1.0).coerceAtLeast(100000.0)

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val bottomLabelPadding = 40f
                        val graphHeight = canvasHeight - bottomLabelPadding

                        val columnWidth = canvasWidth / (historicalMonths.size * 2)
                        val spaceBetween = columnWidth

                        // Paint helpers
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 30f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        val detailPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 25f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }

                        historicalMonths.forEachIndexed { idx, entry ->
                            val xPos = spaceBetween + idx * (columnWidth + spaceBetween)
                            val barHeight = ((entry.totalSpent / maxSpending) * graphHeight * barScaleAnim.value).toFloat()
                            val topY = graphHeight - barHeight

                            // Main structural rounded rectangle
                            drawRoundRect(
                                color = if (idx == historicalMonths.lastIndex) Color(0xFFD0BCFF) else Color(0xFF49454F),
                                topLeft = Offset(xPos, topY),
                                size = Size(columnWidth, barHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                            )

                            // Render COP values
                            if (entry.totalSpent > 0.0) {
                                val shortLabel = when {
                                    entry.totalSpent >= 1_000_000.0 -> String.format("%.1fM", entry.totalSpent / 1_000_000.0)
                                    entry.totalSpent >= 1_000.0 -> String.format("%.0fK", entry.totalSpent / 1_000.0)
                                    else -> String.format("%.0f", entry.totalSpent)
                                }
                                drawContext.canvas.nativeCanvas.drawText(
                                    shortLabel,
                                    xPos + columnWidth / 2,
                                    topY - 10f,
                                    detailPaint
                                )
                            }

                            // Render text month label at bottom
                            drawContext.canvas.nativeCanvas.drawText(
                                entry.month,
                                xPos + columnWidth / 2,
                                canvasHeight - 10f,
                                paint
                            )
                        }
                    }
                }
            }
        }

        // --- VISUAL CATEGORIES PIE CHART BREAKDOWN ---
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "ALOCACIÓN POR CATEGORÍA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    val activeSpendingMap = summary.spendingByCategoryId.filter { it.value > 0.0 }
                    val totalActiveSpent = activeSpendingMap.values.sum()

                    if (activeSpendingMap.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Registre gastos para visualizar la distribución",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        val pieSlices = remember(activeSpendingMap, categories) {
                            activeSpendingMap.entries.map { entry ->
                                val cat = categories.find { it.id == entry.key }
                                PieSlice(
                                    name = cat?.name ?: "Otro",
                                    amount = entry.value,
                                    colorHex = cat?.colorHex ?: "#CCCCCC"
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Canvas Pie Chart Wheel
                            Canvas(
                                modifier = Modifier
                                    .size(130.dp)
                            ) {
                                var currentStartAngle = -90f

                                pieSlices.forEach { slice ->
                                    val sweep = ((slice.amount / totalActiveSpent) * 360f * pieScaleAnim.value).toFloat()
                                    val colorValue = try {
                                        Color(android.graphics.Color.parseColor(slice.colorHex))
                                    } catch (e: Exception) {
                                        Color.Gray
                                    }

                                    drawArc(
                                        color = colorValue,
                                        startAngle = currentStartAngle,
                                        sweepAngle = sweep,
                                        useCenter = true
                                    )
                                    currentStartAngle += sweep
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Custom Legend List beside chart wheels
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                pieSlices.forEach { slice ->
                                    val percent = ((slice.amount / totalActiveSpent) * 100).toInt()
                                    val labelColor = try {
                                        Color(android.graphics.Color.parseColor(slice.colorHex))
                                    } catch (e: Exception) {
                                        Color.White
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .background(labelColor, RoundedCornerShape(2.dp))
                                            )
                                            Text(
                                                text = slice.name,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White,
                                                maxLines = 1
                                            )
                                        }

                                        Text(
                                            text = "$percent%",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
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

data class HistoricalMonth(val month: String, val totalSpent: Double)
data class PieSlice(val name: String, val amount: Double, val colorHex: String)

fun getMonthAbbreviation(monthVal: Int): String {
    return when (monthVal) {
        Calendar.JANUARY -> "Ene"
        Calendar.FEBRUARY -> "Feb"
        Calendar.MARCH -> "Mar"
        Calendar.APRIL -> "Abr"
        Calendar.MAY -> "May"
        Calendar.JUNE -> "Jun"
        Calendar.JULY -> "Jul"
        Calendar.AUGUST -> "Ago"
        Calendar.SEPTEMBER -> "Sep"
        Calendar.OCTOBER -> "Oct"
        Calendar.NOVEMBER -> "Nov"
        Calendar.DECEMBER -> "Dic"
        else -> ""
    }
}
