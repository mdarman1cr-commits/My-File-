package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.EmeraldTertiaryDark

@Composable
fun FilesCleanupDialog(
  onDismiss: () -> Unit,
  onCleanSuccess: (String) -> Unit
) {
  var isCleaning by remember { mutableStateOf(false) }
  var isCleaned by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(ColorCleanupTool.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = ColorCleanupTool,
            modifier = Modifier.size(20.dp)
          )
        }
        Text("Files Cleanup", style = MaterialTheme.typography.titleLarge)
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = if (isCleaned) "Your storage is optimized! 2.24 GB space freed." else "Analyze and remove temporary cache, duplicate APKs, and residual files.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Storage Bar
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Storage Usage", style = MaterialTheme.typography.labelMedium)
            Text(if (isCleaned) "90.05 / 128 GB" else "92.29 / 128 GB", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
          }
          LinearProgressIndicator(
            progress = { if (isCleaned) 0.70f else 0.72f },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp))
          )
        }

        // Junk Items List
        CleanupItemRow("App Temporary Cache", if (isCleaned) "Cleaned" else "1.42 GB", isCleaned)
        CleanupItemRow("Obsolete APK Packages", if (isCleaned) "Cleaned" else "520 MB", isCleaned)
        CleanupItemRow("Duplicate Downloads", if (isCleaned) "Cleaned" else "300 MB", isCleaned)
      }
    },
    confirmButton = {
      if (!isCleaned) {
        Button(
          onClick = {
            isCleaned = true
            onCleanSuccess("Cleaned 2.24 GB junk files successfully!")
          },
          colors = ButtonDefaults.buttonColors(containerColor = ColorCleanupTool),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("confirm_cleanup_button")
        ) {
          Text("Clean 2.24 GB")
        }
      } else {
        Button(
          onClick = onDismiss,
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Done")
        }
      }
    },
    dismissButton = {
      if (!isCleaned) {
        TextButton(onClick = onDismiss) { Text("Cancel") }
      }
    }
  )
}

@Composable
private fun CleanupItemRow(name: String, size: String, isCleaned: Boolean) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(name, style = MaterialTheme.typography.bodyMedium)
    Text(
      text = size,
      style = MaterialTheme.typography.labelMedium,
      color = if (isCleaned) EmeraldTertiaryDark else MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
fun ManageOnPcDialog(
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val pcUrl = "http://192.168.1.104:8080"
  var isServiceRunning by remember { mutableStateOf(true) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(ColorManageOnPc.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Computer,
            contentDescription = null,
            tint = ColorManageOnPc,
            modifier = Modifier.size(20.dp)
          )
        }
        Text("Manage on PC", style = MaterialTheme.typography.titleLarge)
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Access, download, and manage your phone's files from any computer web browser on the same Wi-Fi network.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(Icons.Default.Wifi, contentDescription = null, tint = EmeraldTertiaryDark, modifier = Modifier.size(16.dp))
          Text(
            text = if (isServiceRunning) "Wi-Fi Server Active" else "Service Stopped",
            style = MaterialTheme.typography.labelMedium,
            color = if (isServiceRunning) EmeraldTertiaryDark else MaterialTheme.colorScheme.outline
          )
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (isServiceRunning) pcUrl else "Offline",
              style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              ),
              color = MaterialTheme.colorScheme.primary
            )

            IconButton(
              onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("PC Web URL", pcUrl)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "URL copied to clipboard", Toast.LENGTH_SHORT).show()
              },
              enabled = isServiceRunning
            ) {
              Icon(Icons.Default.ContentCopy, contentDescription = "Copy URL")
            }
          }
        }

        Text(
          text = "Steps:\n1. Connect PC and phone to the same Wi-Fi.\n2. Open Google Chrome, Edge, or Safari on your PC.\n3. Navigate to the address shown above.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { isServiceRunning = !isServiceRunning },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isServiceRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
      ) {
        Text(if (isServiceRunning) "Stop Server" else "Start Server")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Close") }
    }
  )
}

@Composable
fun SdCardStatusDialog(
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(ColorSdCard.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.SdStorage, contentDescription = null, tint = ColorSdCard, modifier = Modifier.size(20.dp))
        }
        Text("SD Card Storage")
      }
    },
    text = {
      Text(
        text = "No physical microSD card is currently inserted. Your primary storage is active on Internal Storage (128 GB). When an SD card is inserted, it will automatically mount here.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    },
    confirmButton = {
      Button(onClick = onDismiss) { Text("OK") }
    }
  )
}
