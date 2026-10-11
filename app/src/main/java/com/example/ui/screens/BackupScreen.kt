package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.BackupEntryEntity
import com.example.ui.components.FileFormatUtils
import com.example.ui.viewmodel.FileExplorerViewModel

@Composable
fun BackupScreen(
  viewModel: FileExplorerViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val backups by viewModel.backupEntries.collectAsStateWithLifecycle()
  var showCreateBackupDialog by remember { mutableStateOf(false) }

  androidx.activity.compose.BackHandler(enabled = true) {
    onNavigateBack()
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
      .testTag("backup_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        IconButton(
          onClick = onNavigateBack,
          modifier = Modifier.testTag("backup_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back"
          )
        }
        Column {
          Text("Encrypted Backups", style = MaterialTheme.typography.titleLarge)
          Text("Local offline backups with AES-256", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }

      Button(
        onClick = { showCreateBackupDialog = true },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("create_backup_button")
      ) {
        Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("New Backup")
      }
    }

    // Security Guarantee Info
    Surface(
      shape = RoundedCornerShape(14.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
      Row(
        modifier = Modifier.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Security,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp)
        )
        Text(
          text = "All backup archives (.mfe) are end-to-end encrypted with PBKDF2 key derivation and AES-256-GCM. Backups remain unreadable without your custom passphrase.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Backups List
    Text("Saved Encrypted Archives", style = MaterialTheme.typography.titleMedium)

    if (backups.isEmpty()) {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text("No Backups Created Yet", style = MaterialTheme.typography.titleSmall)
          Text("Tap 'New Backup' to create an encrypted snapshot of your files and safe vault.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(backups, key = { it.id }) { backup ->
          BackupCard(
            backup = backup,
            onDelete = { viewModel.deleteBackup(backup) }
          )
        }
      }
    }
  }

  // Create Backup Dialog
  if (showCreateBackupDialog) {
    CreateBackupDialog(
      onDismiss = { showCreateBackupDialog = false },
      onConfirm = { pass, hint ->
        viewModel.createEncryptedBackup(pass, hint)
      }
    )
  }
}

@Composable
private fun BackupCard(
  backup: BackupEntryEntity,
  onDelete: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
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
          Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
          Text(backup.filename, style = MaterialTheme.typography.titleSmall)
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline)
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Size: ${FileFormatUtils.formatFileSize(backup.sizeBytes)} • ${backup.fileCount} items", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(FileFormatUtils.formatDate(backup.createdAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }

      Text(
        text = "Algorithm: ${backup.encryptionAlgorithm} • Hint: ${backup.passphraseHint}",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary
      )

      if (backup.checksum.isNotBlank()) {
        Text(
          text = "SHA-256: ${backup.checksum.take(24)}...",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.outline
        )
      }
    }
  }
}

@Composable
fun CreateBackupDialog(
  onDismiss: () -> Unit,
  onConfirm: (String, String) -> Unit
) {
  var passphrase by remember { mutableStateOf("") }
  var hint by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Create Encrypted Backup") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Enter a secure passphrase to encrypt the backup snapshot:", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(
          value = passphrase,
          onValueChange = { passphrase = it },
          label = { Text("Passphrase") },
          visualTransformation = PasswordVisualTransformation(),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("backup_passphrase_input")
        )
        OutlinedTextField(
          value = hint,
          onValueChange = { hint = it },
          label = { Text("Password Hint (Optional)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("backup_hint_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (passphrase.isNotBlank()) {
            onConfirm(passphrase, hint)
            onDismiss()
          }
        },
        enabled = passphrase.isNotBlank(),
        modifier = Modifier.testTag("confirm_create_backup_button")
      ) {
        Text("Encrypt & Save")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
