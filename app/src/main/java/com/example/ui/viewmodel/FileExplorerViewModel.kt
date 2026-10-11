package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BackupEntryEntity
import com.example.data.local.FileItemEntity
import com.example.data.model.DashboardConfig
import com.example.data.model.FileCategory
import com.example.data.model.SortOption
import com.example.data.model.StorageBreakdown
import com.example.data.model.ViewMode
import com.example.data.ocr.DocumentOcrEngine
import com.example.data.ocr.OcrScanResult
import com.example.data.repository.FileRepository
import com.example.data.security.EncryptionManager
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenDestination {
  DASHBOARD, FILES, SCANNER, VAULT, BACKUPS, SETTINGS
}

data class Breadcrumb(
  val folderId: Long?,
  val title: String
)

data class UiState(
  val currentScreen: ScreenDestination = ScreenDestination.DASHBOARD,
  val currentFolderId: Long? = null,
  val breadcrumbs: List<Breadcrumb> = listOf(Breadcrumb(null, "Internal Storage")),
  val selectedCategory: FileCategory = FileCategory.ALL,
  val sortOption: SortOption = SortOption.DATE_DESC,
  val viewMode: ViewMode = ViewMode.LIST,
  val searchQuery: String = "",
  val activeOcrTagFilter: String? = null,
  val isVaultUnlocked: Boolean = false,
  val vaultMasterPin: String = "1234",
  val themeMode: ThemeMode = ThemeMode.SYSTEM,
  val dashboardConfig: DashboardConfig = DashboardConfig(),
  val previewFile: FileItemEntity? = null,
  val previewDecryptedContent: String? = null,
  val isScannerActive: Boolean = false,
  val selectedDocumentFilter: String = "HIGH_CONTRAST",
  val selectedHighlightFilter: String? = null
)

class FileExplorerViewModel(application: Application) : AndroidViewModel(application) {

  val repository = FileRepository(application)

