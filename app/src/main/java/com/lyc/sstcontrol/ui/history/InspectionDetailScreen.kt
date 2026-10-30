package com.lyc.sstcontrol.ui.history

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lyc.sstcontrol.R
import com.lyc.sstcontrol.data.DetailResponse
import com.lyc.sstcontrol.data.Inspection
import com.lyc.sstcontrol.data.PhotoUtils
import com.lyc.sstcontrol.ui.common.ItemStatusChip
import com.lyc.sstcontrol.ui.common.StatusChip
import com.lyc.sstcontrol.ui.common.TypeChip
import com.lyc.sstcontrol.ui.common.activityViewModel
import com.lyc.sstcontrol.ui.common.formatAssignedAt
import com.lyc.sstcontrol.ui.common.formatDueDate
import java.util.Locale

@Composable
fun InspectionDetailScreen(
    inspection: Inspection,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = activityViewModel<DetailViewModel>()
    val context = LocalContext.current

    LaunchedEffect(inspection.id, inspection.status) {
        if (inspection.status == "PENDING") viewModel.reset() else viewModel.load(inspection.id)
    }

    val detail = viewModel.detail

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.detail_back)
                )
            }
            Text(
                text = stringResource(R.string.detail_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(12.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
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
                Spacer(Modifier.height(12.dp))
                DetailRow(stringResource(R.string.detail_inspector), inspection.inspectorName)
                DetailRow(stringResource(R.string.detail_location), inspection.location)
                DetailRow(stringResource(R.string.detail_assigned), formatAssignedAt(inspection.createdAt))
                DetailRow(stringResource(R.string.detail_due), formatDueDate(inspection.dueDate))

                if (detail != null) {
                    if (detail.submittedAt != null) {
                        DetailRow(
                            stringResource(R.string.detail_sent),
                            formatAssignedAt(detail.submittedAt)
                        )
                    }
                    val point = detail.gpsEnd ?: detail.gpsStart
                    DetailRow(
                        stringResource(R.string.detail_gps),
                        point?.let {
                            String.format(Locale.US, "%.5f, %.5f (±%.0f m)", it.latitude, it.longitude, it.accuracy)
                        } ?: stringResource(R.string.detail_no_gps)
                    )
                    if (point != null) {
                        TextButton(onClick = { openMap(context, point.latitude, point.longitude) }) {
                            Text(stringResource(R.string.detail_open_map))
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))

        if (inspection.status == "PENDING") {
            Text(
                text = stringResource(R.string.detail_not_submitted),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (viewModel.failed) {
            Text(
                text = stringResource(R.string.detail_loading_error),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        } else if (detail == null) {
            CircularProgressIndicator()
        } else {
            Text(
                text = stringResource(R.string.detail_results),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            detail.responses.forEach { response ->
                ResponseCard(response)
                Spacer(Modifier.height(10.dp))
            }
            if (detail.generalNotes.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.detail_notes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(text = detail.generalNotes, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(Modifier.height(24.dp).navigationBarsPadding())
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ResponseCard(response: DetailResponse) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = response.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                ItemStatusChip(status = response.status)
            }
            if (response.note.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(text = response.note, style = MaterialTheme.typography.bodyMedium)
            }
            if (response.photo != null) {
                Spacer(Modifier.height(10.dp))
                val bitmap = remember(response.photo) {
                    PhotoUtils.base64ToBitmap(response.photo, 2)?.asImageBitmap()
                }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }
        }
    }
}

private fun openMap(context: Context, lat: Double, lng: Double) {
    val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng")
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (e: Exception) {
    }
}