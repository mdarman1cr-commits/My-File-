package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.FileItemEntity
import com.example.data.model.FileCategory
import com.example.data.model.ViewMode
import com.example.ui.components.BreadcrumbBar
import com.example.ui.components.CreateFileDialog
import com.example.ui.components.CreateFolderDialog
import com.example.ui.components.CreateZipDialog
import com.example.ui.components.ExtractZipDialog
import com.example.ui.components.FileDetailsDialog
import com.example.ui.components.FileItemGrid
import com.example.ui.components.FileItemRow
import com.example.ui.components.HighlightColorPickerDialog
import com.example.ui.components.RenameDialog
import com.example.ui.components.TopSearchBar
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.FileExplorerViewModel
import com.example.ui.viewmodel.ScreenDestination

@Composable
fun FilesBrowserScreen(
  viewModel: FileExplorerViewModel,
  onNavigateToScanner: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val files by viewModel.displayedFiles.collectAsStateWithLifecycle()

  var showCreateFolderDialog by remember { mutableStateOf(false) }
  var showCreateFileDialog by remember { mutableStateOf(false) }
  var showCreateZipDialog by remember { mutableStateOf(false) }
  var fileToRename by remember { mutableStateOf<FileItemEntity?>(null) }
  var fileToShowDetails by remember { mutableStateOf<FileItemEntity?>(null) }
  var fileToHighlight by remember { mutableStateOf<FileItemEntity?>(null) }
  var fileToExtractArchive by remember { mutableStateOf<FileItemEntity?>(null) }
  var isFabExpanded by remember { mutableStateOf(false) }

  // BackHandler to navigate up folder hierarchy or back to Dashboard
  BackHandler(enabled = true) {
    if (!viewModel.navigateUpFolder()) {
      viewModel.navigateTo(ScreenDestination.DASHBOARD)
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .testTag("files_browser_screen")
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Top Search Bar
      TopSearchBar(
        searchQuery = uiState.searchQuery,
        onSearchQueryChange = { viewModel.setSearchQuery(it) },
        activeOcrTag = uiState.activeOcrTagFilter,
        onSelectOcrTag = { viewModel.setActiveOcrTagFilter(it) },
        viewMode = uiState.viewMode,
        onToggleViewMode = { viewModel.toggleViewMode() },
        sortOption = uiState.sortOption,
        onSelectSortOption = { viewModel.setSortOption(it) },
        currentTheme = uiState.themeMode,
        onToggleTheme = {
          val next = if (uiState.themeMode == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK
          viewModel.setThemeMode(next)
        },
        onNavigateBack = {
          if (!viewModel.navigateUpFolder()) {
            viewModel.navigateTo(ScreenDestination.DASHBOARD)
          }
        }
      )

      // Breadcrumb Navigation
      BreadcrumbBar(
        breadcrumbs = uiState.breadcrumbs,
        onNavigateToBreadcrumb = { viewModel.navigateToBreadcrumb(it) },
        onNavigateUp = { viewModel.navigateUpFolder() },
        canNavigateUp = uiState.breadcrumbs.size > 1
      )

      // Trash bin empty button if viewing recycle bin
      if (uiState.selectedCategory == FileCategory.TRASH && files.isNotEmpty()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
          horizontalArrangement = Arrangement.End
        ) {
          OutlinedButton(
            onClick = { viewModel.emptyTrash() },
            modifier = Modifier.testTag("empty_trash_button")
          ) {
            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.size(6.dp))
            Text("Empty Recycle Bin")
          }
        }
      }

      // Empty State
      if (files.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.FolderOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(36.dp)
              )
            }
            Text(
              text = if (uiState.searchQuery.isNotBlank()) "No files match your query" else "This folder is empty",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (uiState.searchQuery.isNotBlank()) "Try searching by OCR keywords or clearing filters" else "Create a new folder or scan a document to get started",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (uiState.searchQuery.isBlank() && uiState.selectedCategory != FileCategory.TRASH) {
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                  onClick = { showCreateFolderDialog = true },
                  modifier = Modifier.testTag("empty_create_folder_button")
                ) {
                  Text("New Folder")
                }
                OutlinedButton(
                  onClick = onNavigateToScanner,
                  modifier = Modifier.testTag("empty_scan_button")
                ) {
                  Text("Scan Document")
                }
              }
            }
          }
        }
      } else {
        // Files list or grid
        if (uiState.viewMode == ViewMode.LIST) {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
          ) {
            items(files, key = { it.id }) { file ->
              FileItemRow(
                item = file,
                onClick = { viewModel.openFilePreview(file) },
                onToggleFavorite = { viewModel.toggleFavorite(file.id) },
                onRename = { fileToRename = file },
                onMoveToVault = { viewModel.moveIntoVault(file.id) },
                onExtractFromVault = { viewModel.extractFromVault(file.id) },
                onDelete = {
                  if (file.isTrash) viewModel.deletePermanently(file.id)
                  else viewModel.moveToTrash(file.id)
                },
                onRestore = { viewModel.restoreFromTrash(file.id) },
                onShowDetails = { fileToShowDetails = file },
                onSetHighlight = { fileToHighlight = file },
                onExtractZip = { fileToExtractArchive = file },
                onCompressToZip = { viewModel.compressSingleFile(file) }
              )
            }
          }
        } else {
          LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 110.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
          ) {
            items(files, key = { it.id }) { file ->
              FileItemGrid(
                item = file,
                onClick = { viewModel.openFilePreview(file) },
                onShowDetails = { fileToShowDetails = file }
              )
            }
          }
        }
      }
    }

    // Expandable FAB
    if (uiState.selectedCategory != FileCategory.TRASH) {
      Column(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(24.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        if (isFabExpanded) {
          SmallFloatingActionButton(
            onClick = {
              isFabExpanded = false
              onNavigateToScanner()
            },
            containerColor = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.testTag("fab_scan_action")
          ) {
            Icon(Icons.Default.DocumentScanner, contentDescription = "Scan Document")
          }
          SmallFloatingActionButton(
            onClick = {
              isFabExpanded = false
              showCreateZipDialog = true
            },
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier.testTag("fab_create_zip_action")
          ) {
            Icon(Icons.Default.Archive, contentDescription = "New ZIP Archive")
          }
          SmallFloatingActionButton(
            onClick = {
              isFabExpanded = false
              showCreateFileDialog = true
            },
            containerColor = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.testTag("fab_create_file_action")
          ) {
            Icon(Icons.Default.NoteAdd, contentDescription = "New File")
          }
          SmallFloatingActionButton(
            onClick = {
              isFabExpanded = false
              showCreateFolderDialog = true
            },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.testTag("fab_create_folder_action")
          ) {
            Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder")
          }
        }

        FloatingActionButton(
          onClick = { isFabExpanded = !isFabExpanded },
          containerColor = MaterialTheme.colorScheme.primary,
          modifier = Modifier.testTag("main_fab_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = "Add Item")
        }
      }
    }

    // Dialogs
    if (showCreateFolderDialog) {
      CreateFolderDialog(
        onDismiss = { showCreateFolderDialog = false },
        onConfirm = { viewModel.createNewFolder(it) }
      )
    }

    if (showCreateFileDialog) {
      CreateFileDialog(
        onDismiss = { showCreateFileDialog = false },
        onConfirm = { name, content -> viewModel.createNewTextFile(name, content) }
      )
    }

    if (showCreateZipDialog) {
      CreateZipDialog(
        onDismiss = { showCreateZipDialog = false },
        onConfirm = { zipName ->
          val currentIds = files.map { it.id }
          viewModel.compressFilesToZip(currentIds, zipName)
        }
      )
    }

    fileToHighlight?.let { target ->
      HighlightColorPickerDialog(
        currentColor = target.highlightColor,
        onDismiss = { fileToHighlight = null },
        onSelectColor = { colorKey ->
          viewModel.setFileHighlight(target.id, colorKey)
        }
      )
    }

    fileToExtractArchive?.let { target ->
      ExtractZipDialog(
        archiveName = target.name,
        onDismiss = { fileToExtractArchive = null },
        onConfirm = {
          viewModel.extractZipArchive(target)
        }
      )
    }

    fileToRename?.let { target ->
      RenameDialog(
        currentName = target.name,
        onDismiss = { fileToRename = null },
        onConfirm = { viewModel.renameFile(target.id, it) }
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
