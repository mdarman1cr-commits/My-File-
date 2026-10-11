package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.FileItemEntity
import com.example.ui.components.CreateFileDialog
import com.example.ui.components.FileDetailsDialog
import com.example.ui.components.FileItemRow
import com.example.ui.theme.ColorVault
import com.example.ui.theme.EmeraldTertiaryDark
import com.example.ui.viewmodel.FileExplorerViewModel

@Composable
fun SecureVaultScreen(
  viewModel: FileExplorerViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val vaultFiles by viewModel.displayedFiles.collectAsStateWithLifecycle()

  var pinInput by remember { mutableStateOf("") }
  var showChangePinDialog by remember { mutableStateOf(false) }
  var showCreateVaultFileDialog by remember { mutableStateOf(false) }
  var fileToShowDetails by remember { mutableStateOf<FileItemEntity?>(null) }

  androidx.activity.compose.BackHandler(enabled = true) {
    onNavigateBack()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .testTag("secure_vault_screen")
  ) {
    // Back arrow button in top left
    IconButton(
      onClick = onNavigateBack,
      modifier = Modifier
        .align(Alignment.TopStart)
        .padding(16.dp)
        .testTag("vault_back_button")
    ) {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = "Back",
        tint = MaterialTheme.colorScheme.onBackground
      )
    }

    if (!uiState.isVaultUnlocked) {
      // Locked Vault UI with PIN entry
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Box(
          modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(ColorVault.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Encrypted Safe",
            tint = ColorVault,
            modifier = Modifier.size(42.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Encrypted Safe Vault",
          style = MaterialTheme.typography.displayLarge.copy(fontSize = 24.sp),
          color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Zero-Knowledge AES-256-GCM Encryption",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        // PIN display dots
        Row(
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          modifier = Modifier.padding(bottom = 20.dp)
        ) {
          repeat(4) { index ->
            val isFilled = pinInput.length > index
            Box(
              modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(
                  if (isFilled) ColorVault else MaterialTheme.colorScheme.surfaceVariant
                )
                .border(1.dp, ColorVault.copy(alpha = 0.5f), CircleShape)
            )
          }
        }

        // Numeric Keypad
        NumericKeypad(
          onDigitClick = { digit ->
            if (pinInput.length < 4) {
              val newPin = pinInput + digit
              pinInput = newPin
              if (newPin.length == 4) {
                if (viewModel.unlockVault(newPin)) {
                  pinInput = ""
                } else {
                  pinInput = ""
                }
              }
            }
          },
          onBackspace = {
            if (pinInput.isNotEmpty()) {
              pinInput = pinInput.dropLast(1)
            }
          }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Default PIN hint
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
          Text(
            text = "Default Vault PIN: 1234 (Change anytime in settings)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
          )
        }
      }
    } else {
      // Unlocked Vault Screen
      Column(modifier = Modifier.fillMaxSize()) {
        // Vault Header
        Card(
          shape = RoundedCornerShape(0.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(EmeraldTertiaryDark.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.LockOpen,
                  contentDescription = null,
                  tint = EmeraldTertiaryDark,
                  modifier = Modifier.size(20.dp)
                )
              }
              Column {
                Text("Safe Vault Unlocked", style = MaterialTheme.typography.titleMedium)
                Text(
                  "E2E Encryption Active • 256-bit",
                  style = MaterialTheme.typography.bodySmall,
                  color = EmeraldTertiaryDark
                )
              }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              IconButton(onClick = { showChangePinDialog = true }) {
                Icon(Icons.Default.Key, contentDescription = "Change PIN")
              }
              Button(
                onClick = { viewModel.lockVault() },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                  containerColor = ColorVault
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("lock_vault_now_button")
              ) {
                Text("Lock")
              }
            }
          }
        }

        // Vault Files
        if (vaultFiles.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(32.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = ColorVault,
                modifier = Modifier.size(48.dp)
              )
              Text("No Encrypted Files in Safe", style = MaterialTheme.typography.titleMedium)
              Text(
                "Files moved here are strictly encrypted with AES-256-GCM and inaccessible outside without your master PIN.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              OutlinedButton(
                onClick = { showCreateVaultFileDialog = true },
                modifier = Modifier.testTag("create_vault_note_button")
              ) {
                Text("Create Encrypted Note")
              }
            }
          }
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 10.dp, bottom = 96.dp)
          ) {
            items(vaultFiles, key = { it.id }) { file ->
              FileItemRow(
                item = file,
                onClick = { viewModel.openFilePreview(file) },
                onToggleFavorite = { viewModel.toggleFavorite(file.id) },
                onRename = {},
                onMoveToVault = {},
                onExtractFromVault = { viewModel.extractFromVault(file.id) },
                onDelete = { viewModel.deletePermanently(file.id) },
                onRestore = {},
                onShowDetails = { fileToShowDetails = file }
              )
            }
          }
        }
      }

      FloatingActionButton(
        onClick = { showCreateVaultFileDialog = true },
        containerColor = ColorVault,
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(24.dp)
          .testTag("add_vault_file_fab")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Encrypted Item")
      }
    }

    // Change PIN Dialog
    if (showChangePinDialog) {
      ChangePinDialog(
        onDismiss = { showChangePinDialog = false },
        onConfirm = { oldPin, newPin ->
          viewModel.changeMasterPin(oldPin, newPin)
        }
      )
    }

    if (showCreateVaultFileDialog) {
      CreateFileDialog(
        onDismiss = { showCreateVaultFileDialog = false },
        onConfirm = { name, content ->
          viewModel.createNewTextFile(name, content)
        }
      )
    }

    fileToShowDetails?.let { target ->
      FileDetailsDialog(
        item = target,
        onDismiss = { fileToShowDetails = null }
      )
    }
  }
}

@Composable
private fun NumericKeypad(
  onDigitClick: (String) -> Unit,
  onBackspace: () -> Unit
) {
  val rows = listOf(
    listOf("1", "2", "3"),
    listOf("4", "5", "6"),
    listOf("7", "8", "9"),
    listOf("C", "0", "⌫")
  )

  Column(
    verticalArrangement = Arrangement.spacedBy(10.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    rows.forEach { row ->
      Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        row.forEach { key ->
          Box(
            modifier = Modifier
              .size(62.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .clickable {
                when (key) {
                  "⌫" -> onBackspace()
                  "C" -> onBackspace()
                  else -> onDigitClick(key)
                }
              }
              .testTag("pin_key_$key"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = key,
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    }
  }
}

@Composable
fun ChangePinDialog(
  onDismiss: () -> Unit,
  onConfirm: (String, String) -> Unit
) {
  var oldPin by remember { mutableStateOf("") }
  var newPin by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Change Master PIN") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = oldPin,
          onValueChange = { if (it.length <= 4) oldPin = it },
          label = { Text("Current 4-digit PIN") },
          visualTransformation = PasswordVisualTransformation(),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("old_pin_input")
        )
        OutlinedTextField(
          value = newPin,
          onValueChange = { if (it.length <= 4) newPin = it },
          label = { Text("New 4-digit PIN") },
          visualTransformation = PasswordVisualTransformation(),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("new_pin_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (oldPin.isNotBlank() && newPin.length == 4) {
            onConfirm(oldPin, newPin)
            onDismiss()
          }
        },
        enabled = oldPin.isNotBlank() && newPin.length == 4
      ) {
        Text("Update PIN")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
