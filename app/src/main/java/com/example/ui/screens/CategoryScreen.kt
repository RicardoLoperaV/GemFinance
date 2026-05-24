package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.util.CurrencyHelper
import com.example.ui.viewmodel.FinanceViewModel

@Composable
fun CategoryScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var isAddingNew by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var limitInput by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf("#F2B8B5") }
    var selectedIconName by remember { mutableStateOf("restaurant") }

    val presetColors = listOf(
        "#F2B8B5", // Food Red
        "#7FCFFF", // Transport Blue
        "#B4E495", // Ocio Green
        "#00E5FF", // Services Cyan
        "#EADDFF", // Premium Pink
        "#FFB74D", // Warn Orange
        "#FF8A80", // Coral
        "#80DEEA" // Teal
    )

    val presetIcons = listOf(
        "restaurant" to Icons.Default.Restaurant,
        "directions_car" to Icons.Default.DirectionsCar,
        "bolt" to Icons.Default.Bolt,
        "sports_esports" to Icons.Default.SportsEsports,
        "flight" to Icons.Default.Flight,
        "medication" to Icons.Default.Medication,
        "school" to Icons.Default.School,
        "home" to Icons.Default.Home,
        "shopping_cart" to Icons.Default.ShoppingCart
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PRESUPUESTOS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Configuración",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (!isAddingNew) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isAddingNew = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Limit", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nuevo", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        if (isAddingNew) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Añadir Categoría & Límite",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Nombre de Categoría") },
                            placeholder = { Text("p. ej., Alquiler o Gimnasio") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = limitInput,
                            onValueChange = { limitInput = it },
                            label = { Text("Límite Mensual (COP)") },
                            placeholder = { Text("0 si no tiene presupuesto") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Preset Colors list
                        Text("Color Representativo", style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            presetColors.forEach { colorStr ->
                                val parsedColor = try {
                                    Color(android.graphics.Color.parseColor(colorStr))
                                } catch (e: Exception) {
                                    MaterialTheme.colorScheme.primary
                                }
                                val outlineBorder = if (colorStr == selectedColorHex) {
                                    Modifier.size(32.dp).clip(CircleShape).background(parsedColor).clickable {
                                        selectedColorHex = colorStr
                                    }.padding(4.dp).background(Color.White).clip(CircleShape).background(parsedColor)
                                } else {
                                    Modifier.size(32.dp).clip(CircleShape).background(parsedColor).clickable {
                                        selectedColorHex = colorStr
                                    }
                                }

                                Box(modifier = outlineBorder)
                            }
                        }

                        // Presets Icons Selection
                        Text("Icono Visual", style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            presetIcons.take(6).forEach { pair ->
                                val isSelected = pair.first == selectedIconName
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                        .clickable {
                                            selectedIconName = pair.first
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = pair.second,
                                        contentDescription = pair.first,
                                        modifier = Modifier.size(18.dp),
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White
                                    )
                                }
                            }
                        }

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { isAddingNew = false },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancelar")
                            }

                            Button(
                                onClick = {
                                    val limitVal = limitInput.toDoubleOrNull() ?: 0.0
                                    if (nameInput.trim().isEmpty()) {
                                        Toast.makeText(context, "Ingrese un nombre", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.addCategory(
                                        name = nameInput.trim(),
                                        iconName = selectedIconName,
                                        colorHex = selectedColorHex,
                                        monthlyLimit = limitVal
                                    )

                                    // Reset attributes
                                    nameInput = ""
                                    limitInput = ""
                                    isAddingNew = false
                                    Toast.makeText(context, "Categoría creada", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Guardar", color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                }
            }
        }

        if (categories.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No posee categorías guardadas", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(categories) { category ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val tintColor = try {
                                Color(android.graphics.Color.parseColor(category.colorHex))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(tintColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getIconVectorByName(category.iconName),
                                    contentDescription = category.name,
                                    tint = tintColor
                                )
                            }

                            Column {
                                Text(
                                    text = category.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = if (category.monthlyLimit > 0.0) {
                                        "Límite: ${CurrencyHelper.formatCOP(category.monthlyLimit)}"
                                    } else {
                                        "Sin presupuesto asignado"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.deleteCategory(category.id)
                                Toast.makeText(context, "Categoría eliminada", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
