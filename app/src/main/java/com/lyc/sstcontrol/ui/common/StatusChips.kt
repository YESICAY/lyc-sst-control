package com.lyc.sstcontrol.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lyc.sstcontrol.R
import com.lyc.sstcontrol.data.ItemStatus
import com.lyc.sstcontrol.ui.theme.OnMint
import com.lyc.sstcontrol.ui.theme.StatusApproved
import com.lyc.sstcontrol.ui.theme.StatusPending
import com.lyc.sstcontrol.ui.theme.StatusRisk

fun ItemStatus.labelRes(): Int = when (this) {
    ItemStatus.GOOD -> R.string.form_status_good
    ItemStatus.REGULAR -> R.string.form_status_regular
    ItemStatus.BAD -> R.string.form_status_bad
}

fun ItemStatus.containerColor(): Color = when (this) {
    ItemStatus.GOOD -> StatusApproved
    ItemStatus.REGULAR -> StatusPending
    ItemStatus.BAD -> StatusRisk
}

fun ItemStatus.contentColor(): Color = if (this == ItemStatus.BAD) Color.White else OnMint

@Composable
private fun Pill(text: String, container: Color, content: Color, modifier: Modifier) {
    Surface(shape = RoundedCornerShape(50), color = container, modifier = modifier) {
        Text(
            text = text,
            color = content,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun StatusChip(status: String, modifier: Modifier = Modifier) {
    when (status) {
        "APPROVED" -> Pill(stringResource(R.string.status_approved), StatusApproved, OnMint, modifier)
        "RISK" -> Pill(stringResource(R.string.status_risk), StatusRisk, Color.White, modifier)
        else -> Pill(stringResource(R.string.status_pending), StatusPending, OnMint, modifier)
    }
}

@Composable
fun ItemStatusChip(status: ItemStatus, modifier: Modifier = Modifier) {
    Pill(stringResource(status.labelRes()), status.containerColor(), status.contentColor(), modifier)
}