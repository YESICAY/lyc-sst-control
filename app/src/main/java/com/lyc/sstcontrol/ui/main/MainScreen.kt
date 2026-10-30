package com.lyc.sstcontrol.ui.main

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lyc.sstcontrol.R
import com.lyc.sstcontrol.data.Inspection
import com.lyc.sstcontrol.data.UserProfile
import com.lyc.sstcontrol.data.UserRole
import com.lyc.sstcontrol.ui.admin.AssignScreen
import com.lyc.sstcontrol.ui.admin.CreateUserScreen
import com.lyc.sstcontrol.ui.common.activityViewModel
import com.lyc.sstcontrol.ui.history.HistoryScreen
import com.lyc.sstcontrol.ui.inspector.InboxScreen
import com.lyc.sstcontrol.ui.inspector.InspectionFormScreen
import com.lyc.sstcontrol.ui.inspector.InspectionFormViewModel
import com.lyc.sstcontrol.ui.theme.StatusBarIcons

private data class TabItem(@StringRes val label: Int, val icon: ImageVector)

private val adminTabs = listOf(
    TabItem(R.string.nav_assign, Icons.AutoMirrored.Outlined.Assignment),
    TabItem(R.string.nav_create, Icons.Outlined.PersonAdd),
    TabItem(R.string.nav_history, Icons.Outlined.History)
)

private val inspectorTabs = listOf(
    TabItem(R.string.nav_task, Icons.AutoMirrored.Outlined.Assignment),
    TabItem(R.string.nav_history, Icons.Outlined.History)
)

@Composable
fun MainScreen(profile: UserProfile, onSignOut: () -> Unit) {
    val formViewModel = activityViewModel<InspectionFormViewModel>()
    val activeInspection = formViewModel.inspection

    if (activeInspection != null) {
        InspectionFormScreen(inspection = activeInspection)
    } else {
        MainScaffold(
            profile = profile,
            onSignOut = onSignOut,
            onStartInspection = formViewModel::start
        )
    }
}

@Composable
private fun MainScaffold(
    profile: UserProfile,
    onSignOut: () -> Unit,
    onStartInspection: (Inspection) -> Unit
) {
    StatusBarIcons(darkIcons = false)

    val isAdmin = profile.role == UserRole.ADMIN
    val tabs = if (isAdmin) adminTabs else inspectorTabs
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    val selected = selectedIndex.coerceIn(0, tabs.lastIndex)

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(if (isAdmin) R.string.role_admin else R.string.role_inspector),
                initials = initialsOf(profile.fullName),
                containerColor = if (isAdmin) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.tertiary
                },
                onSignOut = onSignOut
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = index == selected,
                        onClick = { selectedIndex = index },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(stringResource(tab.label)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when {
            isAdmin && selected == 0 -> AssignScreen(adminUid = profile.uid, modifier = contentModifier)
            isAdmin && selected == 1 -> CreateUserScreen(modifier = contentModifier)
            isAdmin && selected == 2 -> HistoryScreen(profile = profile, modifier = contentModifier)
            !isAdmin && selected == 0 -> InboxScreen(
                profile = profile,
                onStartInspection = onStartInspection,
                modifier = contentModifier
            )
            else -> HistoryScreen(profile = profile, modifier = contentModifier)
        }
    }
}

@Composable
private fun AppTopBar(
    title: String,
    initials: String,
    containerColor: Color,
    onSignOut: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Box {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { menuOpen = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.sign_out)) },
                    onClick = {
                        menuOpen = false
                        onSignOut()
                    }
                )
            }
        }
    }
}

private fun initialsOf(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotEmpty() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
    }
}