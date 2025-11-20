package com.example.amiot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.amiot.ui.auth.ForgotPasswordScreen
import com.example.amiot.viewmodel.AuthViewModelFactory
import com.example.amiot.ui.auth.LoginScreen
import com.example.amiot.ui.auth.RegisterScreen
import com.example.amiot.ui.navigation.MainNavigation
import com.example.amiot.ui.theme.AMIoTTheme
import com.example.amiot.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AMIoTTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AMIoTApp()
                }
            }
        }
    }
}

@Composable
fun AMIoTApp() {
    val context = LocalContext.current
    val application = remember { context.applicationContext as android.app.Application }
    val factory = remember { AuthViewModelFactory(application) }
    val viewModel: AuthViewModel = viewModel(factory = factory)
    val isLoggedIn by viewModel.isLoggedIn.collectAsState(initial = false)
    val userEmail by viewModel.userEmail.collectAsState(initial = "")
    val uiState by viewModel.uiState.collectAsState()
    
    var currentAuthScreen by remember { mutableStateOf<AuthScreen>(AuthScreen.Login) }

    if (isLoggedIn) {
        MainNavigation(
            email = userEmail,
            onLogout = {
                viewModel.logout()
                currentAuthScreen = AuthScreen.Login
            }
        )
    } else {
        when (currentAuthScreen) {
            AuthScreen.Login -> {
                LoginScreen(
                    onLoginClick = { email, password ->
                        viewModel.login(email, password) {
                            // Login exitoso, isLoggedIn se actualizará automáticamente
                        }
                    },
                    onRegisterClick = {
                        currentAuthScreen = AuthScreen.Register
                        viewModel.clearError()
                    },
                    onForgotPasswordClick = {
                        currentAuthScreen = AuthScreen.ForgotPassword
                        viewModel.clearError()
                    },
                    isLoading = uiState.isLoading,
                    errorMessage = uiState.errorMessage
                )
            }
            AuthScreen.Register -> {
                RegisterScreen(
                    onRegisterClick = { email, password ->
                        viewModel.register(email, password) {
                            currentAuthScreen = AuthScreen.Login
                        }
                    },
                    onLoginClick = {
                        currentAuthScreen = AuthScreen.Login
                        viewModel.clearError()
                    },
                    isLoading = uiState.isLoading,
                    errorMessage = uiState.errorMessage
                )
            }
            AuthScreen.ForgotPassword -> {
                var successMessage by remember { mutableStateOf<String?>(null) }
                
                ForgotPasswordScreen(
                    onResetClick = { email ->
                        viewModel.resetPassword(email) {
                            successMessage = "Se han enviado las instrucciones a tu email"
                        }
                    },
                    onBackToLoginClick = {
                        currentAuthScreen = AuthScreen.Login
                        viewModel.clearError()
                        successMessage = null
                    },
                    isLoading = uiState.isLoading,
                    errorMessage = uiState.errorMessage,
                    successMessage = successMessage
                )
            }
        }
    }
}

sealed class AuthScreen {
    object Login : AuthScreen()
    object Register : AuthScreen()
    object ForgotPassword : AuthScreen()
}
