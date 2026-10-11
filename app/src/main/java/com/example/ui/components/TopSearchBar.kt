package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.SortOption
import com.example.data.model.ViewMode
import com.example.ui.theme.ThemeMode

@Composable
fun TopSearchBar(
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  activeOcrTag: String?,
  onSelectOcrTag: (String?) -> Unit,
  viewMode: ViewMode,
  onToggleViewMode: () -> Unit,
  sortOption: SortOption,
  onSelectSortOption: (SortOption) -> Unit,
  currentTheme: ThemeMode,
  onToggleTheme: () -> Unit,
  isTravelOffline: Boolean = false,
  onNavigateBack: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var showSortMenu by remember { mutableStateOf(false) }

  val popularOcrTags = listOf(
    "#Invoice", "#Receipt", "#Contract", "#IDCard", "#Travel", "#Tax", "#Notes", "#Confidential"
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
  ) {
    // Offline Travel Mode Banner if active
    if (isTravelOffline) {
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 8.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.AirplanemodeActive,
            contentDescription = "Travel Offline Mode",
            tint = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.size(18.dp)
          )
          Text(
            text = "Travel Offline Mode: Safe local cache & queued changes",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer
          )
        }
      }
    }

    // Search Input Field with action buttons
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      if (onNavigateBack != null) {
        IconButton(
          onClick = onNavigateBack,
          modifier = Modifier
            .size(40.dp)
            .testTag("search_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.onBackground
          )
        }
      }

      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = {
          Text(
            text = "Search files, OCR text, tags...",
            style = MaterialTheme.typography.bodyMedium
          )
        },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(
              onClick = { onSearchQueryChange("") },
              modifier = Modifier.testTag("clear_search_button")
            ) {
              Icon(Icons.Default.Clear, contentDescription = "Clear search")
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .weight(1f)
          .testTag("search_text_input")
      )

      // Grid / List Toggle
      IconButton(
        onClick = onToggleViewMode,
        modifier = Modifier.testTag("toggle_view_mode_button")
      ) {
        Icon(
          imageVector = if (viewMode == ViewMode.LIST) Icons.Default.GridView else Icons.Default.ViewList,
          contentDescription = "Toggle View Mode"
        )
      }

      // Sort Menu
      IconButton(
        onClick = { showSortMenu = true },
        modifier = Modifier.testTag("sort_menu_button")
      ) {
        Icon(
          imageVector = Icons.Default.FilterList,
          contentDescription = "Sort Options"
        )
        DropdownMenu(
          expanded = showSortMenu,
          onDismissRequest = { showSortMenu = false }
        ) {
          SortOption.entries.forEach { option ->
            DropdownMenuItem(
              text = { Text(option.label) },
              onClick = {
                onSelectSortOption(option)
                showSortMenu = false
              }
            )
          }
        }
      }

      // Theme Mode Toggle (Light / Dark)
      IconButton(
        onClick = onToggleTheme,
        modifier = Modifier.testTag("theme_toggle_button")
      ) {
        Icon(
          imageVector = if (currentTheme == ThemeMode.DARK) Icons.Default.LightMode else Icons.Default.DarkMode,
          contentDescription = "Toggle Theme"
        )
      }
    }

    // Horizontal OCR Smart Tag filter chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp)
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      FilterChip(
        selected = activeOcrTag == null,
        onClick = { onSelectOcrTag(null) },
        label = { Text("All", style = MaterialTheme.typography.labelSmall) },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
          selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.testTag("ocr_tag_all")
      )

      popularOcrTags.forEach { tag ->
        val isSelected = activeOcrTag == tag
        FilterChip(
          selected = isSelected,
          onClick = { onSelectOcrTag(if (isSelected) null else tag) },
          label = { Text(tag, style = MaterialTheme.typography.labelSmall) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
          ),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.testTag("ocr_tag_${tag.replace("#", "")}")
        )
      }
    }
  }
}
