package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "backup_entries")
data class BackupEntryEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0L,
  val filename: String,
  val createdAt: Long = System.currentTimeMillis(),
  val fileCount: Int = 0,
  val sizeBytes: Long = 0L,
  val encryptionAlgorithm: String = "AES-256-GCM",
  val passphraseHint: String = "",
  val checksum: String = ""
)
