package com.lyc.sstcontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.lyc.sstcontrol.ui.login.LoginScreen
import com.lyc.sstcontrol.ui.login.LoginViewModel
import com.lyc.sstcontrol.ui.main.MainScreen
import com.lyc.sstcontrol.ui.theme.SSTControlTheme
import com.lyc.sstcontrol.ui.theme.StatusBarIcons

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        setContent {
            SSTControlTheme {
                val profile = viewModel.profile
                when {
                    viewModel.checkingSession -> LoadingScreen()
                    profile == null -> LoginScreen(viewModel)
                    else -> MainScreen(profile = profile, onSignOut = viewModel::signOut)
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    StatusBarIcons(darkIcons = true)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}