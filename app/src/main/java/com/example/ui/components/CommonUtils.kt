package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.local.FileItemEntity
import com.example.ui.theme.ColorArchive
import com.example.ui.theme.ColorDocument
import com.example.ui.theme.ColorFolder
import com.example.ui.theme.ColorImage
import com.example.ui.theme.ColorScan
import com.example.ui.theme.ColorVault
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Specific palette matching the reference screenshot
val ColorImagesCategory = Color(0xFF20B26C) // Vibrant Green
val ColorVideosCategory = Color(0xFF1E88E5) // Blue
val ColorAudioCategory = Color(0xFFFF6E40) // Orange / Coral
val ColorDocumentCategory = Color(0xFF7C4DFF) // Purple / Violet
val ColorDownloadCategory = Color(0xFF1E88E5) // Blue
val ColorZipApkCategory = Color(0xFFE91E63) // Magenta / Pink

val ColorInternalStorage = Color(0xFF1E88E5)
val ColorSdCard = Color(0xFFFF4081)
val ColorScannerStorage = Color(0xFF00BCD4)
val ColorOtherStorage = Color(0xFF00C853)

val ColorCleanupTool = Color(0xFF00897B)
val ColorManageOnPc = Color(0xFF1E88E5)
val ColorStarredTool = Color(0xFFFFA000)
val ColorSafeFolderTool = Color(0xFF536DFE)

object FileFormatUtils {

  fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
    return String.format(Locale.US, "%.1f %s", value, units[digitGroups])
  }

  fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
  }

  fun getFileIconAndColor(item: FileItemEntity): Pair<ImageVector, Color> {
    if (item.isInVault) {
      return Pair(Icons.Default.Lock, ColorSafeFolderTool)
    }
    if (item.isDirectory) {
      return Pair(Icons.Default.Folder, ColorFolder)
    }
    return when (item.extension.lowercase()) {
      "pdf" -> Pair(Icons.Default.PictureAsPdf, ColorDocumentCategory)
      "png", "jpg", "jpeg", "webp" -> Pair(Icons.Default.Image, ColorImagesCategory)
      "mp4", "mkv", "mov", "avi" -> Pair(Icons.Default.VideoFile, ColorVideosCategory)
      "mp3", "m4a", "wav", "flac" -> Pair(Icons.Default.MusicNote, ColorAudioCategory)
      "apk" -> Pair(Icons.Default.Android, ColorZipApkCategory)
      "zip", "rar", "7z", "tar", "mfe" -> Pair(Icons.Default.Archive, ColorZipApkCategory)
      "txt", "md", "doc", "docx" -> Pair(Icons.Default.TextFields, ColorDocumentCategory)
      "enc" -> Pair(Icons.Default.Lock, ColorSafeFolderTool)
      else -> {
        if (item.category == "SCANS") Pair(Icons.Default.Scanner, ColorScannerStorage)
        else if (item.category == "DOWNLOADS") Pair(Icons.Default.Download, ColorDownloadCategory)
        else Pair(Icons.Default.Description, ColorDocument)
      }
    }
  }
}
