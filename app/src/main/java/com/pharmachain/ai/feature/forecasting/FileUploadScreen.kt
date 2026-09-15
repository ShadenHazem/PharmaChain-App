package com.pharmachain.ai.feature.forecasting

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pharmachain.ai.R
import com.pharmachain.ai.core.designsystem.components.PharmaCard
import com.pharmachain.ai.core.designsystem.components.PharmaPrimaryButton
import com.pharmachain.ai.feature.forecasting.engine.DataCleaningAndMappingService
import com.pharmachain.ai.feature.forecasting.engine.ParsedRawFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import kotlin.math.roundToInt

@Composable
fun FileUploadScreen(
    isProcessing: Boolean,
    onFileUploadSelected: (ParsedRawFile) -> Unit,
    onStartManualEntry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isReadingFile by remember { mutableStateOf(false) }
    var readError by remember { mutableStateOf<String?>(null) }

    // System File Picker Launcher for user's phone files
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isReadingFile = true
            readError = null
            coroutineScope.launch {
                try {
                    val (fileName, fileSizeKb) = getFileInfoFromUri(context, uri)
                    val parsed = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            DataCleaningAndMappingService.parseSpreadsheetStream(stream, fileName, fileSizeKb)
                        } ?: throw IllegalStateException("Could not open input stream for selected file.")
                    }
                    isReadingFile = false
                    onFileUploadSelected(parsed)
                } catch (e: Exception) {
                    isReadingFile = false
                    readError = "Failed to parse file: ${e.localizedMessage ?: "Unknown error"}"
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Step Indicator Badge
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = stringResource(R.string.step_1_of_6),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.upload_sales_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.upload_sales_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Supported format pills
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FormatBadge(".XLSX", "Excel", Modifier.weight(1f))
            FormatBadge(".XLS", "Excel Legacy", Modifier.weight(1f))
            FormatBadge("HTML", "POS Table", Modifier.weight(1f))
            FormatBadge("CSV", "UTF-8", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Main File Picker Zone
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isProcessing && !isReadingFile) {
                    filePickerLauncher.launch(
                        arrayOf(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            "application/vnd.ms-excel",
                            "text/html",
                            "text/csv",
                            "text/comma-separated-values",
                            "text/plain",
                            "*/*"
                        )
                    )
                }
                .testTag("upload_dropzone")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isReadingFile || isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isReadingFile) stringResource(R.string.parsing_spreadsheet) else stringResource(R.string.upload_zone_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.upload_instructions),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                PharmaPrimaryButton(
                    onClick = {
                        filePickerLauncher.launch(
                            arrayOf(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                "application/vnd.ms-excel",
                                "text/html",
                                "text/csv",
                                "text/comma-separated-values",
                                "text/plain",
                                "*/*"
                            )
                        )
                    },
                    enabled = !isProcessing && !isReadingFile,
                    modifier = Modifier.fillMaxWidth(0.9f),
                    testTag = "browse_files_button"
                ) {
                    Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isReadingFile) stringResource(R.string.parsing_spreadsheet) else stringResource(R.string.browse_files_btn))
                }
            }
        }

        if (readError != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = readError ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Sample Template & Manual Entry Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    // Load realistic bilingual sample template
                    coroutineScope.launch {
                        isReadingFile = true
                        val sampleCsv = """
                            تاريخ البيع,اسم الصنف,الكمية المباعة,سعر الجمهور (ج.م),كود الصنف
                            2025-01-10,Panadol Extra 500mg,45,45.50,MED-001
                            2025-01-11,Augmentin 1g Tablets,28,98.00,MED-002
                            2025-01-12,Concor 5mg Plus,32,62.00,MED-003
                            2025-01-13,Antinal 200mg Capsules,20,38.00,MED-004
                            2025-01-14,Cidophage 500mg,55,30.00,MED-005
                            2025-01-15,Brufen 400mg,35,42.00,MED-006
                            2025-01-16,Ketofan 50mg,24,35.00,MED-007
                            2025-01-17,Controloc 40mg,18,115.00,MED-008
                        """.trimIndent()
                        val bytes = sampleCsv.toByteArray(StandardCharsets.UTF_8)
                        val parsed = withContext(Dispatchers.IO) {
                            DataCleaningAndMappingService.parseSpreadsheetStream(
                                ByteArrayInputStream(bytes),
                                "sample_pharmacy_sales.csv",
                                14.5
                            )
                        }
                        isReadingFile = false
                        onFileUploadSelected(parsed)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("load_sample_template_button")
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.sample_batch_btn), style = MaterialTheme.typography.labelMedium)
            }

            OutlinedButton(
                onClick = onStartManualEntry,
                modifier = Modifier
                    .weight(1f)
                    .testTag("manual_entry_flow_button")
            ) {
                Icon(imageVector = Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.manual_mode_btn), style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Supported Data Format Card
        PharmaCard(
            modifier = Modifier.fillMaxWidth(),
            testTag = "format_requirements_card"
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TableChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Enterprise Parsing Features",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                RequirementItem("Apache POI 5.2.5 engine for native .xlsx and .xls workbooks")
                RequirementItem("Jsoup parser for Egyptian pharmacy POS HTML table exports")
                RequirementItem("Automatic Arabic-Indic digit (٠-٩) and date format standardization")
                RequirementItem("Bilingual fuzzy mapping for Arabic & English POS column headers")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy & Security Notice
        PharmaCard(
            modifier = Modifier.fillMaxWidth(),
            testTag = "security_notice_card"
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Strict Privacy: Only the data from your uploaded file is processed on-device by the forecasting engine.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (isProcessing) {
            Spacer(modifier = Modifier.height(20.dp))
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("upload_progress_indicator")
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.processing_forecast),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun FormatBadge(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RequirementItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getFileInfoFromUri(context: Context, uri: Uri): Pair<String, Double> {
    var fileName = "sales_upload_${System.currentTimeMillis()}.xlsx"
    var sizeKb = 120.0

    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIndex != -1) {
                    val queriedName = cursor.getString(nameIndex)
                    if (!queriedName.isNullOrBlank()) {
                        fileName = queriedName
                    }
                }
                if (sizeIndex != -1) {
                    val sizeBytes = cursor.getLong(sizeIndex)
                    if (sizeBytes > 0) {
                        sizeKb = sizeBytes / 1024.0
                    }
                }
            }
        }
    } catch (e: Exception) {
        uri.lastPathSegment?.let { segment ->
            if (segment.isNotBlank()) fileName = segment.substringAfterLast("/")
        }
    }

    return Pair(fileName, (sizeKb * 10.0).roundToInt() / 10.0)
}

