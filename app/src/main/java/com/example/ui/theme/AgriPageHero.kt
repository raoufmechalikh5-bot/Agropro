package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AgriPageHero(
    eyebrow: String,
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        androidx.compose.ui.graphics.Color(0xFF183D2A),
                        androidx.compose.ui.graphics.Color(0xFF397A50),
                        androidx.compose.ui.graphics.Color(0xFF101B14)
                    )
                )
            )
            .padding(horizontal = 17.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = eyebrow,
                style = MaterialTheme.typography.labelMedium,
                color = androidx.compose.ui.graphics.Color(0xFF9BCB78),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.82f)
            )
        }
        Spacer(Modifier.width(12.dp))
        Surface(
            color = androidx.compose.ui.graphics.Color(0xFF9BCB78).copy(alpha = 0.18f),
            shape = CircleShape,
            modifier = Modifier.size(54.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color(0xFF9BCB78),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

enum class AgriBackgroundStyle {
    CALCULATOR,
    FERTILIZER,
    ENCYCLOPEDIA,
    NOTEBOOK,
    WEATHER,
    LIVE
}

@Composable
fun Modifier.agriAmbientBackground(
    style: AgriBackgroundStyle = AgriBackgroundStyle.CALCULATOR
): Modifier {
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.background.red < 0.2f
    val palette = when (style) {
        AgriBackgroundStyle.CALCULATOR -> if (dark) listOf(Color(0xFF14251C), Color(0xFF172A20), Color(0xFF101B15)) else listOf(Color(0xFFE2EFE7), Color(0xFFEDF4EE), Color(0xFFF8FAF5))
        AgriBackgroundStyle.FERTILIZER -> if (dark) listOf(Color(0xFF282719), Color(0xFF20291D), Color(0xFF151D14)) else listOf(Color(0xFFECEBD9), Color(0xFFF1F1E5), Color(0xFFFAF8EF))
        AgriBackgroundStyle.ENCYCLOPEDIA -> if (dark) listOf(Color(0xFF14271F), Color(0xFF172A22), Color(0xFF101B16)) else listOf(Color(0xFFF1EBDD), Color(0xFFF7F3E9), Color(0xFFFCFAF4))
        AgriBackgroundStyle.NOTEBOOK -> if (dark) listOf(Color(0xFF29251B), Color(0xFF24271D), Color(0xFF171D15)) else listOf(Color(0xFFECE6D6), Color(0xFFF3F0E5), Color(0xFFFAF8F0))
        AgriBackgroundStyle.WEATHER -> if (dark) listOf(Color(0xFF172832), Color(0xFF192A32), Color(0xFF111D24)) else listOf(Color(0xFFDCECF5), Color(0xFFEDF5F8), Color(0xFFF8FBFC))
        AgriBackgroundStyle.LIVE -> if (dark) listOf(Color(0xFF14291F), Color(0xFF172B22), Color(0xFF101B15)) else listOf(Color(0xFFDDEFE5), Color(0xFFECF5EE), Color(0xFFF7FAF5))
    }
    val accent = when (style) {
        AgriBackgroundStyle.CALCULATOR -> Color(0xFF78A9A0)
        AgriBackgroundStyle.FERTILIZER -> Color(0xFFB49A62)
        AgriBackgroundStyle.ENCYCLOPEDIA -> Color(0xFF70A77C)
        AgriBackgroundStyle.NOTEBOOK -> Color(0xFFB39A70)
        AgriBackgroundStyle.WEATHER -> Color(0xFF78B7D5)
        AgriBackgroundStyle.LIVE -> Color(0xFF72B995)
    }
    return this
        .background(Brush.verticalGradient(palette))
        .drawBehind {
            val alpha = if (dark) 0.10f else 0.16f
            when (style) {
                AgriBackgroundStyle.CALCULATOR -> {
                    val step = 34.dp.toPx()
                    var x = 0f
                    while (x <= size.width) {
                        drawLine(accent.copy(alpha = alpha * 0.42f), Offset(x, 0f), Offset(x, size.height), 1.dp.toPx())
                        x += step
                    }
                    var y = 0f
                    while (y <= size.height) {
                        drawLine(accent.copy(alpha = alpha * 0.42f), Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                        y += step
                    }
                    drawCircle(accent.copy(alpha = alpha), size.minDimension * 0.30f, Offset(size.width * 0.94f, size.height * 0.12f))
                }
                AgriBackgroundStyle.FERTILIZER -> {
                    drawCircle(accent.copy(alpha = alpha * 0.85f), size.minDimension * 0.36f, Offset(size.width * 1.02f, size.height * 0.82f))
                    drawCircle(accent.copy(alpha = alpha * 0.65f), size.minDimension * 0.24f, Offset(size.width * 1.02f, size.height * 0.82f))
                    drawCircle(scheme.primary.copy(alpha = alpha * 0.55f), size.minDimension * 0.16f, Offset(size.width * -0.08f, size.height * 0.40f))
                }
                AgriBackgroundStyle.ENCYCLOPEDIA -> {
                    // Keep the encyclopedia background clean so cards and controls stand apart.
                }
                AgriBackgroundStyle.NOTEBOOK -> {
                    val step = 29.dp.toPx()
                    var y = 24.dp.toPx()
                    while (y < size.height) {
                        drawLine(accent.copy(alpha = alpha * 0.48f), Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                        y += step
                    }
                    drawCircle(accent.copy(alpha = alpha * 0.8f), size.minDimension * 0.22f, Offset(size.width * 0.96f, size.height * 0.12f))
                }
                AgriBackgroundStyle.WEATHER -> {
                    drawCircle(accent.copy(alpha = alpha), size.minDimension * 0.34f, Offset(size.width * 0.92f, size.height * 0.10f))
                    drawCircle(Color.White.copy(alpha = if (dark) 0.035f else 0.32f), size.minDimension * 0.25f, Offset(size.width * 0.78f, size.height * 0.16f))
                    drawCircle(accent.copy(alpha = alpha * 0.65f), size.minDimension * 0.27f, Offset(size.width * 0.08f, size.height * 0.78f))
                }
                AgriBackgroundStyle.LIVE -> {
                    drawCircle(accent.copy(alpha = alpha), size.minDimension * 0.38f, Offset(size.width * 0.94f, size.height * 0.16f))
                    drawCircle(accent.copy(alpha = alpha * 0.65f), size.minDimension * 0.26f, Offset(size.width * 0.06f, size.height * 0.78f))
                }
            }
        }
}
