package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.util.CurrencyHelper
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(
    viewModel: FinanceViewModel,
    onTransactionLogged: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var amountInput by remember { mutableStateOf("0") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var notesInput by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }

    // Synchronize category selection if list gets seeded or updated
    LaunchedEffect(categories) {
        if (selectedCategory == null && categories.isNotEmpty()) {
            selectedCategory = categories.first()
        }
    }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper COP visual field
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "VALOR DE TRANSACCIÓN",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                val doubleVal = amountInput.toDoubleOrNull() ?: 0.0
                Text(
                    text = CurrencyHelper.formatCOP(doubleVal),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    ),
                    modifier = Modifier.testTag("amount_display"),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Category Scroll Selector Row
        Text(
            text = "Elegir Categoría",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
            fontWeight = FontWeight.SemiBold
        )

        if (categories.isEmpty()) {
            Text(
                text = "Cargando categorías...",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 140.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory?.id == category.id
                    val cardBg = if (isSelected) {
                        try {
                            Color(android.graphics.Color.parseColor(category.colorHex)).copy(alpha = 0.25f)
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                    val borderStroke = if (isSelected) {
                        BorderStroke(1.5.dp, try {
                            Color(android.graphics.Color.parseColor(category.colorHex))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary
                        })
                    } else {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    }

                    Card(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedCategory = category
                        },
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = borderStroke,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val tintColor = try {
                                Color(android.graphics.Color.parseColor(category.colorHex))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }
                            Icon(
                                imageVector = getIconVectorByName(category.iconName),
                                contentDescription = category.name,
                                tint = tintColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Details Column
        OutlinedTextField(
            value = notesInput,
            onValueChange = { notesInput = it },
            label = { Text("Notas degasto") },
            placeholder = { Text("p. ej., almuerzo casero") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Notes, contentDescription = "Notes") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            singleLine = true
        )

        // Date selection Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showDatePicker = true
                }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Calendar", tint = MaterialTheme.colorScheme.primary)
                Text("Fecha de transacción", style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = dateFormatter.format(Date(selectedDate)),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Tactile Haptic Keypad
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("000", "0", "⌫")
                )

                keys.forEach { rowKeys ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowKeys.forEach { key ->
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    keyPressed(key, amountInput) { newStr ->
                                        amountInput = newStr
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (key == "⌫") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = key,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Primary Log Action
        Button(
            onClick = {
                val value = amountInput.toDoubleOrNull() ?: 0.0
                if (value <= 0.0) {
                    Toast.makeText(context, "Por favor ingrese un valor mayor a 0", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val cat = selectedCategory
                if (cat == null) {
                    Toast.makeText(context, "Por favor cree o elija una categoría", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.logTransaction(
                    amount = value,
                    category = cat,
                    notes = notesInput,
                    timestamp = selectedDate
                )

                Toast.makeText(context, "Gasto registrado con éxito (${CurrencyHelper.formatCOPSimple(value)})", Toast.LENGTH_SHORT).show()
                notesInput = ""
                amountInput = "0"
                onTransactionLogged() // Callback to switch back or navigate
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_transaction_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = "Log", modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Registar Transacción", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary)
        }
    }

    // Material 3 Custom Date Picker Dialogue
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDate = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("De Acuerdo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun keyPressed(key: String, current: String, onUpdate: (String) -> Unit) {
    if (key == "⌫") {
        if (current.length > 1) {
            onUpdate(current.substring(0, current.length - 1))
        } else {
            onUpdate("0")
        }
    } else {
        if (current == "0") {
            if (key != "0" && key != "000") {
                onUpdate(key)
            }
        } else {
            // Cap limit to keep numbers clean
            if (current.length < 10) {
                onUpdate(current + key)
            }
        }
    }
}

/**
 * Utility vector mapper for generic category definitions
 */
fun getIconVectorByName(name: String): ImageVector {
    return when (name.lowercase()) {
        "restaurant", "food", "cafeteria" -> Icons.Default.Restaurant
        "directions_car", "transport", "transporte" -> Icons.Default.DirectionsCar
        "bolt", "light", "energy", "servicios" -> Icons.Default.Bolt
        "sports_esports", "netflix", "games" -> Icons.Default.SportsEsports
        "flight", "trip", "travel" -> Icons.Default.Flight
        "medication", "health" -> Icons.Default.Medication
        "school", "book" -> Icons.Default.School
        "home", "house" -> Icons.Default.Home
        "shopping_cart", "shop" -> Icons.Default.ShoppingCart
        else -> Icons.Default.LocalOffer
    }
}
