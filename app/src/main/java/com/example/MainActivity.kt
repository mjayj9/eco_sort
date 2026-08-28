package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.ApartmentSelectionScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainTabScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.GlobalState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalState.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val startDest = if (GlobalState.isLoggedIn) "main" else "login"

    NavHost(navController = navController, startDestination = startDest) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { isNewUser ->
                    if (isNewUser) {
                        navController.navigate("apartmentSelection") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
            )
        }
        composable("apartmentSelection") {
            ApartmentSelectionScreen(
                onApartmentSelected = { aptName ->
                    GlobalState.apartmentId = aptName
                    GlobalState.saveToPrefs()
                    navController.navigate("main") {
                        popUpTo("apartmentSelection") { inclusive = true }
                    }
                }
            )
        }
        composable("main") {
            MainTabScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onChangeApartment = {
                    navController.navigate("apartmentSelection")
                }
            )
        }
    }
}
