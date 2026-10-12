package com.example.data.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.example.data.local.FileItemEntity
import com.example.data.model.FileCategory
import com.example.data.model.StorageBreakdown
import java.io.File
import kotlin.math.absoluteValue

object RealDeviceFileManager {

  val rootStoragePath: String
    get() = Environment.getExternalStorageDirectory().absolutePath

  fun detectCategory(file: File): String {
    if (file.isDirectory) return "FOLDER"
    val ext = file.extension.lowercase()
    return when (ext) {
      "jpg", "jpeg", "png", "webp", "gif", "bmp", "heic", "svg" -> "IMAGES"
      "mp4", "mkv", "mov", "avi", "3gp", "webm", "flv", "ts" -> "VIDEOS"
      "mp3", "m4a", "wav", "aac", "ogg", "flac", "amr", "opus", "wma" -> "AUDIO"
      "pdf", "doc", "docx", "txt", "xlsx", "xls", "pptx", "ppt", "csv", "md", "json", "xml", "html", "epub" -> "DOCUMENTS"
      "zip", "rar", "7z", "tar", "gz", "apk", "bz2", "xz" -> "ARCHIVES"
      else -> "DOCUMENTS"
    }
  }

  fun fileToEntity(file: File, parentFolderId: Long? = null): FileItemEntity {
    val isDir = file.isDirectory
    val category = detectCategory(file)
    val ext = if (isDir) "folder" else file.extension.lowercase()
    val size = if (isDir) {
      file.listFiles()?.size?.toLong() ?: 0L
    } else {
      file.length()
    }
    // Generate a positive unique ID based on path hash
    val id = (file.absolutePath.hashCode().toLong().absoluteValue and 0x7FFFFFFFL).coerceAtLeast(1L)

    return FileItemEntity(
      id = id,
      name = file.name,
      extension = ext,
      isDirectory = isDir,
      parentFolderId = parentFolderId,
      path = file.absolutePath,
      sizeBytes = size,
      createdAt = file.lastModified(),
      updatedAt = file.lastModified(),
      isEncrypted = false,
      isInVault = false,
      isFavorite = false,
      isTrash = false,
      category = category,
      content = null,
      ocrTags = ""
    )
  }

