package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.FileItemEntity
import com.example.data.security.EncryptionManager

@Composable
fun CreateFolderDialog(
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit
) {
  var folderName by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("New Folder", style = MaterialTheme.typography.titleLarge) },
    text = {
      OutlinedTextField(
        value = folderName,
        onValueChange = { folderName = it },
        placeholder = { Text("e.g. Work Documents") },
        label = { Text("Folder Name") },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("new_folder_input")
      )
    },
    confirmButton = {
      Button(
        onClick = {
          if (folderName.isNotBlank()) {
            onConfirm(folderName)
            onDismiss()
          }
        },
        enabled = folderName.isNotBlank(),
        modifier = Modifier.testTag("confirm_create_folder_button")
      ) {
        Text("Create")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun CreateFileDialog(
  onDismiss: () -> Unit,
  onConfirm: (String, String) -> Unit
) {
  var fileName by remember { mutableStateOf("") }
  var content by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("New File", style = MaterialTheme.typography.titleLarge) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = fileName,
          onValueChange = { fileName = it },
          placeholder = { Text("e.g. meeting_notes.md") },
          label = { Text("File Name") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("new_file_name_input")
        )
        OutlinedTextField(
          value = content,
          onValueChange = { content = it },
          placeholder = { Text("Enter initial text or markdown...") },
          label = { Text("File Content") },
          maxLines = 5,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("new_file_content_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (fileName.isNotBlank()) {
            onConfirm(fileName, content)
            onDismiss()
          }
        },
        enabled = fileName.isNotBlank(),
        modifier = Modifier.testTag("confirm_create_file_button")
      ) {
        Text("Create")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun RenameDialog(
  currentName: String,
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit
) {
  var newName by remember { mutableStateOf(currentName) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Rename Item", style = MaterialTheme.typography.titleLarge) },
    text = {
      OutlinedTextField(
        value = newName,
        onValueChange = { newName = it },
        label = { Text("Name") },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("rename_input")
      )
    },
    confirmButton = {
      Button(
        onClick = {
          if (newName.isNotBlank()) {
            onConfirm(newName)
            onDismiss()
          }
        },
        enabled = newName.isNotBlank() && newName != currentName,
        modifier = Modifier.testTag("confirm_rename_button")
      ) {
        Text("Save")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun FileDetailsDialog(
  item: FileItemEntity,
  onDismiss: () -> Unit
) {
  val checksum = remember(item) {
    EncryptionManager.calculateSha256(item.content ?: item.name)
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("File Details", style = MaterialTheme.typography.titleLarge) },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        DetailRow("Name", item.name)
        DetailRow("Type", if (item.isDirectory) "Directory" else item.extension.uppercase())
        DetailRow("Size", FileFormatUtils.formatFileSize(item.sizeBytes))
        DetailRow("Created", FileFormatUtils.formatDate(item.createdAt))
        DetailRow("Modified", FileFormatUtils.formatDate(item.updatedAt))
        DetailRow("Encryption", if (item.isEncrypted || item.isInVault) "AES-256-GCM (End-to-End)" else "Standard Storage")
        DetailRow("Storage", "100% Local On-Device")
        if (item.ocrTags.isNotBlank()) {
          DetailRow("OCR Tags", item.ocrTags)
        }
        DetailRow("SHA-256", checksum.take(16) + "...")
      }
    },
    confirmButton = {
      Button(onClick = onDismiss) {
        Text("Close")
      }
    }
  )
}

@Composable
private fun DetailRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
fun DocumentViewerDialog(
  item: FileItemEntity,
  content: String?,
  onDismiss: () -> Unit,
  onSaveContent: (String) -> Unit
) {
  var editedText by remember { mutableStateOf(content ?: "") }
  var isEditing by remember { mutableStateOf(false) }
  val context = LocalContext.current

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surface,
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .padding(vertical = 24.dp)
        .testTag("document_viewer_dialog")
    ) {
      Column(
        modifier = Modifier.padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Top Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            if (item.isInVault) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Vault",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
            }
            Text(
              text = item.name,
              style = MaterialTheme.typography.titleMedium,
              maxLines = 1
            )
          }

          Row {
            IconButton(
              onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("File Content", editedText)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
              },
              modifier = Modifier.testTag("copy_content_button")
            ) {
              Icon(Icons.Default.ContentCopy, contentDescription = "Copy text")
            }

            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = "Close")
            }
          }
        }

        if (item.ocrTags.isNotBlank()) {
          Text(
            text = "Automated OCR Tags: ${item.ocrTags}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
          )
        }

        // Editor / Viewer
        OutlinedTextField(
          value = editedText,
          onValueChange = {
            editedText = it
            isEditing = true
          },
          label = { Text(if (item.category == "SCANS") "Extracted OCR Document Text" else "Document Content") },
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .testTag("document_editor_field")
        )

        // Save Button if edited
        if (isEditing) {
          Button(
            onClick = {
              onSaveContent(editedText)
              isEditing = false
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("save_content_button")
          ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.size(8.dp))
            Text("Save Changes")
          }
        }
      }
    }
  }
}

val HighlightColorOptions = listOf(
  Triple("YELLOW", Color(0xFFF59E0B), "Important"),
  Triple("RED", Color(0xFFEF4444), "Urgent"),
  Triple("GREEN", Color(0xFF10B981), "Verified"),
  Triple("BLUE", Color(0xFF3B82F6), "Work"),
  Triple("PURPLE", Color(0xFF8B5CF6), "Personal")
)

@Composable
fun HighlightColorPickerDialog(
  currentColor: String?,
  onDismiss: () -> Unit,
  onSelectColor: (String?) -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Highlight File", style = MaterialTheme.typography.titleLarge) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Select a highlight tag color to visually categorize and pin this file:", style = MaterialTheme.typography.bodySmall)

        HighlightColorOptions.forEach { (key, color, label) ->
          val isSelected = currentColor == key
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                onSelectColor(key)
                onDismiss()
              }
              .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                  if (isSelected) Modifier.border(2.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                  else Modifier
                )
            )
            Text(label, style = MaterialTheme.typography.bodyMedium)
          }
        }

        // Option to remove highlight
        Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                onSelectColor(null)
                onDismiss()
              }
              .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Box(
            modifier = Modifier
              .size(26.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
          }
          Text("Remove Highlight", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        }
      }
    },
    confirmButton = {},
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

@Composable
fun CreateZipDialog(
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit
) {
  var zipName by remember { mutableStateOf("Archive_${System.currentTimeMillis() % 10000}.zip") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Create ZIP Archive", style = MaterialTheme.typography.titleLarge) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Enter name for the new compressed ZIP file:", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(
          value = zipName,
          onValueChange = { zipName = it },
          label = { Text("Archive Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("zip_archive_name_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (zipName.isNotBlank()) {
            onConfirm(zipName)
            onDismiss()
          }
        },
        enabled = zipName.isNotBlank(),
        modifier = Modifier.testTag("confirm_create_zip_button")
      ) {
        Text("Create ZIP")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

@Composable
fun ExtractZipDialog(
  archiveName: String,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit
) {
  val targetFolder = archiveName.substringBeforeLast(".") + "_Extracted"

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Extract ZIP Archive", style = MaterialTheme.typography.titleLarge) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Extract all files and contents from '$archiveName' into a new folder?", style = MaterialTheme.typography.bodySmall)
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.Save, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Text("Destination: $targetFolder/", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onConfirm()
          onDismiss()
        },
        modifier = Modifier.testTag("confirm_extract_zip_button")
      ) {
        Text("Extract Files")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

