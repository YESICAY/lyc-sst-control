package com.lyc.sstcontrol.ui.common

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Composable
inline fun <reified T : ViewModel> activityViewModel(): T {
    val activity = LocalContext.current as ComponentActivity
    return remember(activity) { ViewModelProvider(activity)[T::class.java] }
}