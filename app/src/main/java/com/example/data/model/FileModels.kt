package com.example.data.model

enum class FileCategory(val label: String) {
  ALL("All Files"),
  IMAGES("Images"),
  VIDEOS("Videos"),
  AUDIO("Audio"),
  DOCUMENTS("Document"),
  DOWNLOADS("Download"),
  ARCHIVES("Zip/APK"),
  SCANS("Scanner"),
  VAULT("Safe Folder"),
  FAVORITES("Starred"),
  TRASH("Recycle Bin")
}

enum class SortOption(val label: String) {
  DATE_DESC("Newest first"),
  DATE_ASC("Oldest first"),
  NAME_ASC("Name (A to Z)"),
  NAME_DESC("Name (Z to A)"),
  SIZE_DESC("Largest first"),
  SIZE_ASC("Smallest first")
}

enum class ViewMode {
  LIST, GRID
}

enum class StorageType(val label: String) {
  INTERNAL("Internal Device Storage"),
  SD_CARD("SD Card"),
  VAULT("Encrypted Safe Vault")
}

data class StorageBreakdown(
  val totalCapacityBytes: Long = 128L * 1024 * 1024 * 1024, // 128 GB device capacity
  val usedBytes: Long = 92_290_000_000L, // 92.29 GB matching the reference screenshot!
  val documentBytes: Long = 14_300_000_000L,
  val imageBytes: Long = 38_500_000_000L,
  val videoBytes: Long = 26_200_000_000L,
  val audioBytes: Long = 6_400_000_000L,
  val downloadBytes: Long = 4_800_000_000L,
  val scanBytes: Long = 1_800_000_000L,
  val vaultBytes: Long = 290_000_000L,
  val otherBytes: Long = 0L
) {
  val freeBytes: Long
    get() = (totalCapacityBytes - usedBytes).coerceAtLeast(0L)

  val usedPercentage: Float
    get() = if (totalCapacityBytes > 0) (usedBytes.toFloat() / totalCapacityBytes.toFloat()).coerceIn(0f, 1f) else 0f
}

data class DashboardConfig(
  val showStorageOverview: Boolean = true,
  val showQuickActions: Boolean = true,
  val showRecentFiles: Boolean = true,
  val showLocalStorageStatus: Boolean = true,
  val showOcrQuickSearch: Boolean = true,
  val showCategoryGrid: Boolean = true
)

