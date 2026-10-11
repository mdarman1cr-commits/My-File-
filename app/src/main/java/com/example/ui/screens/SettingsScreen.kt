package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.FileExplorerViewModel

@Composable
fun SettingsScreen(
  viewModel: FileExplorerViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var showChangePinDialog by remember { mutableStateOf(false) }

  androidx.activity.compose.BackHandler(enabled = true) {
    onNavigateBack()
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
      .testTag("settings_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      IconButton(
        onClick = onNavigateBack,
        modifier = Modifier.testTag("settings_back_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back"
        )
      }
      Text("Settings & Customization", style = MaterialTheme.typography.titleLarge)
    }

    // 1. Theme Selection
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Text("App Theme", style = MaterialTheme.typography.titleMedium)
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          ThemeOptionChip(
            label = "System",
            icon = Icons.Default.BrightnessAuto,
            isSelected = uiState.themeMode == ThemeMode.SYSTEM,
            onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
            modifier = Modifier.weight(1f)
          )
          ThemeOptionChip(
            label = "Light",
            icon = Icons.Default.LightMode,
            isSelected = uiState.themeMode == ThemeMode.LIGHT,
            onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
            modifier = Modifier.weight(1f)
          )
          ThemeOptionChip(
            label = "Dark",
            icon = Icons.Default.DarkMode,
            isSelected = uiState.themeMode == ThemeMode.DARK,
            onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    // 2. Customizable Dashboard Widgets
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.Widgets, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Text("Customizable Dashboard", style = MaterialTheme.typography.titleMedium)
        }

        Text(
          text = "Personalize which cards and widgets appear on your home dashboard:",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val cfg = uiState.dashboardConfig

        DashboardToggleItem(
          title = "Device Storage Overview",
          subtitle = "Display used/free space and categorized usage bars",
          checked = cfg.showStorageOverview,
          onCheckedChange = { viewModel.updateDashboardConfig(cfg.copy(showStorageOverview = it)) }
        )

        DashboardToggleItem(
          title = "Quick Actions Panel",
          subtitle = "Show 1-tap buttons for Scan, Safe Vault, and Backups",
          checked = cfg.showQuickActions,
          onCheckedChange = { viewModel.updateDashboardConfig(cfg.copy(showQuickActions = it)) }
        )

        DashboardToggleItem(
          title = "Local Device Storage Status",
          subtitle = "Display on-device storage metrics and 100% offline status",
          checked = cfg.showLocalStorageStatus,
          onCheckedChange = { viewModel.updateDashboardConfig(cfg.copy(showLocalStorageStatus = it)) }
        )

        DashboardToggleItem(
          title = "Recent Files Carousel",
          subtitle = "Quickly resume recent documents and scans",
          checked = cfg.showRecentFiles,
          onCheckedChange = { viewModel.updateDashboardConfig(cfg.copy(showRecentFiles = it)) }
        )

        DashboardToggleItem(
          title = "Category Grid",
          subtitle = "Show category shortcuts for Documents, Images, Scans",
          checked = cfg.showCategoryGrid,
          onCheckedChange = { viewModel.updateDashboardConfig(cfg.copy(showCategoryGrid = it)) }
        )
      }
    }

    // 3. Security & Safe Vault Settings
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Text("Security & Privacy", style = MaterialTheme.typography.titleMedium)
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("Master Vault PIN", style = MaterialTheme.typography.bodyLarge)
            Text("Used to protect encrypted folders and vault", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }

          OutlinedButton(
            onClick = { showChangePinDialog = true },
            modifier = Modifier.testTag("settings_change_pin_button")
          ) {
            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.size(4.dp))
            Text("Change PIN")
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("Lock Safe Vault Immediately", style = MaterialTheme.typography.bodyLarge)
            Text("Clear decrypted keys from memory", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }

          Button(
            onClick = { viewModel.lockVault() },
            modifier = Modifier.testTag("settings_lock_vault_button")
          ) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.size(4.dp))
            Text("Lock")
          }
        }
      }
    }

    // 4. Maintenance & Storage Cleanup
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
          Text("Storage Cleanup", style = MaterialTheme.typography.titleMedium)
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("Empty Recycle Bin", style = MaterialTheme.typography.bodyLarge)
            Text("Permanently delete discarded files", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }

          OutlinedButton(
            onClick = { viewModel.emptyTrash() },
            modifier = Modifier.testTag("settings_empty_trash_button")
          ) {
            Text("Empty")
          }
        }
      }
    }

    // 5. About
    Text(
      text = "My File Explorer • v1.0.0\n100% Private Offline Storage • AES-256-GCM Vault • OCR Scanning",
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.outline,
      modifier = Modifier.padding(top = 8.dp)
    )

    Spacer(modifier = Modifier.height(24.dp))
  }

  if (showChangePinDialog) {
    ChangePinDialog(
      onDismiss = { showChangePinDialog = false },
      onConfirm = { oldPin, newPin ->
        viewModel.changeMasterPin(oldPin, newPin)
      }
    )
  }
}

@Composable
private fun ThemeOptionChip(
  label: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  FilterChip(
    selected = isSelected,
    onClick = onClick,
    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
    leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
    shape = RoundedCornerShape(12.dp),
    modifier = modifier
  )
}

@Composable
private fun DashboardToggleItem(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(title, style = MaterialTheme.typography.bodyMedium)
      Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Switch(checked = checked, onCheckedChange = onCheckedChange)
  }
}