  fun getStorageBreakdown(context: Context): StorageBreakdown {
    return try {
      val externalDir = Environment.getExternalStorageDirectory()
      val statFs = StatFs(externalDir.path)
      val totalCapacityBytes = statFs.totalBytes
      val freeBytes = statFs.availableBytes
      val usedBytes = (totalCapacityBytes - freeBytes).coerceAtLeast(0L)

      var docBytes = 0L
      var imgBytes = 0L
      var videoBytes = 0L
      var audioBytes = 0L
      var downloadBytes = 0L

      if (StoragePermissionManager.hasStoragePermission(context)) {
        fun getFolderSize(dir: File, maxDepth: Int = 2, depth: Int = 0): Long {
          if (!dir.exists() || !dir.canRead() || depth > maxDepth) return 0L
          var size = 0L
          dir.listFiles()?.forEach { f ->
            if (f.isFile) size += f.length()
            else if (f.isDirectory && depth < maxDepth) size += getFolderSize(f, maxDepth, depth + 1)
          }
          return size
        }

        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val dcimDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        imgBytes = getFolderSize(picturesDir) + getFolderSize(dcimDir)

        val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        videoBytes = getFolderSize(moviesDir)

        val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val podcastsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS)
        audioBytes = getFolderSize(musicDir) + getFolderSize(podcastsDir)

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        downloadBytes = getFolderSize(downloadsDir)

        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        docBytes = getFolderSize(documentsDir)
      }

      StorageBreakdown(
        totalCapacityBytes = totalCapacityBytes,
        usedBytes = usedBytes,
        documentBytes = docBytes,
        imageBytes = imgBytes,
        videoBytes = videoBytes,
        audioBytes = audioBytes,
        downloadBytes = downloadBytes,
        scanBytes = 0L,
        vaultBytes = 0L,
        otherBytes = (usedBytes - (docBytes + imgBytes + videoBytes + audioBytes + downloadBytes)).coerceAtLeast(0L)
      )
    } catch (e: Exception) {
      StorageBreakdown()
    }
  }

  fun listFilesInDirectory(path: String?): List<FileItemEntity> {
    val dirPath = if (path.isNullOrBlank()) rootStoragePath else path
    val dir = File(dirPath)
    if (!dir.exists() || !dir.isDirectory || !dir.canRead()) {
      return emptyList()
    }

    val files = dir.listFiles() ?: return emptyList()
    return files
      .filter { !it.name.startsWith(".") } // Skip hidden dot files for clean view
      .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
      .map { fileToEntity(it) }
  }

  fun listFilesForCategory(context: Context, category: FileCategory): List<FileItemEntity> {
    if (!StoragePermissionManager.hasStoragePermission(context)) {
      return emptyList()
    }

    val result = mutableListOf<FileItemEntity>()
    val searchDirs = when (category) {
      FileCategory.IMAGES -> listOf(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
      )
      FileCategory.VIDEOS -> listOf(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
      )
      FileCategory.AUDIO -> listOf(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RECORDINGS),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
      )
      FileCategory.DOCUMENTS -> listOf(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        File(rootStoragePath)
      )
      FileCategory.DOWNLOADS -> listOf(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
      )
      FileCategory.ARCHIVES -> listOf(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
        File(rootStoragePath)
      )
      else -> listOf(File(rootStoragePath))
    }

    fun scan(dir: File, maxDepth: Int = 3, currentDepth: Int = 0) {
      if (!dir.exists() || !dir.canRead() || currentDepth > maxDepth) return
      val files = dir.listFiles() ?: return
      for (f in files) {
        if (f.name.startsWith(".")) continue
        if (f.isDirectory) {
          if (currentDepth < maxDepth) scan(f, maxDepth, currentDepth + 1)
        } else {
          val cat = detectCategory(f)
          val match = when (category) {
            FileCategory.IMAGES -> cat == "IMAGES"
            FileCategory.VIDEOS -> cat == "VIDEOS"
            FileCategory.AUDIO -> cat == "AUDIO"
            FileCategory.DOCUMENTS -> cat == "DOCUMENTS"
            FileCategory.DOWNLOADS -> true // all files in Download folder
            FileCategory.ARCHIVES -> cat == "ARCHIVES"
            else -> false
          }
          if (match) {
            result.add(fileToEntity(f))
            if (result.size >= 150) return // Cap to 150 items for smooth UI performance
          }
        }
      }
    }

    for (dir in searchDirs) {
      scan(dir)
      if (result.size >= 150) break
    }

    return result.sortedByDescending { it.updatedAt }
  }

  fun searchFiles(query: String, searchPath: String? = null): List<FileItemEntity> {
    val q = query.trim().lowercase()
    if (q.isBlank()) return emptyList()

    val baseDir = File(searchPath ?: rootStoragePath)
    val matches = mutableListOf<FileItemEntity>()

    fun scan(dir: File, depth: Int = 0) {
      if (!dir.exists() || !dir.canRead() || depth > 4 || matches.size >= 60) return
      val list = dir.listFiles() ?: return
      for (f in list) {
        if (f.name.startsWith(".")) continue
        if (f.name.lowercase().contains(q)) {
          matches.add(fileToEntity(f))
        }
        if (f.isDirectory && depth < 4 && matches.size < 60) {
          scan(f, depth + 1)
        }
      }
    }

    scan(baseDir)
    return matches.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
  }

  fun createFolder(parentPath: String?, folderName: String): Boolean {
    val base = if (parentPath.isNullOrBlank()) rootStoragePath else parentPath
    val newDir = File(base, folderName.trim())
    return if (!newDir.exists()) newDir.mkdirs() else true
  }

  fun createTextFile(parentPath: String?, fileName: String, content: String): Boolean {
    val base = if (parentPath.isNullOrBlank()) rootStoragePath else parentPath
    val newFile = File(base, fileName.trim())
    return try {
      newFile.writeText(content, Charsets.UTF_8)
      true
    } catch (e: Exception) {
      false
    }
  }

  fun renameFile(filePath: String, newName: String): String? {
    val target = File(filePath)
    if (!target.exists()) return null
    val parent = target.parentFile ?: return null
    val dest = File(parent, newName.trim())
    return if (target.renameTo(dest)) dest.absolutePath else null
  }

  fun deleteFile(filePath: String): Boolean {
    val target = File(filePath)
    if (!target.exists()) return false
    return if (target.isDirectory) target.deleteRecursively() else target.delete()
  }

  fun readFileText(filePath: String): String? {
    val file = File(filePath)
    if (!file.exists() || file.isDirectory || !file.canRead()) return null
    return try {
      file.bufferedReader(Charsets.UTF_8).use { it.readText() }
    } catch (e: Exception) {
      null
    }
  }

  fun saveFileText(filePath: String, text: String): Boolean {
    val file = File(filePath)
    return try {
      file.writeText(text, Charsets.UTF_8)
      true
    } catch (e: Exception) {
      false
    }
  }

  fun getOpenWithIntent(context: Context, filePath: String): Intent? {
    val file = File(filePath)
    if (!file.exists()) return null

    return try {
      val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
      )
      val ext = file.extension.lowercase()
      val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "*/*"

      Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    } catch (e: Exception) {
      null
    }
  }
}
