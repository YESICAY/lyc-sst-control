package com.lyc.sstcontrol.ui.history

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lyc.sstcontrol.R
import com.lyc.sstcontrol.data.Inspection
import com.lyc.sstcontrol.data.UserProfile
import com.lyc.sstcontrol.data.UserRole
import com.lyc.sstcontrol.ui.common.StatusChip
import com.lyc.sstcontrol.ui.common.TypeChip
import com.lyc.sstcontrol.ui.common.activityViewModel
import com.lyc.sstcontrol.ui.common.appTextFieldColors
import com.lyc.sstcontrol.ui.common.formatAssignedAt
import com.lyc.sstcontrol.ui.common.labelRes

@Composable
fun HistoryScreen(profile: UserProfile, modifier: Modifier = Modifier) {
    val viewModel = activityViewModel<HistoryViewModel>()
    var selected by remember { mutableStateOf<Inspection?>(null) }

    DisposableEffect(profile.uid) {
        viewModel.start(profile.role == UserRole.ADMIN, profile.uid)
        onDispose { viewModel.stop() }
    }

    val current = selected
    if (current != null) {
        BackHandler { selected = null }
        InspectionDetailScreen(
            inspection = current,
            onBack = { selected = null },
            modifier = modifier
        )
    } else {
        HistoryList(viewModel = viewModel, onOpen = { selected = it }, modifier = modifier)
    }
}

@Composable
private fun HistoryList(
    viewModel: HistoryViewModel,
    onOpen: (Inspection) -> Unit,
    modifier: Modifier
) {
    val filters = listOf<Pair<String?, Int>>(
        null to R.string.history_filter_all,
        "PENDING" to R.string.status_pending,
        "APPROVED" to R.string.status_approved,
        "RISK" to R.string.status_risk
    )
    val visible = remember(viewModel.items, viewModel.query, viewModel.statusFilter) {
        val q = viewModel.query.trim().lowercase()
        viewModel.items.filter { item ->
            (viewModel.statusFilter == null || item.status == viewModel.statusFilter) &&
                    (q.isEmpty() ||
                            item.inspectorName.lowercase().contains(q) ||
                            item.location.lowercase().contains(q))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = stringResource(R.string.history_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = viewModel.query,
            onValueChange = viewModel::onQueryChange,
            placeholder = { Text(stringResource(R.string.history_search)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            colors = appTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { (value, labelRes) ->
                FilterChip(
                    selected = viewModel.statusFilter == value,
                    onClick = { viewModel.onFilterSelected(value) },
                    label = { Text(stringResource(labelRes)) }
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        when {
            viewModel.isLoading -> CircularProgressIndicator()
            visible.isEmpty() -> Text(
                text = stringResource(R.string.history_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(visible, key = { it.id }) { inspection ->
                    InspectionCard(inspection = inspection, onClick = { onOpen(inspection) })
                }
            }
        }
    }
}

@Composable
private fun InspectionCard(inspection: Inspection, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TypeChip(type = inspection.type)
                StatusChip(status = inspection.status)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(inspection.type.labelRes()) + " - " + inspection.location,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(text = inspection.inspectorName, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = formatAssignedAt(inspection.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.history_detail),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}