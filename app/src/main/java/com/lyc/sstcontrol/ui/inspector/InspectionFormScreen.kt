package com.lyc.sstcontrol.ui.inspector

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.lyc.sstcontrol.R
import com.lyc.sstcontrol.data.ChecklistItem
import com.lyc.sstcontrol.data.Inspection
import com.lyc.sstcontrol.data.ItemStatus
import com.lyc.sstcontrol.data.LocationHelper
import com.lyc.sstcontrol.data.PhotoUtils
import com.lyc.sstcontrol.ui.common.activityViewModel
import com.lyc.sstcontrol.ui.common.appTextFieldColors
import com.lyc.sstcontrol.ui.common.containerColor
import com.lyc.sstcontrol.ui.common.contentColor
import com.lyc.sstcontrol.ui.common.labelRes
import com.lyc.sstcontrol.ui.theme.StatusBarIcons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

@Composable
fun InspectionFormScreen(inspection: Inspection, modifier: Modifier = Modifier) {
    StatusBarIcons(darkIcons = false)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel = activityViewModel<InspectionFormViewModel>()
    val locationHelper = remember { LocationHelper(context) }

    BackHandler { viewModel.onBack() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.any { it }) {
            locationHelper.getCurrent { viewModel.onGpsStart(it) }
        } else {
            viewModel.onGpsStart(null)
        }
    }
    LaunchedEffect(inspection.id) {
        if (viewModel.gpsStart == null) {
            if (hasLocationPermission(context)) {
                locationHelper.getCurrent { viewModel.onGpsStart(it) }
            } else {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val itemId = viewModel.pendingPhotoItemId
        val file = viewModel.pendingPhotoFile
        viewModel.pendingPhotoItemId = null
        viewModel.pendingPhotoFile = null
        if (success && itemId != null && file != null) {
            scope.launch(Dispatchers.Default) {
                val data = PhotoUtils.compressToBase64(file)
                withContext(Dispatchers.Main) {
                    if (data != null) viewModel.onPhotoTaken(itemId, data) else viewModel.onPhotoFailed()
                }
            }
        }
    }
    val takePhoto: (String) -> Unit = { itemId ->
        val file = PhotoUtils.createTempImageFile(context)
        viewModel.pendingPhotoItemId = itemId
        viewModel.pendingPhotoFile = file
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        cameraLauncher.launch(uri)
    }

    val sections = viewModel.sections
    val isSummary = viewModel.step >= sections.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        FormTopBar(
            title = stringResource(
                R.string.form_title_format,
                stringResource(inspection.type.labelRes())
            ),
            onBack = viewModel::onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            ProgressHeader(
                percent = viewModel.progressPercent,
                step = viewModel.step,
                total = viewModel.totalSteps
            )
            Spacer(Modifier.height(16.dp))

            if (!isSummary) {
                val section = sections[viewModel.step]
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))

                section.items.forEach { item ->
                    ItemCard(
                        item = item,
                        draft = viewModel.drafts[item.id] ?: ItemDraft(),
                        onStatus = { viewModel.onStatusSelected(item.id, it) },
                        onNote = { viewModel.onNoteChange(item.id, it) },
                        onPhotoClick = { takePhoto(item.id) }
                    )
                    Spacer(Modifier.height(12.dp))
                }

                if (viewModel.showIncompleteError) {
                    Text(
                        text = stringResource(R.string.form_error_incomplete),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))
                }
                if (viewModel.photoError) {
                    Text(
                        text = stringResource(R.string.form_photo_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))
                }

                Button(
                    onClick = viewModel::onNext,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = stringResource(R.string.form_next),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                val (good, regular, bad) = viewModel.counts()
                val gps = viewModel.gpsStart

                Text(
                    text = stringResource(R.string.form_summary_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.form_summary_counts, good, regular, bad),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = when {
                        gps != null -> stringResource(R.string.form_gps_ok)
                        viewModel.gpsPending -> stringResource(R.string.form_gps_waiting)
                        else -> stringResource(R.string.form_gps_missing)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (gps == null && !viewModel.gpsPending) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = viewModel.generalNotes,
                    onValueChange = viewModel::onGeneralNotesChange,
                    label = { Text(stringResource(R.string.form_general_notes)) },
                    minLines = 3,
                    colors = appTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        viewModel.beginSubmit()
                        if (hasLocationPermission(context)) {
                            locationHelper.getCurrent { viewModel.submit(it) }
                        } else {
                            viewModel.submit(null)
                        }
                    },
                    enabled = !viewModel.isSubmitting,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (viewModel.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.form_submit),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (viewModel.step > 0) {
                TextButton(
                    onClick = viewModel::onBack,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(stringResource(R.string.form_back))
                }
            }
            Spacer(Modifier.height(24.dp).navigationBarsPadding())
        }
    }

    if (viewModel.finished) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.form_sent_title)) },
            text = { Text(stringResource(R.string.form_sent)) },
            confirmButton = {
                TextButton(onClick = viewModel::finish) {
                    Text(stringResource(R.string.form_close))
                }
            }
        )
    }
}

@Composable
private fun FormTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.tertiary)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.detail_back),
                tint = Color.White
            )
        }
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ProgressHeader(percent: Int, step: Int, total: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.form_progress, percent),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = stringResource(R.string.form_step, step + 1, total),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
    Spacer(Modifier.height(8.dp))
    LinearProgressIndicator(
        progress = { percent / 100f },
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
    )
}

@Composable
private fun ItemCard(
    item: ChecklistItem,
    draft: ItemDraft,
    onStatus: (ItemStatus) -> Unit,
    onNote: (String) -> Unit,
    onPhotoClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ItemStatus.entries.forEach { status ->
                    FilterChip(
                        selected = draft.status == status,
                        onClick = { onStatus(status) },
                        label = { Text(stringResource(status.labelRes())) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = status.containerColor(),
                            selectedLabelColor = status.contentColor()
                        )
                    )
                }
            }

            if (draft.status == ItemStatus.REGULAR || draft.status == ItemStatus.BAD) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = draft.note,
                    onValueChange = onNote,
                    label = { Text(stringResource(R.string.form_note_hint)) },
                    minLines = 2,
                    colors = appTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                PhotoRow(photo = draft.photo, onClick = onPhotoClick)
            }
        }
    }
}

@Composable
private fun PhotoRow(photo: String?, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Outlined.PhotoCamera,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(
                    if (photo == null) R.string.form_photo_add else R.string.form_photo_retake
                )
            )
        }
        if (photo != null) {
            Spacer(Modifier.width(12.dp))
            val bitmap = remember(photo) { PhotoUtils.base64ToBitmap(photo, 4)?.asImageBitmap() }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
    }
}