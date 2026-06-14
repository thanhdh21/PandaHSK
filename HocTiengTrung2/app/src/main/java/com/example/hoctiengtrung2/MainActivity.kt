package com.example.hoctiengtrung2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.hoctiengtrung2.ui.navigation.BottomNavItems
import com.example.hoctiengtrung2.ui.navigation.SetupNavGraph
import com.example.hoctiengtrung2.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HocTiengTrung2Theme {
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
                            NavigationBar(containerColor = Color.White) {
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
                                            indicatorColor = Color.Black.copy(alpha = 0.03f),
                                            selectedIconColor = TimTrung,
                                            unselectedIconColor = TimNhat.copy(alpha = 0.7f),
                                            selectedTextColor = TimTrung,
                                            unselectedTextColor = TimNhat.copy(alpha = 0.7f)
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
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
