package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.FileItemEntity
import com.example.ui.theme.EmeraldTertiaryDark

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun FileItemRow(
  item: FileItemEntity,
  onClick: () -> Unit,
  onToggleFavorite: () -> Unit,
  onRename: () -> Unit,
  onMoveToVault: () -> Unit,
  onExtractFromVault: () -> Unit,
  onDelete: () -> Unit,
  onRestore: () -> Unit,
  onShowDetails: () -> Unit,
  onSetHighlight: () -> Unit = {},
  onExtractZip: () -> Unit = {},
  onCompressToZip: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var showMenu by remember { mutableStateOf(false) }
  val (icon, iconColor) = FileFormatUtils.getFileIconAndColor(item)

  val highlightColor = when (item.highlightColor?.uppercase()) {
    "YELLOW" -> androidx.compose.ui.graphics.Color(0xFFF59E0B)
    "RED" -> androidx.compose.ui.graphics.Color(0xFFEF4444)
    "GREEN" -> androidx.compose.ui.graphics.Color(0xFF10B981)
    "BLUE" -> androidx.compose.ui.graphics.Color(0xFF3B82F6)
    "PURPLE" -> androidx.compose.ui.graphics.Color(0xFF8B5CF6)
    else -> null
  }

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    border = if (highlightColor != null) BorderStroke(1.8.dp, highlightColor) else null,
    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 4.dp)
      .combinedClickable(
        onClick = onClick,
        onLongClick = { showMenu = true }
      )
      .testTag("file_item_row_${item.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Icon Box with status badge
      Box {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(iconColor.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
          )
        }

        if (item.isInVault || item.isEncrypted) {
          Box(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .size(16.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = "Encrypted",
              tint = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.size(10.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      // Content Column
      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = item.name,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
          )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = if (item.isDirectory) "Folder" else FileFormatUtils.formatFileSize(item.sizeBytes),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "•",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
          )
          Text(
            text = FileFormatUtils.formatDate(item.updatedAt),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Tags flow row
        if (item.ocrTags.isNotBlank()) {
          Spacer(modifier = Modifier.height(4.dp))
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
          ) {
            item.ocrTags.split(",").filter { it.isNotBlank() }.take(3).forEach { tag ->
              SuggestionChip(
                onClick = {},
                label = { Text(tag.trim(), style = MaterialTheme.typography.labelSmall) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                  containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp),
                border = null,
                modifier = Modifier.height(22.dp)
              )
            }
          }
        }
      }

      // Actions
      if (!item.isTrash) {
        IconButton(
          onClick = onToggleFavorite,
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
            contentDescription = "Star",
            tint = if (item.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Box {
        IconButton(
          onClick = { showMenu = true },
          modifier = Modifier
            .size(36.dp)
            .testTag("file_options_${item.id}")
        ) {
          Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Options",
            modifier = Modifier.size(20.dp)
          )
        }

        DropdownMenu(
          expanded = showMenu,
          onDismissRequest = { showMenu = false }
        ) {
          if (item.isTrash) {
            DropdownMenuItem(
              text = { Text("Restore") },
              leadingIcon = { Icon(Icons.Default.RestoreFromTrash, null) },
              onClick = {
                showMenu = false
                onRestore()
              }
            )
            DropdownMenuItem(
              text = { Text("Delete Permanently") },
              leadingIcon = { Icon(Icons.Default.Delete, null) },
              onClick = {
                showMenu = false
                onDelete()
              }
            )
          } else {
            DropdownMenuItem(
              text = { Text("Rename") },
              leadingIcon = { Icon(Icons.Default.Edit, null) },
              onClick = {
                showMenu = false
                onRename()
              }
            )
            if (item.isInVault) {
              DropdownMenuItem(
                text = { Text("Extract from Safe Vault") },
                leadingIcon = { Icon(Icons.Default.LockOpen, null) },
                onClick = {
                  showMenu = false
                  onExtractFromVault()
                }
              )
            } else {
              DropdownMenuItem(
                text = { Text("Move to Encrypted Safe") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                onClick = {
                  showMenu = false
                  onMoveToVault()
                }
              )
            }
            DropdownMenuItem(
              text = { Text("Highlight Tag Color") },
              leadingIcon = { Icon(Icons.Default.Palette, null) },
              onClick = {
                showMenu = false
                onSetHighlight()
              }
            )
            if (item.name.endsWith(".zip", ignoreCase = true) || item.extension.equals("zip", ignoreCase = true)) {
              DropdownMenuItem(
                text = { Text("Extract ZIP") },
                leadingIcon = { Icon(Icons.Default.Unarchive, null) },
                onClick = {
                  showMenu = false
                  onExtractZip()
                }
              )
            } else if (!item.isDirectory) {
              DropdownMenuItem(
                text = { Text("Compress to ZIP") },
                leadingIcon = { Icon(Icons.Default.Archive, null) },
                onClick = {
                  showMenu = false
                  onCompressToZip()
                }
              )
            }
            DropdownMenuItem(
              text = { Text("Details & Checksum") },
              leadingIcon = { Icon(Icons.Default.Info, null) },
              onClick = {
                showMenu = false
                onShowDetails()
              }
            )
            DropdownMenuItem(
              text = { Text("Move to Recycle Bin") },
              leadingIcon = { Icon(Icons.Default.Delete, null) },
              onClick = {
                showMenu = false
                onDelete()
              }
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileItemGrid(
  item: FileItemEntity,
  onClick: () -> Unit,
  onShowDetails: () -> Unit,
  modifier: Modifier = Modifier
) {
  val (icon, iconColor) = FileFormatUtils.getFileIconAndColor(item)

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
    modifier = modifier
      .padding(6.dp)
      .combinedClickable(
        onClick = onClick,
        onLongClick = onShowDetails
      )
      .testTag("file_item_grid_${item.id}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Box(
        modifier = Modifier
          .size(54.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(iconColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = iconColor,
          modifier = Modifier.size(30.dp)
        )
        if (item.isInVault || item.isEncrypted) {
          Box(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .size(16.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = "Encrypted",
              tint = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.size(10.dp)
            )
          }
        }
      }

      Text(
        text = item.name,
        style = MaterialTheme.typography.titleSmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Text(
        text = if (item.isDirectory) "Folder" else FileFormatUtils.formatFileSize(item.sizeBytes),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
