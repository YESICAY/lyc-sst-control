package com.lyc.sstcontrol.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lyc.sstcontrol.R
import com.lyc.sstcontrol.data.InspectionType
import com.lyc.sstcontrol.ui.theme.OnMint
import com.lyc.sstcontrol.ui.theme.TypeElectrical
import com.lyc.sstcontrol.ui.theme.TypeEpp
import com.lyc.sstcontrol.ui.theme.TypeHeights

fun InspectionType.labelRes(): Int = when (this) {
    InspectionType.EPP -> R.string.type_epp
    InspectionType.HEIGHTS -> R.string.type_heights
    InspectionType.ELECTRICAL -> R.string.type_electrical
}

@Composable
fun TypeChip(type: InspectionType, modifier: Modifier = Modifier) {
    val color = when (type) {
        InspectionType.EPP -> TypeEpp
        InspectionType.HEIGHTS -> TypeHeights
        InspectionType.ELECTRICAL -> TypeElectrical
    }
    Surface(shape = RoundedCornerShape(50), color = color, modifier = modifier) {
        Text(
            text = stringResource(type.labelRes()),
            color = OnMint,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}