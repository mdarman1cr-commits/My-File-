package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.DocumentViewerDialog
import com.example.ui.screens.BackupScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DocScannerScreen
import com.example.ui.screens.FilesBrowserScreen
import com.example.ui.screens.SecureVaultScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FileExplorerViewModel
import com.example.ui.viewmodel.ScreenDestination
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

  private val viewModel: FileExplorerViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val uiState by viewModel.uiState.collectAsStateWithLifecycle()
      val snackbarHostState = remember { SnackbarHostState() }

      LaunchedEffect(Unit) {
        viewModel.snackBarEvent.collectLatest { message ->
          snackbarHostState.showSnackbar(message)
        }
      }

      MyApplicationTheme(themeMode = uiState.themeMode) {
        Scaffold(
          contentWindowInsets = WindowInsets.safeDrawing,
          bottomBar = {
            if (uiState.currentScreen != ScreenDestination.DASHBOARD) {
              AppBottomNavigationBar(
                currentScreen = uiState.currentScreen,
                onSelectScreen = { viewModel.navigateTo(it) }
              )
            }
          },
          snackbarHost = { SnackbarHost(snackbarHostState) },
          modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          ) {
            AnimatedContent(
              targetState = uiState.currentScreen,
              transitionSpec = { fadeIn() togetherWith fadeOut() },
              label = "screen_transition"
            ) { targetScreen ->
              when (targetScreen) {
                ScreenDestination.DASHBOARD -> DashboardScreen(
                  viewModel = viewModel,
                  onNavigateToFiles = { viewModel.navigateTo(ScreenDestination.FILES) },
                  onNavigateToScanner = { viewModel.navigateTo(ScreenDestination.SCANNER) },
                  onNavigateToVault = { viewModel.navigateTo(ScreenDestination.VAULT) },
                  onNavigateToBackups = { viewModel.navigateTo(ScreenDestination.BACKUPS) },
                  onNavigateToSettings = { viewModel.navigateTo(ScreenDestination.SETTINGS) },
                  onOpenSearch = {
                    viewModel.selectCategory(com.example.data.model.FileCategory.ALL)
                    viewModel.navigateTo(ScreenDestination.FILES)
                  }
                )
                ScreenDestination.FILES -> FilesBrowserScreen(
                  viewModel = viewModel,
                  onNavigateToScanner = { viewModel.navigateTo(ScreenDestination.SCANNER) }
                )
                ScreenDestination.SCANNER -> DocScannerScreen(
                  viewModel = viewModel,
                  onDismiss = { viewModel.navigateTo(ScreenDestination.DASHBOARD) }
                )
                ScreenDestination.VAULT -> SecureVaultScreen(
                  viewModel = viewModel,
                  onNavigateBack = { viewModel.navigateTo(ScreenDestination.DASHBOARD) }
                )
                ScreenDestination.BACKUPS -> BackupScreen(
                  viewModel = viewModel,
                  onNavigateBack = { viewModel.navigateTo(ScreenDestination.DASHBOARD) }
                )
                ScreenDestination.SETTINGS -> SettingsScreen(
                  viewModel = viewModel,
                  onNavigateBack = { viewModel.navigateTo(ScreenDestination.DASHBOARD) }
                )
              }
            }

            // File content preview & editor dialog
            uiState.previewFile?.let { previewItem ->
              DocumentViewerDialog(
                item = previewItem,
                content = uiState.previewDecryptedContent,
                onDismiss = { viewModel.closeFilePreview() },
                onSaveContent = { updatedText ->
                  viewModel.saveFileContent(previewItem, updatedText)
                }
              )
            }
          }
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    viewModel.checkPermissionState()
  }
}

private data class NavItem(
  val destination: ScreenDestination,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String
)

@Composable
private fun AppBottomNavigationBar(
  currentScreen: ScreenDestination,
  onSelectScreen: (ScreenDestination) -> Unit
) {
  val items = listOf(
    NavItem(
      destination = ScreenDestination.DASHBOARD,
      label = "Home",
      selectedIcon = Icons.Filled.Dashboard,
      unselectedIcon = Icons.Outlined.Dashboard,
      testTag = "nav_dashboard"
    ),
    NavItem(
      destination = ScreenDestination.FILES,
      label = "Files",
      selectedIcon = Icons.Filled.Folder,
      unselectedIcon = Icons.Outlined.Folder,
      testTag = "nav_files"
    ),
    NavItem(
      destination = ScreenDestination.SCANNER,
      label = "Scanner",
      selectedIcon = Icons.Filled.DocumentScanner,
      unselectedIcon = Icons.Outlined.DocumentScanner,
      testTag = "nav_scanner"
    ),
    NavItem(
      destination = ScreenDestination.VAULT,
      label = "Safe Vault",
      selectedIcon = Icons.Filled.Lock,
      unselectedIcon = Icons.Outlined.Lock,
      testTag = "nav_vault"
    ),
    NavItem(
      destination = ScreenDestination.SETTINGS,
      label = "Settings",
      selectedIcon = Icons.Filled.Settings,
      unselectedIcon = Icons.Outlined.Settings,
      testTag = "nav_settings"
    )
  )

  NavigationBar(
    windowInsets = WindowInsets.navigationBars,
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = Modifier.testTag("bottom_navigation_bar")
  ) {
    items.forEach { item ->
      val isSelected = currentScreen == item.destination
      NavigationBarItem(
        selected = isSelected,
        onClick = { onSelectScreen(item.destination) },
        icon = {
          Icon(
            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.label,
            modifier = Modifier.size(22.dp)
          )
        },
        label = {
          Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
          )
        },
        modifier = Modifier.testTag(item.testTag)
      )
    }
  }
}