  private val _uiState = MutableStateFlow(UiState())
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  val storageBreakdown: StateFlow<StorageBreakdown> = repository.storageBreakdownFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StorageBreakdown())

  val backupEntries: StateFlow<List<BackupEntryEntity>> = repository.getAllBackups()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val recentFiles: StateFlow<List<FileItemEntity>> = repository.getRecentFiles(8)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _snackBarEvent = MutableSharedFlow<String>()
  val snackBarEvent = _snackBarEvent.asSharedFlow()

  // Dynamic files in current context
  @OptIn(ExperimentalCoroutinesApi::class)
  val displayedFiles: StateFlow<List<FileItemEntity>> = combine(
    _uiState,
    _uiState.flatMapLatest { state ->
      when {
        state.searchQuery.isNotBlank() -> repository.searchFiles(state.searchQuery)
        state.selectedCategory == FileCategory.VAULT -> repository.getVaultItems()
        state.selectedCategory == FileCategory.TRASH -> repository.getTrashItems()
        state.selectedCategory == FileCategory.FAVORITES -> repository.getFavoriteItems()
        state.selectedCategory != FileCategory.ALL -> repository.getItemsByCategory(state.selectedCategory.name)
        else -> repository.getItemsInFolder(state.currentFolderId)
      }
    }
  ) { state, rawList ->
    var filtered = rawList

    // Filter by active OCR Tag if selected
    if (!state.activeOcrTagFilter.isNullOrBlank()) {
      filtered = filtered.filter { it.ocrTags.contains(state.activeOcrTagFilter, ignoreCase = true) }
    }

    // Filter by highlight color if selected
    if (!state.selectedHighlightFilter.isNullOrBlank()) {
      filtered = filtered.filter { it.highlightColor.equals(state.selectedHighlightFilter, ignoreCase = true) }
    }

    // Sort list
    when (state.sortOption) {
      SortOption.NAME_ASC -> filtered.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
      SortOption.NAME_DESC -> filtered.sortedWith(compareBy({ !it.isDirectory }, { -it.name.lowercase().hashCode() }))
      SortOption.DATE_DESC -> filtered.sortedWith(compareBy({ !it.isDirectory }, { -it.updatedAt }))
      SortOption.DATE_ASC -> filtered.sortedWith(compareBy({ !it.isDirectory }, { it.updatedAt }))
      SortOption.SIZE_DESC -> filtered.sortedWith(compareBy({ !it.isDirectory }, { -it.sizeBytes }))
      SortOption.SIZE_ASC -> filtered.sortedWith(compareBy({ !it.isDirectory }, { it.sizeBytes }))
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  init {
    viewModelScope.launch {
      repository.initializeSampleDataIfNeeded()
    }
  }

  fun navigateTo(screen: ScreenDestination) {
    _uiState.value = _uiState.value.copy(currentScreen = screen)
  }

  fun openFolder(folder: FileItemEntity) {
    val newBreadcrumbs = _uiState.value.breadcrumbs + Breadcrumb(folder.id, folder.name)
    _uiState.value = _uiState.value.copy(
      currentFolderId = folder.id,
      breadcrumbs = newBreadcrumbs,
      currentScreen = ScreenDestination.FILES,
      selectedCategory = FileCategory.ALL
    )
  }

  fun navigateToBreadcrumb(target: Breadcrumb) {
    val index = _uiState.value.breadcrumbs.indexOfFirst { it.folderId == target.folderId }
    val newBreadcrumbs = if (index >= 0) {
      _uiState.value.breadcrumbs.take(index + 1)
    } else {
      listOf(Breadcrumb(null, "Internal Storage"))
    }
    _uiState.value = _uiState.value.copy(
      currentFolderId = target.folderId,
      breadcrumbs = newBreadcrumbs,
      currentScreen = ScreenDestination.FILES
    )
  }

  fun navigateUpFolder(): Boolean {
    val currentBreadcrumbs = _uiState.value.breadcrumbs
    if (currentBreadcrumbs.size > 1) {
      val newBreadcrumbs = currentBreadcrumbs.dropLast(1)
      val parentBreadcrumb = newBreadcrumbs.last()
      _uiState.value = _uiState.value.copy(
        currentFolderId = parentBreadcrumb.folderId,
        breadcrumbs = newBreadcrumbs
      )
      return true
    }
    return false
  }

  fun selectCategory(category: FileCategory) {
    _uiState.value = _uiState.value.copy(
      selectedCategory = category,
      currentScreen = if (category == FileCategory.VAULT) ScreenDestination.VAULT else ScreenDestination.FILES,
      currentFolderId = null,
      breadcrumbs = listOf(Breadcrumb(null, category.label))
    )
  }

  fun setSearchQuery(query: String) {
    _uiState.value = _uiState.value.copy(searchQuery = query)
  }

  fun setActiveOcrTagFilter(tag: String?) {
    _uiState.value = _uiState.value.copy(activeOcrTagFilter = tag)
  }

  fun setSortOption(sort: SortOption) {
    _uiState.value = _uiState.value.copy(sortOption = sort)
  }

  fun toggleViewMode() {
    val newMode = if (_uiState.value.viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST
    _uiState.value = _uiState.value.copy(viewMode = newMode)
  }

  fun setThemeMode(mode: ThemeMode) {
    _uiState.value = _uiState.value.copy(themeMode = mode)
  }

  fun updateDashboardConfig(newConfig: DashboardConfig) {
    _uiState.value = _uiState.value.copy(dashboardConfig = newConfig)
  }

  // Vault Unlock & Lock
  fun unlockVault(pin: String): Boolean {
    val isValid = EncryptionManager.verifyPin(getApplication(), pin)
    if (isValid) {
      _uiState.value = _uiState.value.copy(
        isVaultUnlocked = true,
        vaultMasterPin = pin
      )
      notifyUser("Safe Vault Unlocked (AES-256 Active)")
      return true
    } else {
      notifyUser("Incorrect PIN. Please try again.")
      return false
    }
  }

  fun lockVault() {
    _uiState.value = _uiState.value.copy(isVaultUnlocked = false)
    notifyUser("Safe Vault Locked")
  }

  fun changeMasterPin(oldPin: String, newPin: String): Boolean {
    if (EncryptionManager.verifyPin(getApplication(), oldPin)) {
      EncryptionManager.setMasterPin(getApplication(), newPin)
      _uiState.value = _uiState.value.copy(vaultMasterPin = newPin)
      notifyUser("Master PIN successfully updated")
      return true
    } else {
      notifyUser("Old PIN is incorrect")
      return false
    }
  }

  // File Operations
  fun createNewFolder(name: String) {
    viewModelScope.launch {
      val isInVault = _uiState.value.currentScreen == ScreenDestination.VAULT
      repository.createFolder(
        name = name,
        parentFolderId = _uiState.value.currentFolderId,
        isInVault = isInVault
      )
      notifyUser("Folder '$name' created")
    }
  }

  fun createNewTextFile(name: String, content: String) {
    viewModelScope.launch {
      val isInVault = _uiState.value.currentScreen == ScreenDestination.VAULT
      val extension = if (name.contains(".")) name.substringAfterLast(".") else "txt"
      val fileName = if (name.contains(".")) name else "$name.txt"
      repository.createFile(
        name = fileName,
        extension = extension,
        content = content,
        category = "DOCUMENTS",
        parentFolderId = _uiState.value.currentFolderId,
        isInVault = isInVault
      )
      notifyUser("File '$fileName' created")
    }
  }

  fun saveScannedDocument(
    scanResult: OcrScanResult,
    filter: String
  ) {
    viewModelScope.launch {
      val tagsFormatted = scanResult.autoTags.joinToString(",")
      repository.createFile(
        name = scanResult.documentTitle,
        extension = "pdf",
        content = scanResult.extractedText,
        category = "SCANS",
        parentFolderId = _uiState.value.currentFolderId,
        isInVault = false,
        ocrTags = tagsFormatted,
        filterApplied = filter
      )
      notifyUser("Scanned document saved & indexed with ${scanResult.autoTags.size} OCR tags")
      navigateTo(ScreenDestination.FILES)
    }
  }

  fun renameFile(fileId: Long, newName: String) {
    viewModelScope.launch {
      repository.renameItem(fileId, newName)
      notifyUser("Renamed to '$newName'")
    }
  }

  fun toggleFavorite(fileId: Long) {
    viewModelScope.launch {
      repository.toggleFavorite(fileId)
    }
  }

  fun moveToTrash(fileId: Long) {
    viewModelScope.launch {
      repository.moveToTrash(fileId)
      notifyUser("Moved to Recycle Bin")
    }
  }

  fun restoreFromTrash(fileId: Long) {
    viewModelScope.launch {
      repository.restoreFromTrash(fileId)
      notifyUser("Item restored")
    }
  }

  fun deletePermanently(fileId: Long) {
    viewModelScope.launch {
      repository.deletePermanently(fileId)
      notifyUser("Permanently deleted")
    }
  }

  fun emptyTrash() {
    viewModelScope.launch {
      repository.emptyTrash()
      notifyUser("Recycle Bin emptied")
    }
  }

  fun setHighlightFilter(color: String?) {
    _uiState.value = _uiState.value.copy(selectedHighlightFilter = color)
  }

  fun setFileHighlight(fileId: Long, color: String?) {
    viewModelScope.launch {
      repository.setHighlightColor(fileId, color)
      val label = when (color) {
        "YELLOW" -> "Yellow (Important)"
        "RED" -> "Red (Urgent)"
        "GREEN" -> "Green (Done)"
        "BLUE" -> "Blue (Work)"
        "PURPLE" -> "Purple (Personal)"
        else -> "Removed"
      }
      notifyUser("Highlight set: $label")
    }
  }

  fun extractZipArchive(file: FileItemEntity) {
    viewModelScope.launch {
      val count = repository.extractZipArchive(file.id, _uiState.value.currentFolderId)
      notifyUser("Extracted $count files from '${file.name}' into folder '${file.name.substringBeforeLast(".")}_Extracted'")
    }
  }

  fun compressFilesToZip(itemIds: List<Long>, zipName: String) {
    viewModelScope.launch {
      val name = if (zipName.endsWith(".zip", ignoreCase = true)) zipName else "$zipName.zip"
      repository.compressFilesToZip(itemIds, name, _uiState.value.currentFolderId)
      notifyUser("Created archive '$name' with ${itemIds.size} files")
    }
  }

  fun compressSingleFile(file: FileItemEntity) {
    val zipName = "${file.name.substringBeforeLast(".")}.zip"
    compressFilesToZip(listOf(file.id), zipName)
  }

  fun moveIntoVault(fileId: Long) {
    viewModelScope.launch {
      repository.toggleVault(fileId, moveToVault = true, pin = _uiState.value.vaultMasterPin)
      notifyUser("Encrypted and secured inside Safe Vault")
    }
  }

  fun extractFromVault(fileId: Long) {
    viewModelScope.launch {
      repository.toggleVault(fileId, moveToVault = false, pin = _uiState.value.vaultMasterPin)
      notifyUser("Extracted from Safe Vault to standard storage")
    }
  }

  // File Preview & Content Editor
  fun openFilePreview(file: FileItemEntity) {
    if (file.isDirectory) {
      openFolder(file)
      return
    }

    val decrypted = if (file.isInVault && !file.content.isNullOrEmpty()) {
      repository.decryptVaultContent(file.content, _uiState.value.vaultMasterPin)
    } else {
      file.content
    }

    _uiState.value = _uiState.value.copy(
      previewFile = file,
      previewDecryptedContent = decrypted
    )
  }

  fun closeFilePreview() {
    _uiState.value = _uiState.value.copy(previewFile = null, previewDecryptedContent = null)
  }

  fun saveFileContent(fileId: Long, newContent: String) {
    viewModelScope.launch {
      repository.updateFileContent(fileId, newContent)
      _uiState.value = _uiState.value.copy(previewDecryptedContent = newContent)
      notifyUser("Changes saved successfully")
    }
  }

  // Encrypted Backup
  fun createEncryptedBackup(passphrase: String, hint: String) {
    viewModelScope.launch {
      val result = repository.createEncryptedBackup(passphrase, hint)
      if (result.isSuccess) {
        val backup = result.getOrThrow()
        notifyUser("Encrypted backup '${backup.filename}' created successfully")
      } else {
        notifyUser("Backup failed: ${result.exceptionOrNull()?.message}")
      }
    }
  }

  fun deleteBackup(backup: BackupEntryEntity) {
    viewModelScope.launch {
      repository.deleteBackup(backup)
      notifyUser("Backup removed")
    }
  }

  private fun notifyUser(message: String) {
    viewModelScope.launch {
      _snackBarEvent.emit(message)
    }
  }
}
