package com.pharmachain.ai.feature.forecasting

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pharmachain.ai.R
import com.pharmachain.ai.core.common.result.NetworkResult
import com.pharmachain.ai.core.designsystem.components.PharmaCard
import com.pharmachain.ai.core.designsystem.components.PharmaOutlinedCard
import com.pharmachain.ai.core.designsystem.components.PharmaPrimaryButton
import com.pharmachain.ai.core.model.Medication
import com.pharmachain.ai.feature.vision.RecognizedMedicine
import com.pharmachain.ai.feature.vision.VisionRestockConfirmationDialog
import com.pharmachain.ai.feature.vision.VisionRestockRepository
import kotlinx.coroutines.launch

// Sample Egyptian pharmacy reference catalog for quick manual addition
private val DEFAULT_REFERENCE_CATALOG = listOf(
    Triple("med_1", "Panadol Extra 500mg", "Analgesics & Antipyretics"),
    Triple("med_2", "Augmentin 1g Tablets", "Antibiotics"),
    Triple("med_3", "Concor 5mg Plus", "Cardiovascular"),
    Triple("med_4", "Antinal 200mg Capsules", "Gastrointestinal"),
    Triple("med_5", "Cidophage 500mg", "Diabetes Care"),
    Triple("med_6", "Brufen 400mg Tablets", "Analgesics & Antipyretics"),
    Triple("med_7", "Ketofan 50mg", "Analgesics & Antipyretics"),
    Triple("med_8", "Controloc 40mg", "Gastrointestinal"),
    Triple("med_9", "Telfast 120mg", "Antihistamines"),
    Triple("med_10", "Ator 20mg", "Cardiovascular"),
    Triple("med_11", "Amoxil 500mg", "Antibiotics"),
    Triple("med_12", "Zyrtec 10mg", "Antihistamines")
)

@Composable
fun ManualEntryScreen(
    manualEntries: List<ManualProductEntry>,
    onAddOrUpdateEntry: (medicationId: String, brandName: String, genericName: String, category: String, qty: Int) -> Unit,
    onRemoveEntry: (medicationId: String) -> Unit,
    onProceedToConstraints: () -> Unit,
    onBackToUpload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf(DEFAULT_REFERENCE_CATALOG.first()) }
    var inputQuantity by remember { mutableStateOf("30") }

    // Vision Restock Camera state
    val visionRepository = remember { VisionRestockRepository() }
    var isAnalyzingVision by remember { mutableStateOf(false) }
    var recognizedMedicine by remember { mutableStateOf<RecognizedMedicine?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            isAnalyzingVision = true
            coroutineScope.launch {
                when (val result = visionRepository.recognizeMedicineBox(bitmap)) {
                    is NetworkResult.Success -> {
                        isAnalyzingVision = false
                        recognizedMedicine = result.data
                    }
                    is NetworkResult.Error -> {
                        isAnalyzingVision = false
                        Toast.makeText(context, "Recognition error: ${result.message}", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        isAnalyzingVision = false
                    }
                }
            }
        }
    }

    val filteredCatalog = DEFAULT_REFERENCE_CATALOG.filter {
        it.second.contains(searchQuery, ignoreCase = true) || it.third.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.tertiaryContainer
        ) {
            Text(
                text = stringResource(R.string.manual_entry_badge),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.manual_entry_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = stringResource(R.string.manual_entry_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // AI Vision Camera Scan Quick Action Button
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { cameraLauncher.launch(null) }
                .testTag("ai_camera_scan_restock_btn")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.scan_box_btn),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = stringResource(R.string.scan_box_tooltip),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "Scan",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Medication Selection & Add Form Card
        PharmaCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = stringResource(R.string.search_and_add_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(R.string.search_med_placeholder)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Catalog selection chips/list
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(2f)) {
                        Text(
                            text = "${stringResource(R.string.selected_label)} ${selectedItem.second}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = selectedItem.third,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedTextField(
                        value = inputQuantity,
                        onValueChange = { inputQuantity = it.filter { ch -> ch.isDigit() } },
                        label = { Text(stringResource(R.string.quantity_units)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )

                    IconButton(
                        onClick = {
                            val qty = inputQuantity.toIntOrNull() ?: 10
                            onAddOrUpdateEntry(
                                selectedItem.first,
                                selectedItem.second,
                                selectedItem.second,
                                selectedItem.third,
                                qty
                            )
                        },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
                            .size(48.dp)
                            .testTag("add_manual_item_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.add_item_btn), tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }

                if (filteredCatalog.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(R.string.quick_select_label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        filteredCatalog.take(3).forEach { item ->
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = if (selectedItem.first == item.first) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { selectedItem = item }
                            ) {
                                Text(
                                    text = item.second.take(14),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    color = if (selectedItem.first == item.first) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Selected Items List
        PharmaCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            testTag = "manual_entries_list_card"
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                Text(
                    text = stringResource(R.string.selected_items_list_title, manualEntries.size),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (manualEntries.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_items_added_placeholder),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("manual_entries_list"),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(manualEntries, key = { it.medicationId }) { entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = entry.brandName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${entry.category} • ${entry.monthlyEstimateQty} units",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = { onRemoveEntry(entry.medicationId) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove item",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Navigation Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBackToUpload,
                modifier = Modifier
                    .weight(1f)
                    .testTag("manual_back_to_upload_button")
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.back_btn))
            }

            PharmaPrimaryButton(
                onClick = onProceedToConstraints,
                enabled = manualEntries.isNotEmpty(),
                modifier = Modifier
                    .weight(1.5f)
                    .testTag("manual_proceed_to_constraints_button")
            ) {
                Text(stringResource(R.string.proceed_to_constraints_btn))
                Spacer(modifier = Modifier.width(6.dp))
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }

    // Vision Restock Confirmation Dialog
    if (recognizedMedicine != null) {
        val med = recognizedMedicine ?: return
        VisionRestockConfirmationDialog(
            medicine = med,
            actionLabel = "Add to Restock Plan",
            onConfirm = { qty ->
                onAddOrUpdateEntry(
                    med.medicationId,
                    med.brandName,
                    med.genericName,
                    med.category,
                    qty
                )
                Toast.makeText(
                    context,
                    "Added $qty boxes of ${med.brandName} to restocking list!",
                    Toast.LENGTH_SHORT
                ).show()
                recognizedMedicine = null
            },
            onDismiss = { recognizedMedicine = null }
        )
    }

    // Vision Analysis Loading Dialog
    if (isAnalyzingVision) {
        AlertDialog(
            onDismissRequest = { /* non-dismissible */ },
            title = {
                Text(
                    text = stringResource(R.string.analyzing_box_vision),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Analyzing physical medicine packaging with AI Vision…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {}
        )
    }
}
