package com.lyc.sstcontrol.ui.inspector

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lyc.sstcontrol.R
import com.lyc.sstcontrol.data.Inspection
import com.lyc.sstcontrol.data.UserProfile
import com.lyc.sstcontrol.ui.common.activityViewModel
import com.lyc.sstcontrol.ui.common.formatDueDate
import com.lyc.sstcontrol.ui.common.labelRes

@Composable
fun InboxScreen(
    profile: UserProfile,
    onStartInspection: (Inspection) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel = activityViewModel<InboxViewModel>()

    DisposableEffect(profile.uid) {
        viewModel.start(profile.uid)
        onDispose { viewModel.stop() }
    }

    val firstName = profile.fullName.trim().split(" ").firstOrNull().orEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.inbox_greeting, firstName),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.inbox_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(20.dp))

        when {
            viewModel.isLoading -> CircularProgressIndicator()
            viewModel.tasks.isEmpty() -> Text(
                text = stringResource(R.string.inbox_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            else -> viewModel.tasks.forEach { task ->
                TaskCard(
                    task = task,
                    onStartClick = { onStartInspection(task) }
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun TaskCard(task: Inspection, onStartClick: () -> Unit) {
    val overdue = task.dueDate?.let { it.seconds * 1000 < System.currentTimeMillis() } == true
    val dueText = formatDueDate(task.dueDate) +
            if (overdue) " (" + stringResource(R.string.inbox_overdue) + ")" else ""

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            InfoRow(
                icon = Icons.Outlined.Description,
                label = stringResource(R.string.inbox_type),
                value = stringResource(task.type.labelRes())
            )
            Spacer(Modifier.height(14.dp))
            InfoRow(
                icon = Icons.Outlined.LocationOn,
                label = stringResource(R.string.inbox_location),
                value = task.location
            )
            Spacer(Modifier.height(14.dp))
            InfoRow(
                icon = Icons.Outlined.CalendarMonth,
                label = stringResource(R.string.inbox_due),
                value = dueText,
                valueColor = if (overdue) MaterialTheme.colorScheme.error else Color.Unspecified
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onStartClick,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = stringResource(R.string.inbox_start),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = valueColor
            )
        }
    }
}