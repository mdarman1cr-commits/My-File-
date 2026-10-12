package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FileCategory
import com.example.ui.components.ColorAudioCategory
import com.example.ui.components.ColorCleanupTool
import com.example.ui.components.ColorDocumentCategory
import com.example.ui.components.ColorDownloadCategory
import com.example.ui.components.ColorImagesCategory
import com.example.ui.components.ColorInternalStorage
import com.example.ui.components.ColorManageOnPc
import com.example.ui.components.ColorOtherStorage
import com.example.ui.components.ColorSafeFolderTool
import com.example.ui.components.ColorScannerStorage
import com.example.ui.components.ColorSdCard
import com.example.ui.components.ColorStarredTool
import com.example.ui.components.ColorVideosCategory
import com.example.ui.components.ColorZipApkCategory
import com.example.ui.components.FilesCleanupDialog
import com.example.ui.components.ManageOnPcDialog
import com.example.ui.components.SdCardStatusDialog
import com.example.ui.components.FileFormatUtils
import com.example.ui.components.StoragePermissionBanner
import com.example.ui.viewmodel.FileExplorerViewModel
import com.example.ui.viewmodel.ScreenDestination

@Composable
fun DashboardScreen(
  viewModel: FileExplorerViewModel,
  onNavigateToFiles: () -> Unit,
  onNavigateToScanner: () -> Unit,
  onNavigateToVault: () -> Unit,
  onNavigateToBackups: () -> Unit,
  onNavigateToSettings: () -> Unit,
  onOpenSearch: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val storageBreakdown by viewModel.storageBreakdown.collectAsStateWithLifecycle()
  var showCleanupDialog by remember { mutableStateOf(false) }
  var showPcDialog by remember { mutableStateOf(false) }
  var showSdCardDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 16.dp)
      .testTag("dashboard_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Top Bar: "My File" with Search and Settings
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 14.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = buildAnnotatedString {
            withStyle(
              SpanStyle(
                color = Color(0xFF1976D2),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
              )
            ) {
              append("My ")
            }
            withStyle(
              SpanStyle(
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp
              )
            ) {
              append("File")
            }
          },
          style = MaterialTheme.typography.titleLarge
        )

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          IconButton(
            onClick = onOpenSearch,
            modifier = Modifier.testTag("top_search_button")
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search",
              tint = MaterialTheme.colorScheme.onBackground,
              modifier = Modifier.size(26.dp)
            )
          }

          IconButton(
            onClick = onNavigateToSettings,
            modifier = Modifier.testTag("top_settings_button")
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "Settings",
              tint = MaterialTheme.colorScheme.onBackground,
              modifier = Modifier.size(26.dp)
            )
          }
        }
      }
    }

    // Storage Permission banner if not granted
    if (!uiState.hasStoragePermission) {
      item {
        StoragePermissionBanner(
          onGrantPermission = { viewModel.requestStoragePermission() },
          onOpenSettings = { viewModel.openAppSettings() }
        )
      }
    }

    // 2. Top Category Grid Card (6 Categories in 3 columns x 2 rows)
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("top_categories_card")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp, horizontal = 12.dp),
          verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
          // Row 1: Images, Videos, Audio
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            CategoryGridItem(
              title = "Images",
              icon = Icons.Default.Image,
              badgeColor = ColorImagesCategory,
              onClick = {
                viewModel.selectCategory(FileCategory.IMAGES)
                onNavigateToFiles()
              },
              testTag = "category_images"
            )
            CategoryGridItem(
              title = "Videos",
              icon = Icons.Default.PlayCircle,
              badgeColor = ColorVideosCategory,
              onClick = {
                viewModel.selectCategory(FileCategory.VIDEOS)
                onNavigateToFiles()
              },
              testTag = "category_videos"
            )
            CategoryGridItem(
              title = "Audio",
              icon = Icons.Default.MusicNote,
              badgeColor = ColorAudioCategory,
              onClick = {
                viewModel.selectCategory(FileCategory.AUDIO)
                onNavigateToFiles()
              },
              testTag = "category_audio"
            )
          }

          // Row 2: Document, Download, Zip/APK
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            CategoryGridItem(
              title = "Document",
              icon = Icons.Default.Description,
              badgeColor = ColorDocumentCategory,
              onClick = {
                viewModel.selectCategory(FileCategory.DOCUMENTS)
                onNavigateToFiles()
              },
              testTag = "category_document"
            )
            CategoryGridItem(
              title = "Download",
              icon = Icons.Default.Download,
              badgeColor = ColorDownloadCategory,
              onClick = {
                viewModel.selectCategory(FileCategory.DOWNLOADS)
                onNavigateToFiles()
              },
              testTag = "category_download"
            )
            CategoryGridItem(
              title = "Zip/APK",
              icon = Icons.Default.Android,
              badgeColor = ColorZipApkCategory,
              onClick = {
                viewModel.selectCategory(FileCategory.ARCHIVES)
                onNavigateToFiles()
              },
              testTag = "category_zip_apk"
            )
          }
        }
      }
    }

    // 3. Section: "All Storage"
    item {
      SectionHeader(title = "All Storage")
    }

    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          StorageTileCard(
            title = "Internal",
            subtitle = if (uiState.hasStoragePermission) "${FileFormatUtils.formatFileSize(storageBreakdown.usedBytes)} / ${FileFormatUtils.formatFileSize(storageBreakdown.totalCapacityBytes)}" else "Permission required",
            icon = Icons.Default.PhoneAndroid,
            badgeColor = ColorInternalStorage,
            onClick = {
              viewModel.selectCategory(FileCategory.ALL)
              onNavigateToFiles()
            },
            modifier = Modifier
              .weight(1f)
              .testTag("storage_tile_internal")
          )
          StorageTileCard(
            title = "SD Card",
            subtitle = "Not inserted",
            icon = Icons.Default.SdStorage,
            badgeColor = ColorSdCard,
            onClick = { showSdCardDialog = true },
            modifier = Modifier
              .weight(1f)
              .testTag("storage_tile_sd_card")
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          StorageTileCard(
            title = "Scanner",
            subtitle = "Smart Scanning",
            icon = Icons.Default.Scanner,
            badgeColor = ColorScannerStorage,
            onClick = onNavigateToScanner,
            modifier = Modifier
              .weight(1f)
              .testTag("storage_tile_scanner")
          )
          StorageTileCard(
            title = "Backups",
            subtitle = "Encrypted Snapshots",
            icon = Icons.Default.Backup,
            badgeColor = ColorOtherStorage,
            onClick = onNavigateToBackups,
            modifier = Modifier
              .weight(1f)
              .testTag("storage_tile_backups")
          )
        }
      }
    }

    // 4. Section: "Sorted by Files"
    item {
      SectionHeader(title = "Sorted by Files")
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        StorageTileCard(
          title = "Files Cleanup",
          subtitle = if (uiState.hasStoragePermission) "${FileFormatUtils.formatFileSize(storageBreakdown.usedBytes)} of ${FileFormatUtils.formatFileSize(storageBreakdown.totalCapacityBytes)}" else "Permission required",
          icon = Icons.Default.AutoAwesome,
          badgeColor = ColorCleanupTool,
          onClick = { showCleanupDialog = true },
          modifier = Modifier
            .weight(1f)
            .testTag("tool_files_cleanup")
        )
        StorageTileCard(
          title = "Manage on PC",
          subtitle = "View files on PC",
          icon = Icons.Default.Computer,
          badgeColor = ColorManageOnPc,
          onClick = { showPcDialog = true },
          modifier = Modifier
            .weight(1f)
            .testTag("tool_manage_on_pc")
        )
      }
    }

    // 5. Section: "All Tools"
    item {
      SectionHeader(title = "All Tools")
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        StorageTileCard(
          title = "Starred",
          subtitle = "Your favourite items",
          icon = Icons.Default.Star,
          badgeColor = ColorStarredTool,
          onClick = {
            viewModel.selectCategory(FileCategory.FAVORITES)
            onNavigateToFiles()
          },
          modifier = Modifier
            .weight(1f)
            .testTag("tool_starred")
        )
        StorageTileCard(
          title = "Safe Folder",
          subtitle = "Protect your files",
          icon = Icons.Default.Lock,
          badgeColor = ColorSafeFolderTool,
          onClick = onNavigateToVault,
          modifier = Modifier
            .weight(1f)
            .testTag("tool_safe_folder")
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(28.dp))
    }
  }

  // Dialogs for Special Tiles
  if (showCleanupDialog) {
    FilesCleanupDialog(
      onDismiss = { showCleanupDialog = false },
      onCleanSuccess = { /* notified inside */ }
    )
  }

  if (showPcDialog) {
    ManageOnPcDialog(
      onDismiss = { showPcDialog = false }
    )
  }

  if (showSdCardDialog) {
    SdCardStatusDialog(
      onDismiss = { showSdCardDialog = false }
    )
  }
}

@Composable
private fun SectionHeader(title: String) {
  Text(
    text = title,
    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
  )
}

@Composable
private fun CategoryGridItem(
  title: String,
  icon: ImageVector,
  badgeColor: Color,
  onClick: () -> Unit,
  testTag: String
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier
      .width(88.dp)
      .clip(RoundedCornerShape(14.dp))
      .clickable { onClick() }
      .padding(vertical = 6.dp)
      .testTag(testTag)
  ) {
    Box(
      modifier = Modifier
        .size(54.dp)
        .clip(CircleShape)
        .background(badgeColor),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = Color.White,
        modifier = Modifier.size(28.dp)
      )
    }

    Text(
      text = title,
      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
      color = MaterialTheme.colorScheme.onSurface,
      maxLines = 1
    )
  }
}

@Composable
private fun StorageTileCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  badgeColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = modifier
      .clickable { onClick() }
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(CircleShape)
          .background(badgeColor),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = Color.White,
          modifier = Modifier.size(24.dp)
        )
      }

      Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1
        )
      }
    }
  }
}
