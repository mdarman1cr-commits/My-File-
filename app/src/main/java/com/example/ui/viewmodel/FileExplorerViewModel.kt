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
import com.example.data.storage.RealDeviceFileManager
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenDestination {
  DASHBOARD, FILES, SCANNER, VAULT, BACKUPS, SETTINGS
}

data class Breadcrumb(
  val folderId: Long? = null,
  val path: String = "",
  val title: String
)

data class UiState(
  val currentScreen: ScreenDestination = ScreenDestination.DASHBOARD,
  val currentFolderId: Long? = null,
  val currentFolderPath: String = RealDeviceFileManager.rootStoragePath,
  val breadcrumbs: List<Breadcrumb> = listOf(
    Breadcrumb(null, RealDeviceFileManager.rootStoragePath, "Internal Storage")
  ),
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
  val selectedHighlightFilter: String? = null,
  val hasStoragePermission: Boolean = false,
  val isLoadingFiles: Boolean = false
)

class FileExplorerViewModel(application: Application) : AndroidViewModel(application) {

  val repository = FileRepository(application)

  private val _uiState = MutableStateFlow(
    UiState(hasStoragePermission = repository.hasStoragePermission())
  )
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  private val _storageBreakdown = MutableStateFlow(StorageBreakdown())
  val storageBreakdown: StateFlow<StorageBreakdown> = _storageBreakdown.asStateFlow()

  val backupEntries: StateFlow<List<BackupEntryEntity>> = repository.getAllBackups()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val recentFiles: StateFlow<List<FileItemEntity>> = repository.getRecentFiles(8)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _snackBarEvent = MutableSharedFlow<String>()
  val snackBarEvent = _snackBarEvent.asSharedFlow()

  private val _realDeviceFiles = MutableStateFlow<List<FileItemEntity>>(emptyList())

