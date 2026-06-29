package com.yaksperm.analyzer.presentation.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.yaksperm.analyzer.presentation.theme.Border
import com.yaksperm.analyzer.presentation.theme.CardBg
import com.yaksperm.analyzer.presentation.theme.CyanPrimary
import com.yaksperm.analyzer.presentation.theme.TextDim
import com.yaksperm.analyzer.presentation.theme.TextPrimary

data class NavItem(val route: String, val label: String, val icon: ImageVector)

val bottomNavItems = listOf(
    NavItem("home",     "Home",     Icons.Filled.Home),
    NavItem("history",  "History",  Icons.Filled.History),
    NavItem("settings", "Settings", Icons.Filled.Settings)
)

val routesWithBottomNav = setOf("home", "history", "settings")

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    NavigationBar(
        containerColor = CardBg,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = CyanPrimary,
                    selectedTextColor   = CyanPrimary,
                    unselectedIconColor = TextDim,
                    unselectedTextColor = TextDim,
                    indicatorColor      = CyanPrimary.copy(alpha = 0.12f)
                )
            )
        }
    }
}
