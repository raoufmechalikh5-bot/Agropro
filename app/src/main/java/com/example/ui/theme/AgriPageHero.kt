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

@Composable
fun Modifier.agriAmbientBackground(): Modifier {
    val scheme = MaterialTheme.colorScheme
    val top = if (scheme.background.red < 0.2f) {
        androidx.compose.ui.graphics.Color(0xFF101B14)
    } else {
        androidx.compose.ui.graphics.Color(0xFFE5EFE3)
    }
    val middle = scheme.background
    val bottom = if (scheme.background.red < 0.2f) {
        androidx.compose.ui.graphics.Color(0xFF0C1510)
    } else {
        androidx.compose.ui.graphics.Color(0xFFF5F7F0)
    }
    return this
        .background(Brush.verticalGradient(listOf(top, middle, bottom)))
        .drawBehind {
            drawCircle(
                color = scheme.primary.copy(alpha = if (scheme.background.red < 0.2f) 0.15f else 0.08f),
                radius = size.minDimension * 0.72f,
                center = Offset(size.width * 1.04f, size.height * 0.10f)
            )
            drawCircle(
                color = scheme.tertiary.copy(alpha = if (scheme.background.red < 0.2f) 0.10f else 0.09f),
                radius = size.minDimension * 0.58f,
                center = Offset(size.width * -0.12f, size.height * 0.68f)
            )
        }
}
