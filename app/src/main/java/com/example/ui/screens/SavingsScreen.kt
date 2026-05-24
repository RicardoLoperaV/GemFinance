package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SavingsGoal
import com.example.data.util.CurrencyHelper
import com.example.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val savingsGoals by viewModel.savingsGoals.collectAsState()
    val liquidSavings by viewModel.totalLiquidSavings.collectAsState()
    val investmentBalance by viewModel.totalInvestmentBalance.collectAsState()

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var goalName by remember { mutableStateOf("") }
    var goalTarget by remember { mutableStateOf("") }
    var goalCurrent by remember { mutableStateOf("") }

    var showIncrementDialog by remember { mutableStateOf<SavingsGoal?>(null) }
    var incrementAmountInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High Density Summary Card for Investments + Savings
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "ESTADO DE ACTIVOS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Ahorros Líquidos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = CurrencyHelper.formatCOP(liquidSavings),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Spark ring mini details
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Inversiones Activas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = CurrencyHelper.formatCOP(investmentBalance),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB4E495)
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val totalAssets = liquidSavings + investmentBalance
                            Text("Patrimonio Consolidado", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = CurrencyHelper.formatCOP(totalAssets),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Trending Up",
                            tint = Color(0xFFB4E495),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        // Section header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "METAS DE AHORRO",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showAddGoalDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Add Goal", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nueva Meta", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (savingsGoals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No tiene metas registradas", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(savingsGoals) { goal ->
                SavingsGoalItem(
                    goal = goal,
                    onIncrementRequested = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showIncrementDialog = goal
                    },
                    onDeleteRequested = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.deleteSavingsGoal(goal.id)
                        Toast.makeText(context, "Meta eliminada con éxito", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    // Modal to create savings goal
    if (showAddGoalDialog) {
        AlertDialog(
            onDismissRequest = { showAddGoalDialog = false },
            title = { Text("Añadir Nueva Meta", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = goalName,
                        onValueChange = { goalName = it },
                        label = { Text("Nombre de la Meta") },
                        placeholder = { Text("p. ej., Fondo de Emergencias") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = goalTarget,
                        onValueChange = { goalTarget = it },
                        label = { Text("Monto de Destino (COP)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = goalCurrent,
                        onValueChange = { goalCurrent = it },
                        label = { Text("Monto Inicial (COP)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = goalTarget.toDoubleOrNull() ?: 0.0
                        val current = goalCurrent.toDoubleOrNull() ?: 0.0
                        if (goalName.trim().isEmpty() || target <= 0.0) {
                            Toast.makeText(context, "Complete todos los campos requeridos", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        viewModel.addSavingsGoal(
                            name = goalName.trim(),
                            targetAmount = target,
                            currentAmount = current,
                            colorHex = "#00E5FF" // Cool Cyan theme default
                        )

                        goalName = ""
                        goalTarget = ""
                        goalCurrent = ""
                        showAddGoalDialog = false
                        Toast.makeText(context, "Meta Guardada", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGoalDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal to increase progress
    showIncrementDialog?.let { goal ->
        AlertDialog(
            onDismissRequest = { showIncrementDialog = null },
            title = { Text("Aportar a: ${goal.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Transferir de tus Ahorros Líquidos habituales.", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = incrementAmountInput,
                        onValueChange = { incrementAmountInput = it },
                        label = { Text("Monto a aportar (COP)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = incrementAmountInput.toDoubleOrNull() ?: 0.0
                        if (amount <= 0.0) {
                            Toast.makeText(context, "Por favor ingrese un valor válido", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        viewModel.incrementSavingsGoalProgress(goal, amount)
                        incrementAmountInput = ""
                        showIncrementDialog = null
                        Toast.makeText(context, "¡Aporte registrado!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Confirmar Aporte")
                }
            },
            dismissButton = {
                TextButton(onClick = { showIncrementDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun SavingsGoalItem(
    goal: SavingsGoal,
    onIncrementRequested: () -> Unit,
    onDeleteRequested: () -> Unit
) {
    val progressPercent = if (goal.targetAmount > 0.0) {
        ((goal.currentAmount / goal.targetAmount) * 100).coerceAtMost(100.0)
    } else {
        0.0
    }

    val animatedProgress = animateFloatAsState(
        targetValue = (progressPercent / 100.0).toFloat(),
        animationSpec = tween(1200),
        label = "Progress"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Distinct Premium Savings Progress Ring
            Box(
                modifier = Modifier.size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                val accentColor = try {
                    if (progressPercent >= 100.0) Color(0xFFB4E495) else Color(android.graphics.Color.parseColor(goal.colorHex))
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.primary
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color(0xFF2B2930),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = accentColor,
                        startAngle = -90f,
                        sweepAngle = animatedProgress.value * 360f,
                        useCenter = false,
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Text(
                    text = "${progressPercent.toInt()}%",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = goal.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                Text(
                    text = "${CurrencyHelper.formatCOPSimple(goal.currentAmount)} de ${CurrencyHelper.formatCOPSimple(goal.targetAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Interactive Actions inside Savings Hub items
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onIncrementRequested) {
                    Icon(Icons.Default.TrendingUp, contentDescription = "Invest Detail", tint = Color(0xFF00E5FF))
                }

                IconButton(onClick = onDeleteRequested) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
