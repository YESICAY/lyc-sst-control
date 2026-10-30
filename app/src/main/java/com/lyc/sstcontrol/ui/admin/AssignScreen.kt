package com.lyc.sstcontrol.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.lyc.sstcontrol.R
import com.lyc.sstcontrol.data.AssignError
import com.lyc.sstcontrol.data.Inspection
import com.lyc.sstcontrol.data.InspectionType
import com.lyc.sstcontrol.ui.common.PickerField
import com.lyc.sstcontrol.ui.common.SelectField
import com.lyc.sstcontrol.ui.common.TypeChip
import com.lyc.sstcontrol.ui.common.activityViewModel
import com.lyc.sstcontrol.ui.common.appTextFieldColors
import com.lyc.sstcontrol.ui.common.dateFormatter
import com.lyc.sstcontrol.ui.common.formatAssignedAt
import com.lyc.sstcontrol.ui.common.labelRes
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun AssignScreen(adminUid: String, modifier: Modifier = Modifier) {
    val viewModel = activityViewModel<AssignViewModel>()
    var showDatePicker by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        viewModel.startListening()
        onDispose { viewModel.stopListening() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.assign_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.assign_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (!viewModel.inspectorsLoading && viewModel.inspectors.isEmpty()) {
                    Text(
                        text = stringResource(R.string.assign_no_inspectors),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                }

                SelectField(
                    label = stringResource(R.string.assign_inspector),
                    valueText = viewModel.selectedInspector?.fullName.orEmpty(),
                    options = viewModel.inspectors,
                    optionLabel = { it.fullName },
                    onSelect = viewModel::onInspectorSelected,
                    enabled = !viewModel.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                SelectField(
                    label = stringResource(R.string.assign_type),
                    valueText = viewModel.selectedType
                        ?.let { stringResource(it.labelRes()) }
                        .orEmpty(),
                    options = InspectionType.entries,
                    optionLabel = { stringResource(it.labelRes()) },
                    onSelect = viewModel::onTypeSelected,
                    enabled = !viewModel.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = viewModel.location,
                    onValueChange = viewModel::onLocationChange,
                    label = { Text(stringResource(R.string.assign_location)) },
                    singleLine = true,
                    enabled = !viewModel.isLoading,
                    colors = appTextFieldColors(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                PickerField(
                    label = stringResource(R.string.assign_due_date),
                    valueText = viewModel.dueDate?.format(dateFormatter).orEmpty(),
                    icon = Icons.Outlined.CalendarMonth,
                    onClick = { showDatePicker = true },
                    enabled = !viewModel.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                viewModel.error?.let { error ->
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(error.messageRes()),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (viewModel.justAssigned) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.assign_success),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { viewModel.onAssignClick(adminUid) },
                    enabled = !viewModel.isLoading,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.assign_button),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        Text(
            text = stringResource(R.string.assign_recent),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        if (viewModel.recent.isEmpty()) {
            Text(
                text = stringResource(R.string.assign_recent_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            viewModel.recent.forEach { inspection ->
                RecentAssignmentCard(inspection)
                Spacer(Modifier.height(10.dp))
            }
        }
    }

    if (showDatePicker) {
        DueDatePickerDialog(
            onDateSelected = viewModel::onDueDateSelected,
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
private fun RecentAssignmentCard(inspection: Inspection) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = inspection.inspectorName, fontWeight = FontWeight.SemiBold)
                Text(
                    text = formatAssignedAt(inspection.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = inspection.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TypeChip(type = inspection.type)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DueDatePickerDialog(onDateSelected: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val state = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val todayUtc = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                return utcTimeMillis >= todayUtc
            }
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { millis ->
                        onDateSelected(
                            Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        )
                    }
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.action_accept))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    ) {
        DatePicker(state = state)
    }
}

private fun AssignError.messageRes(): Int = when (this) {
    AssignError.INCOMPLETE -> R.string.assign_error_incomplete
    AssignError.NO_NETWORK -> R.string.assign_error_no_network
    AssignError.NOT_ALLOWED -> R.string.assign_error_not_allowed
    AssignError.UNKNOWN -> R.string.assign_error_unknown
}