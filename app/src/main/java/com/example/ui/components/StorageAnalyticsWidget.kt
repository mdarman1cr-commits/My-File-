package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.StorageBreakdown
import com.example.ui.theme.ColorDocument
import com.example.ui.theme.ColorImage
import com.example.ui.theme.ColorScan
import com.example.ui.theme.ColorVault

@Composable
fun StorageAnalyticsWidget(
  breakdown: StorageBreakdown,
  modifier: Modifier = Modifier
) {
  val usedFormatted = FileFormatUtils.formatFileSize(breakdown.usedBytes)
  val totalFormatted = FileFormatUtils.formatFileSize(breakdown.totalCapacityBytes)
  val freeFormatted = FileFormatUtils.formatFileSize(breakdown.freeBytes)

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    ),
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .testTag("storage_analytics_widget")
  ) {
    Column(
      modifier = Modifier.padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Storage,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
          }
          Column {
            Text(
              text = "Device Storage",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "$freeFormatted available",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Text(
          text = "$usedFormatted / $totalFormatted",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.primary
        )
      }

      // Multi-segment progress bar
      val total = breakdown.totalCapacityBytes.toFloat().coerceAtLeast(1f)
      val docFraction = (breakdown.documentBytes.toFloat() / total).coerceIn(0.01f, 0.4f)
      val imgFraction = (breakdown.imageBytes.toFloat() / total).coerceIn(0.01f, 0.4f)
      val scanFraction = (breakdown.scanBytes.toFloat() / total).coerceIn(0.01f, 0.4f)
      val vaultFraction = (breakdown.vaultBytes.toFloat() / total).coerceIn(0.01f, 0.3f)

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(10.dp)
          .clip(RoundedCornerShape(5.dp))
          .background(MaterialTheme.colorScheme.surface)
      ) {
        Row(modifier = Modifier.fillMaxHeight()) {
          Box(
            modifier = Modifier
              .weight(docFraction)
              .fillMaxHeight()
              .background(ColorDocument)
          )
          Box(
            modifier = Modifier
              .weight(imgFraction)
              .fillMaxHeight()
              .background(ColorImage)
          )
          Box(
            modifier = Modifier
              .weight(scanFraction)
              .fillMaxHeight()
              .background(ColorScan)
          )
          Box(
            modifier = Modifier
              .weight(vaultFraction)
              .fillMaxHeight()
              .background(ColorVault)
          )
          val remaining = (1f - (docFraction + imgFraction + scanFraction + vaultFraction)).coerceAtLeast(0.05f)
          Box(
            modifier = Modifier
              .weight(remaining)
              .fillMaxHeight()
              .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
          )
        }
      }

      // Legend breakdown
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        StorageLegendItem("Docs", FileFormatUtils.formatFileSize(breakdown.documentBytes), ColorDocument)
        StorageLegendItem("Images", FileFormatUtils.formatFileSize(breakdown.imageBytes), ColorImage)
        StorageLegendItem("Scans", FileFormatUtils.formatFileSize(breakdown.scanBytes), ColorScan)
        StorageLegendItem("Safe Vault", FileFormatUtils.formatFileSize(breakdown.vaultBytes), ColorVault)
      }
    }
  }
}

@Composable
private fun StorageLegendItem(
  label: String,
  size: String,
  color: Color
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Box(
      modifier = Modifier
        .size(8.dp)
        .clip(CircleShape)
        .background(color)
    )
    Column {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Text(
        text = size,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}
