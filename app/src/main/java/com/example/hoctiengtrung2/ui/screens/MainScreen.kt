package com.example.hoctiengtrung2.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.example.hoctiengtrung2.ui.navigation.BottomNavItems
import com.example.hoctiengtrung2.ui.navigation.SetupNavGraph
import com.example.hoctiengtrung2.ui.navigation.Screen
import com.example.hoctiengtrung2.utils.SessionManager

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val daDangNhap = remember(context) { SessionManager.daDangNhap(context) }
    val startDest = remember(daDangNhap) {
        if (daDangNhap) Screen.TrangChu.route else Screen.ChaoMung.route
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Hiển thị BottomBar nếu route hiện tại thuộc về danh sách màn hình chính
    val showBottomBar = BottomNavItems.any { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    BottomNavItems.forEach { screen ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = { 
                                screen.icon?.let { 
                                    Icon(
                                        imageVector = it, 
                                        contentDescription = screen.title 
                                    ) 
                                } 
                            },
                            label = { Text(screen.title) },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            ),
                            onClick = {
                                if (!isSelected) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        SetupNavGraph(
            navController = navController,
            startDestination = startDest,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
