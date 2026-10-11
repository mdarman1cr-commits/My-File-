package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.ocr.DocumentOcrEngine
import com.example.data.ocr.OcrScanResult
import com.example.ui.theme.ColorScan
import com.example.ui.theme.EmeraldTertiaryDark
import com.example.ui.viewmodel.FileExplorerViewModel
import com.example.ui.viewmodel.ScreenDestination

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DocScannerScreen(
  viewModel: FileExplorerViewModel,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val presets = DocumentOcrEngine.SAMPLE_SCAN_PRESETS
  var selectedPresetIndex by remember { mutableStateOf(0) }
  var documentFilter by remember { mutableStateOf("HIGH_CONTRAST") }
  var rawText by remember { mutableStateOf(presets[0].second) }
  var customTitle by remember { mutableStateOf(presets[0].first.replace(" ", "_") + ".pdf") }

  var currentResult by remember(rawText, customTitle) {
    mutableStateOf(DocumentOcrEngine.analyzeAndExtract(rawText, customTitle))
  }

  val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
  val laserOffset by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(2200, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "laser"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
      .testTag("doc_scanner_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(ColorScan.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.DocumentScanner,
            contentDescription = null,
            tint = ColorScan,
            modifier = Modifier.size(20.dp)
          )
        }
        Column {
          Text("Document Scanner", style = MaterialTheme.typography.titleLarge)
          Text("Paperless OCR & Smart Tagging", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }

      IconButton(onClick = onDismiss) {
        Icon(Icons.Default.Close, contentDescription = "Close Scanner")
      }
    }

    // Scanner Viewfinder with simulated document boundary & animated laser line
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
      modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
        .testTag("scanner_viewfinder")
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(14.dp)
          .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
          .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
      ) {
        // Document Simulation Graphic
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = currentResult.detectedDocType,
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary
            )
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = EmeraldTertiaryDark,
                modifier = Modifier.size(14.dp)
              )
              Text(
                text = "OCR Active (98%)",
                style = MaterialTheme.typography.labelSmall,
                color = EmeraldTertiaryDark
              )
            }
          }

          Text(
            text = rawText.take(110) + "...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Filter: $documentFilter",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.outline
            )
            Text(
              text = "Alignment: Optimal",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.outline
            )
          }
        }

        // Laser Scan Bar
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .padding(top = (laserOffset * 130).dp)
            .background(MaterialTheme.colorScheme.primary)
        )
      }
    }

    // Preset Selector
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text("Sample Documents & Capture Presets", style = MaterialTheme.typography.labelMedium)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        presets.forEachIndexed { index, preset ->
          val isSelected = selectedPresetIndex == index
          FilterChip(
            selected = isSelected,
            onClick = {
              selectedPresetIndex = index
              rawText = preset.second
              customTitle = preset.first.replace(" ", "_") + ".pdf"
              currentResult = DocumentOcrEngine.analyzeAndExtract(rawText, customTitle)
            },
            label = { Text(preset.first, style = MaterialTheme.typography.labelSmall) },
            shape = RoundedCornerShape(12.dp)
          )
        }
      }
    }

    // Document Filters
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text("Document Enhancement Filter", style = MaterialTheme.typography.labelMedium)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val filters = listOf(
          "HIGH_CONTRAST" to "Magic Contrast",
          "BW_CLEAN" to "B&W Clean",
          "GRAYSCALE" to "Grayscale",
          "ORIGINAL" to "Original"
        )
        filters.forEach { (key, label) ->
          FilterChip(
            selected = documentFilter == key,
            onClick = { documentFilter = key },
            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    // Automated OCR Tags Section
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
          )
          Text(
            text = "Automated OCR Tags (Lightning-Fast Search)",
            style = MaterialTheme.typography.titleSmall
          )
        }

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          currentResult.autoTags.forEach { tag ->
            SuggestionChip(
              onClick = {},
              label = { Text(tag, style = MaterialTheme.typography.labelMedium) },
              colors = SuggestionChipDefaults.suggestionChipColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
              ),
              shape = RoundedCornerShape(10.dp)
            )
          }
        }
      }
    }

    // Editable Filename
    OutlinedTextField(
      value = customTitle,
      onValueChange = {
        customTitle = it
        currentResult = DocumentOcrEngine.analyzeAndExtract(rawText, it)
      },
      label = { Text("Document File Name") },
      singleLine = true,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("scan_filename_input")
    )

    // Extracted Text Editor
    OutlinedTextField(
      value = rawText,
      onValueChange = {
        rawText = it
        currentResult = DocumentOcrEngine.analyzeAndExtract(it, customTitle)
      },
      label = { Text("Extracted OCR Text") },
      maxLines = 6,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("scan_ocr_text_input")
    )

    // Save Button
    Button(
      onClick = {
        viewModel.saveScannedDocument(currentResult, documentFilter)
      },
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .testTag("save_scanned_doc_button"),
      shape = RoundedCornerShape(14.dp)
    ) {
      Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text("Save Scanned Document & Index OCR")
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}
