package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.Breadcrumb

@Composable
fun BreadcrumbBar(
  breadcrumbs: List<Breadcrumb>,
  onNavigateToBreadcrumb: (Breadcrumb) -> Unit,
  onNavigateUp: () -> Unit,
  canNavigateUp: Boolean,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    if (canNavigateUp) {
      IconButton(
        onClick = onNavigateUp,
        modifier = Modifier
          .size(36.dp)
          .testTag("navigate_up_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Navigate Up",
          tint = MaterialTheme.colorScheme.primary
        )
      }
    }

    Row(
      modifier = Modifier
        .weight(1f)
        .horizontalScroll(rememberScrollState()),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      breadcrumbs.forEachIndexed { index, crumb ->
        val isLast = index == breadcrumbs.size - 1

        AssistChip(
          onClick = { onNavigateToBreadcrumb(crumb) },
          label = {
            Text(
              text = crumb.title,
              style = if (isLast) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodySmall,
              color = if (isLast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          },
          leadingIcon = if (index == 0) {
            {
              Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
              )
            }
          } else null,
          colors = AssistChipDefaults.assistChipColors(
            containerColor = if (isLast) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
          ),
          border = null,
          modifier = Modifier.testTag("breadcrumb_${crumb.title}")
        )

        if (!isLast) {
          Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.outline
          )
        }
      }
    }
  }
}
