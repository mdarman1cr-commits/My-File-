package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "file_items",
  indices = [
    Index(value = ["parentFolderId"]),
    Index(value = ["isInVault"]),
    Index(value = ["isTrash"]),
    Index(value = ["category"]),
    Index(value = ["path"])
  ]
)
data class FileItemEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0L,
  val name: String,
  val extension: String,
  val isDirectory: Boolean = false,
  val parentFolderId: Long? = null,
  val path: String = "",
  val sizeBytes: Long = 0L,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
  val isEncrypted: Boolean = false,
  val isInVault: Boolean = false,
  val isFavorite: Boolean = false,
  val isTrash: Boolean = false,
  val category: String = "DOCUMENTS",
  val content: String? = null,
  val ocrTags: String = "",
  val filterApplied: String? = null,
  val syncStatus: String = "SYNCED",
  val cloudProvider: String = "NONE",
  val highlightColor: String? = null // RED, YELLOW, GREEN, BLUE, PURPLE or null
)