  // Dynamic files in current context
  @OptIn(ExperimentalCoroutinesApi::class)
  val displayedFiles: StateFlow<List<FileItemEntity>> = combine(
    _uiState,
    _realDeviceFiles,
    _uiState.flatMapLatest { state ->
      when {
        state.selectedCategory == FileCategory.VAULT -> repository.getVaultItems()
        state.selectedCategory == FileCategory.TRASH -> repository.getTrashItems()
        state.selectedCategory == FileCategory.FAVORITES -> repository.getFavoriteItems()
        else -> flowOf(emptyList())
      }
    }
  ) { state, realList, roomList ->
    var raw = when (state.selectedCategory) {
      FileCategory.VAULT, FileCategory.TRASH, FileCategory.FAVORITES -> roomList
      else -> realList
    }

    // Filter by active OCR Tag if selected
    if (!state.activeOcrTagFilter.isNullOrBlank()) {
      raw = raw.filter { it.ocrTags.contains(state.activeOcrTagFilter, ignoreCase = true) }
    }

    // Filter by highlight color if selected
    if (!state.selectedHighlightFilter.isNullOrBlank()) {
      raw = raw.filter { it.highlightColor.equals(state.selectedHighlightFilter, ignoreCase = true) }
    }

    // Sort list
    when (state.sortOption) {
      SortOption.NAME_ASC -> raw.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
      SortOption.NAME_DESC -> raw.sortedWith(compareBy({ !it.isDirectory }, { -it.name.lowercase().hashCode() }))
      SortOption.DATE_DESC -> raw.sortedWith(compareBy({ !it.isDirectory }, { -it.updatedAt }))
      SortOption.DATE_ASC -> raw.sortedWith(compareBy({ !it.isDirectory }, { it.updatedAt }))
      SortOption.SIZE_DESC -> raw.sortedWith(compareBy({ !it.isDirectory }, { -it.sizeBytes }))
      SortOption.SIZE_ASC -> raw.sortedWith(compareBy({ !it.isDirectory }, { it.sizeBytes }))
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  init {
    viewModelScope.launch {
      repository.initializeSampleDataIfNeeded()
      checkPermissionState()
      refreshStorageBreakdown()
      loadCurrentFiles()
    }
  }

  fun checkPermissionState() {
    val granted = repository.hasStoragePermission()
    _uiState.value = _uiState.value.copy(hasStoragePermission = granted)
    if (granted) {
      refreshStorageBreakdown()
      loadCurrentFiles()
    }
  }

  fun requestStoragePermission() {
    repository.requestStoragePermission()
  }

  fun openAppSettings() {
    repository.openAppSettings()
  }

  fun refreshStorageBreakdown() {
    viewModelScope.launch(Dispatchers.IO) {
      val breakdown = RealDeviceFileManager.getStorageBreakdown(getApplication())
      _storageBreakdown.value = breakdown
    }
  }

  fun loadCurrentFiles() {
    val state = _uiState.value
    if (state.selectedCategory in listOf(FileCategory.VAULT, FileCategory.TRASH, FileCategory.FAVORITES)) {
      return
    }

    viewModelScope.launch(Dispatchers.IO) {
      _uiState.value = _uiState.value.copy(isLoadingFiles = true)
      val files = when {
        !repository.hasStoragePermission() -> emptyList()
        state.searchQuery.isNotBlank() -> repository.searchRealFiles(state.searchQuery, state.currentFolderPath)
        state.selectedCategory != FileCategory.ALL -> repository.getRealCategoryFiles(state.selectedCategory)
        else -> repository.getRealDeviceFiles(state.currentFolderPath)
      }
      _realDeviceFiles.value = files
      _uiState.value = _uiState.value.copy(isLoadingFiles = false)
    }
  }

  fun navigateTo(screen: ScreenDestination) {
    _uiState.value = _uiState.value.copy(currentScreen = screen)
    if (screen == ScreenDestination.DASHBOARD) {
      refreshStorageBreakdown()
    } else if (screen == ScreenDestination.FILES) {
      loadCurrentFiles()
    }
  }

  fun openFolder(folder: FileItemEntity) {
    val folderPath = if (folder.path.isNotBlank()) folder.path else folder.name
    val newBreadcrumbs = _uiState.value.breadcrumbs + Breadcrumb(
      folderId = folder.id,
      path = folderPath,
      title = folder.name
    )
    _uiState.value = _uiState.value.copy(
      currentFolderId = folder.id,
      currentFolderPath = folderPath,
      breadcrumbs = newBreadcrumbs,
      currentScreen = ScreenDestination.FILES,
      selectedCategory = FileCategory.ALL,
      searchQuery = ""
    )
    loadCurrentFiles()
  }

  fun navigateToBreadcrumb(target: Breadcrumb) {
    val index = _uiState.value.breadcrumbs.indexOfFirst {
      if (target.path.isNotBlank()) it.path == target.path else it.folderId == target.folderId
    }
    val newBreadcrumbs = if (index >= 0) {
      _uiState.value.breadcrumbs.take(index + 1)
    } else {
      listOf(Breadcrumb(null, RealDeviceFileManager.rootStoragePath, "Internal Storage"))
    }
    _uiState.value = _uiState.value.copy(
      currentFolderId = target.folderId,
      currentFolderPath = if (target.path.isNotBlank()) target.path else RealDeviceFileManager.rootStoragePath,
      breadcrumbs = newBreadcrumbs,
      currentScreen = ScreenDestination.FILES
    )
    loadCurrentFiles()
  }

  fun navigateUpFolder(): Boolean {
    val currentBreadcrumbs = _uiState.value.breadcrumbs
    if (currentBreadcrumbs.size > 1) {
      val newBreadcrumbs = currentBreadcrumbs.dropLast(1)
      val parentBreadcrumb = newBreadcrumbs.last()
      _uiState.value = _uiState.value.copy(
        currentFolderId = parentBreadcrumb.folderId,
        currentFolderPath = if (parentBreadcrumb.path.isNotBlank()) parentBreadcrumb.path else RealDeviceFileManager.rootStoragePath,
        breadcrumbs = newBreadcrumbs
      )
      loadCurrentFiles()
      return true
    }
    return false
  }

  fun selectCategory(category: FileCategory) {
    _uiState.value = _uiState.value.copy(
      selectedCategory = category,
      currentFolderId = null,
      currentFolderPath = RealDeviceFileManager.rootStoragePath,
      breadcrumbs = listOf(
        Breadcrumb(null, RealDeviceFileManager.rootStoragePath, "Internal Storage"),
        Breadcrumb(null, "", category.label)
      ),
      searchQuery = ""
    )
    loadCurrentFiles()
  }

  fun setSortOption(sortOption: SortOption) {
    _uiState.value = _uiState.value.copy(sortOption = sortOption)
  }

  fun setViewMode(viewMode: ViewMode) {
    _uiState.value = _uiState.value.copy(viewMode = viewMode)
  }

  fun toggleViewMode() {
    val next = if (_uiState.value.viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST
    _uiState.value = _uiState.value.copy(viewMode = next)
  }

  fun setSearchQuery(query: String) {
    _uiState.value = _uiState.value.copy(searchQuery = query)
    loadCurrentFiles()
  }

  fun setActiveOcrTagFilter(tag: String?) {
    _uiState.value = _uiState.value.copy(activeOcrTagFilter = tag)
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
      if (isInVault) {
        repository.createFolder(
          name = name,
          parentFolderId = _uiState.value.currentFolderId,
          isInVault = true
        )
      } else {
        val created = repository.createRealFolder(_uiState.value.currentFolderPath, name)
        if (created) loadCurrentFiles()
      }
      notifyUser("Folder '$name' created")
    }
  }

  fun createNewTextFile(name: String, content: String) {
    viewModelScope.launch {
      val isInVault = _uiState.value.currentScreen == ScreenDestination.VAULT
      val extension = if (name.contains(".")) name.substringAfterLast(".") else "txt"
      val fileName = if (name.contains(".")) name else "$name.txt"

      if (isInVault) {
        repository.createFile(
          name = fileName,
          extension = extension,
          content = content,
          category = "DOCUMENTS",
          parentFolderId = _uiState.value.currentFolderId,
          isInVault = true
        )
      } else {
        val created = repository.createRealTextFile(_uiState.value.currentFolderPath, fileName, content)
        if (created) loadCurrentFiles()
      }
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

  fun renameFile(file: FileItemEntity, newName: String) {
    viewModelScope.launch {
      if (file.path.isNotBlank()) {
        val newPath = repository.renameRealFile(file.path, newName)
        if (newPath != null) {
          loadCurrentFiles()
          notifyUser("Renamed to '$newName'")
        } else {
          notifyUser("Rename failed")
        }
      } else {
        repository.renameItem(file.id, newName)
        notifyUser("Renamed to '$newName'")
      }
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

  fun deletePermanently(file: FileItemEntity) {
    viewModelScope.launch {
      if (file.path.isNotBlank()) {
        val deleted = repository.deleteRealFile(file.path)
        if (deleted) {
          loadCurrentFiles()
          notifyUser("Permanently deleted '${file.name}'")
        } else {
          notifyUser("Could not delete file")
        }
      } else {
        repository.deletePermanently(file.id)
        loadCurrentFiles()
        notifyUser("Permanently deleted")
      }
    }
  }

  fun deletePermanently(fileId: Long) {
    val found = _realDeviceFiles.value.find { it.id == fileId }
    if (found != null) {
      deletePermanently(found)
    } else {
      viewModelScope.launch {
        repository.deletePermanently(fileId)
        loadCurrentFiles()
        notifyUser("Permanently deleted")
      }
    }
  }

  fun renameFile(fileId: Long, newName: String) {
    val found = _realDeviceFiles.value.find { it.id == fileId }
    if (found != null) {
      renameFile(found, newName)
    } else {
      viewModelScope.launch {
        repository.renameItem(fileId, newName)
        loadCurrentFiles()
        notifyUser("Renamed to '$newName'")
      }
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
      notifyUser("Extracted $count files from '${file.name}'")
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

    // If it's a real device file and not in vault
    if (file.path.isNotBlank() && !file.isInVault) {
      val ext = file.extension.lowercase()
      val isTextFile = ext in listOf("txt", "md", "json", "xml", "csv", "html", "log", "kt", "java", "py", "js")
      if (isTextFile) {
        viewModelScope.launch(Dispatchers.IO) {
          val text = repository.readRealFileText(file.path) ?: ""
          _uiState.value = _uiState.value.copy(
            previewFile = file,
            previewDecryptedContent = text
          )
        }
      } else {
        // Launch external app
        val opened = repository.openFileExternally(file.path)
        if (!opened) {
          _uiState.value = _uiState.value.copy(
            previewFile = file,
            previewDecryptedContent = "[Opening with default system viewer...]"
          )
        }
      }
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

  fun saveFileContent(file: FileItemEntity, newContent: String) {
    viewModelScope.launch(Dispatchers.IO) {
      if (file.path.isNotBlank() && !file.isInVault) {
        repository.saveRealFileText(file.path, newContent)
        _uiState.value = _uiState.value.copy(previewDecryptedContent = newContent)
        notifyUser("Saved '${file.name}' to storage")
      } else {
        repository.updateFileContent(file.id, newContent)
        _uiState.value = _uiState.value.copy(previewDecryptedContent = newContent)
        notifyUser("Changes saved successfully")
      }
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
