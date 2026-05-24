package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FinanceViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: FinanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppFrame(viewModel)
            }
        }
    }
}

enum class NavigationTab(val label: String) {
    DASHBOARD("Inicio"),
    INPUT("Nuevo"),
    INSIGHTS("Análisis"),
    SAVINGS("Ahorros"),
    CATEGORIES("Límites")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppFrame(viewModel: FinanceViewModel) {
    var activeTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
    val haptic = LocalHapticFeedback.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("main_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                // Tab: INICIO / DASHBOARD
                NavigationBarItem(
                    selected = activeTab == NavigationTab.DASHBOARD,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        activeTab = NavigationTab.DASHBOARD
                    },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == NavigationTab.DASHBOARD) Icons.Default.Home else Icons.Outlined.Home,
                            contentDescription = "Inicio"
                        )
                    },
                    label = { Text("Inicio", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                // Tab: ANALISIS / INSIGHTS
                NavigationBarItem(
                    selected = activeTab == NavigationTab.INSIGHTS,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        activeTab = NavigationTab.INSIGHTS
                    },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == NavigationTab.INSIGHTS) Icons.Default.PieChart else Icons.Outlined.PieChart,
                            contentDescription = "Análisis"
                        )
                    },
                    label = { Text("Análisis", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                // Tab: MULTI INPUT SPENDING TRIGGER
                NavigationBarItem(
                    selected = activeTab == NavigationTab.INPUT,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        activeTab = NavigationTab.INPUT
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Nuevo Gasto",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = { Text("Registrar", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = Color.Transparent
                    )
                )

                // Tab: AHORROS / SAVINGS
                NavigationBarItem(
                    selected = activeTab == NavigationTab.SAVINGS,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        activeTab = NavigationTab.SAVINGS
                    },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == NavigationTab.SAVINGS) Icons.Default.Savings else Icons.Outlined.Savings,
                            contentDescription = "Ahorros"
                        )
                    },
                    label = { Text("Ahorros", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                // Tab: CATEGORIES / BUDGET SETUPS
                NavigationBarItem(
                    selected = activeTab == NavigationTab.CATEGORIES,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        activeTab = NavigationTab.CATEGORIES
                    },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == NavigationTab.CATEGORIES) Icons.Default.Settings else Icons.Outlined.Settings,
                            contentDescription = "Límites"
                        )
                    },
                    label = { Text("Límites", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Elegant horizontal slide layout transition transitions
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }.using(
                        SizeTransform(clip = false)
                    )
                },
                label = "NavigationTransition"
            ) { targetTab ->
                when (targetTab) {
                    NavigationTab.DASHBOARD -> {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToInput = { activeTab = NavigationTab.INPUT }
                        )
                    }
                    NavigationTab.INPUT -> {
                        InputScreen(
                            viewModel = viewModel,
                            onTransactionLogged = { activeTab = NavigationTab.DASHBOARD }
                        )
                    }
                    NavigationTab.INSIGHTS -> {
                        InsightsScreen(
                            viewModel = viewModel
                        )
                    }
                    NavigationTab.SAVINGS -> {
                        SavingsScreen(
                            viewModel = viewModel
                        )
                    }
                    NavigationTab.CATEGORIES -> {
                        CategoryScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}
