package com.yaksperm.analyzer.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import com.yaksperm.analyzer.presentation.theme.Border
import com.yaksperm.analyzer.presentation.theme.CardBg
import com.yaksperm.analyzer.presentation.theme.TextDim
import com.yaksperm.analyzer.presentation.theme.TextPrimary

/**
 * A card row showing one CASA parameter: colored icon + full name + value + unit.
 */
@Composable
fun CasaParameterCard(
    abbreviation: String,
    fullName: String,
    value: Float,
    unit: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon box
        androidx.compose.foundation.layout.Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(accentColor.copy(alpha = 0.15f))
        ) {
            Text(
                text = abbreviation,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Name
        Column(modifier = Modifier.weight(1f)) {
            Text(text = abbreviation, color = accentColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(text = fullName, style = MaterialTheme.typography.bodySmall, color = TextDim)
        }

        // Value
        Text(
            text = "%.1f".format(value),
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = unit,
            style = MaterialTheme.typography.labelSmall,
            color = TextDim,
            modifier = Modifier.align(Alignment.Bottom).padding(bottom = 2.dp)
        )
    }
}
