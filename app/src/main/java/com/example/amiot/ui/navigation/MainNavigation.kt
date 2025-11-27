package com.example.amiot.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Article
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.amiot.ui.main.AddNewsScreen
import com.example.amiot.ui.main.HomeScreen
import com.example.amiot.ui.main.NewsDetailScreen
import com.example.amiot.ui.main.NewsItem
import com.example.amiot.ui.main.NewsScreen
import com.example.amiot.ui.main.ProfileScreen
import com.example.amiot.ui.main.SettingsScreen

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Inicio", Icons.Default.Home)
    object News : Screen("news", "Noticias", Icons.Default.Article)
    object NewsDetail : Screen("news_detail/{data}", "Detalle", Icons.Default.Article) {
        fun createRoute(news: NewsItem) = "news_detail/${news.title}|TITLE|${news.content}|CONTENT|${news.fullContent}|FULL|${news.date}|DATE|${news.category}|CATEGORY|"
    }
    object AddNews : Screen("add_news", "Agregar Noticia", Icons.Default.Article)
    object Profile : Screen("profile", "Perfil", Icons.Default.Person)
    object Settings : Screen("settings", "Configuración", Icons.Default.Settings)
}

@Composable
fun MainNavigation(
    email: String,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.News, Screen.Profile, Screen.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen()
            }
            composable(Screen.News.route) {
                NewsScreen(
                    onNewsClick = { news ->
                        navController.navigate(Screen.NewsDetail.createRoute(news))
                    },
                    onAddNewsClick = {
                        navController.navigate(Screen.AddNews.route)
                    }
                )
            }
            composable(
                route = "news_detail/{data}",
                arguments = listOf(navArgument("data") { type = NavType.StringType })
            ) { backStackEntry ->
                val data = backStackEntry.arguments?.getString("data") ?: ""
                val parts = data.split("|TITLE|", "|CONTENT|", "|FULL|", "|DATE|", "|CATEGORY|")
                
                val news = if (parts.size >= 5) {
                    NewsItem(
                        title = parts[0],
                        content = parts[1],
                        fullContent = parts[2],
                        date = parts[3],
                        category = parts[4]
                    )
                } else {
                    NewsItem(
                        title = "Noticia",
                        content = "Contenido no disponible",
                        date = "Hoy",
                        category = "General"
                    )
                }
                
                NewsDetailScreen(
                    news = news,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.AddNews.route) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val application = remember { context.applicationContext as android.app.Application }
                val viewModel: com.example.amiot.viewmodel.NewsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.example.amiot.viewmodel.NewsViewModelFactory(application)
                )
                val uiState by viewModel.uiState.collectAsState()
                
                AddNewsScreen(
                    onSaveClick = { title, shortContent, fullContent, category, date ->
                        viewModel.addNews(title, shortContent, fullContent, category, date) {
                            navController.popBackStack()
                        }
                    },
                    onBackClick = { navController.popBackStack() },
                    isLoading = uiState.isLoading,
                    errorMessage = uiState.errorMessage
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(email = email, onLogoutClick = onLogout)
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}

