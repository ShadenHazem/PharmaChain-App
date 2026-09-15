package com.pharmachain.ai.feature.catalog

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pharmachain.ai.R
import com.pharmachain.ai.core.common.result.NetworkResult
import com.pharmachain.ai.core.common.util.CurrencyFormatter
import com.pharmachain.ai.core.designsystem.components.EmptyState
import com.pharmachain.ai.core.designsystem.components.ErrorState
import com.pharmachain.ai.core.designsystem.components.LoadingIndicator
import com.pharmachain.ai.core.designsystem.components.PharmaCard
import com.pharmachain.ai.core.designsystem.components.PharmaOutlinedCard
import com.pharmachain.ai.core.designsystem.components.PharmaPrimaryButton
import com.pharmachain.ai.core.model.MedicationWithListings
import com.pharmachain.ai.core.model.ProductListing
import com.pharmachain.ai.feature.cart.CartViewModel
import com.pharmachain.ai.feature.cart.ReviewOrderBottomSheet
import com.pharmachain.ai.feature.vision.RecognizedMedicine
import com.pharmachain.ai.feature.vision.VisionRestockConfirmationDialog
import com.pharmachain.ai.feature.vision.VisionRestockRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel,
    cartViewModel: CartViewModel,
    onDirectOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cartUiState by cartViewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is CatalogUiState.Loading -> LoadingIndicator(message = stringResource(R.string.loading_catalog))
        is CatalogUiState.Error -> ErrorState(message = state.message, onRetry = { viewModel.loadCatalog() })
        is CatalogUiState.Success -> CatalogContent(
            state = state,
            cartViewModel = cartViewModel,
            onSearchQuery = { viewModel.onSearchQueryChanged(it) },
            onCategorySelect = { viewModel.onCategorySelected(it) },
            onDirectOrderNav = onDirectOrder,
            onDirectOrderFallback = { med, listing, qty ->
                viewModel.placeDirectOrder(med, listing, qty, onDirectOrder)
            },
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatalogContent(
    state: CatalogUiState.Success,
    cartViewModel: CartViewModel,
    onSearchQuery: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onDirectOrderNav: () -> Unit,
    onDirectOrderFallback: (MedicationWithListings, ProductListing, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val cartUiState by cartViewModel.uiState.collectAsStateWithLifecycle()

    // Dialog state for "Add to Cart / Order"
    var selectedMedListing by remember { mutableStateOf<Pair<MedicationWithListings, ProductListing>?>(null) }
    var selectedQuantity by remember { mutableIntStateOf(10) }

    // BottomSheet state for "Review Order"
    var showReviewOrderSheet by remember { mutableStateOf(false) }
    val reviewSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Camera Vision Scanning State
    val visionRepository = remember { VisionRestockRepository() }
    var isAnalyzingVision by remember { mutableStateOf(false) }
    var recognizedMedicine by remember { mutableStateOf<RecognizedMedicine?>(null) }

    // Camera Launcher for capturing physical medicine boxes
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

    val categories = listOf(
        "All" to R.string.cat_all,
        "Antibiotics" to R.string.cat_antibiotics,
        "Cardiovascular" to R.string.cat_cardiovascular,
        "Analgesics" to R.string.cat_analgesics,
        "Gastrointestinal" to R.string.cat_gastrointestinal,
        "Antihistamines" to R.string.cat_antihistamines
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("catalog_screen_view")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar & Actions Top Surface
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = onSearchQuery,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("catalog_search_bar"),
                            placeholder = { Text(stringResource(R.string.search_medicines)) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null)
                            },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        // Camera Scan Box Action
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(50.dp)
                                .clickable { cameraLauncher.launch(null) }
                                .testTag("catalog_camera_scan_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = stringResource(R.string.scan_box_btn),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Shopping Cart Badge Action
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (cartUiState.totalItems > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .size(50.dp)
                                .clickable { showReviewOrderSheet = true }
                                .testTag("catalog_cart_top_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                BadgedBox(
                                    badge = {
                                        if (cartUiState.totalItems > 0) {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            ) {
                                                Text("${cartUiState.totalItems}")
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = stringResource(R.string.cart_title),
                                        tint = if (cartUiState.totalItems > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(categories) { (catKey, catResId) ->
                            val isSelected = state.selectedCategory == catKey
                            FilterChip(
                                selected = isSelected,
                                onClick = { onCategorySelect(catKey) },
                                label = { Text(stringResource(catResId)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.testTag("cat_chip_$catKey")
                            )
                        }
                    }
                }
            }

            if (state.medications.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Medication,
                    title = stringResource(R.string.no_medications_found),
                    description = stringResource(R.string.catalog_empty_desc)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("medications_list"),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = if (cartUiState.totalItems > 0) 88.dp else 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.medications, key = { it.medication.id }) { medItem ->
                        MedicationCard(
                            medicationWithListings = medItem,
                            onOrderClick = { listing ->
                                selectedQuantity = 10
                                selectedMedListing = medItem to listing
                            }
                        )
                    }
                }
            }
        }

        // Floating Bottom Cart Bar when items are present
        AnimatedVisibility(
            visible = cartUiState.totalItems > 0,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showReviewOrderSheet = true }
                    .testTag("floating_cart_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "${cartUiState.totalItems} items (${cartUiState.totalBoxes} boxes)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                            )
                            Text(
                                text = CurrencyFormatter.formatEgp(cartUiState.totalPrice),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.clickable { showReviewOrderSheet = true }
                    ) {
                        Text(
                            text = "Review Order >",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Review Order & Batch Submission
    if (showReviewOrderSheet) {
        ReviewOrderBottomSheet(
            sheetState = reviewSheetState,
            cartUiState = cartUiState,
            onDismiss = { showReviewOrderSheet = false },
            onUpdateQuantity = { medId, newQty ->
                cartViewModel.updateQuantity(medId, newQty)
            },
            onRemoveItem = { medId ->
                cartViewModel.removeFromCart(medId)
            },
            onConfirmOrder = {
                cartViewModel.submitCart(
                    onSuccess = {
                        showReviewOrderSheet = false
                        Toast.makeText(context, context.getString(R.string.order_submitted_success), Toast.LENGTH_LONG).show()
                        onDirectOrderNav()
                    }
                )
            }
        )
    }

    // Add To Cart Dialog
    if (selectedMedListing != null) {
        val (med, listing) = selectedMedListing ?: return
        AlertDialog(
            onDismissRequest = { selectedMedListing = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = med.medication.brandName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "${stringResource(R.string.distributor_name_label)}: ${listing.distributorName} (${listing.distributorReputation}★)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${stringResource(R.string.unit_price)}: ${CurrencyFormatter.formatEgp(listing.price)} • ${stringResource(R.string.in_stock, listing.stockQuantity)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Quantity to add (boxes):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Quantity Stepper & Direct Editable Number Input
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Quick -5 step button
                        Surface(
                            onClick = {
                                val current = if (selectedQuantity <= 0) 10 else selectedQuantity
                                selectedQuantity = (current - 5).coerceAtLeast(1)
                            },
                            enabled = selectedQuantity > 1,
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.height(38.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = "-5",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        FilledTonalIconButton(
                            onClick = {
                                val current = if (selectedQuantity <= 0) 10 else selectedQuantity
                                if (current > 1) {
                                    selectedQuantity = current - 1
                                }
                            },
                            enabled = selectedQuantity > 1,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("catalog_dialog_qty_decrement"),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease by 1",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = if (selectedQuantity > 0) selectedQuantity.toString() else "",
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() }
                                val parsed = clean.toIntOrNull()
                                selectedQuantity = when {
                                    clean.isEmpty() || parsed == null -> 0
                                    parsed > listing.stockQuantity -> listing.stockQuantity
                                    else -> parsed
                                }
                            },
                            label = { Text("Boxes", style = MaterialTheme.typography.labelSmall) },
                            textStyle = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .width(96.dp)
                                .testTag("catalog_dialog_qty_input")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        FilledTonalIconButton(
                            onClick = {
                                val current = if (selectedQuantity <= 0) 0 else selectedQuantity
                                if (current < listing.stockQuantity) {
                                    selectedQuantity = current + 1
                                }
                            },
                            enabled = selectedQuantity < listing.stockQuantity,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("catalog_dialog_qty_increment"),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase by 1",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Quick +5 step button
                        Surface(
                            onClick = {
                                val current = if (selectedQuantity <= 0) 0 else selectedQuantity
                                selectedQuantity = (current + 5).coerceAtMost(listing.stockQuantity)
                            },
                            enabled = selectedQuantity < listing.stockQuantity,
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.height(38.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = "+5",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick presets: 10 to 25 range easily selectable
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(10, 15, 20, 25, 50).forEach { preset ->
                            Surface(
                                onClick = { selectedQuantity = preset },
                                shape = RoundedCornerShape(6.dp),
                                color = if (selectedQuantity == preset) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("catalog_preset_$preset")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 7.dp)
                                ) {
                                    Text(
                                        text = "$preset",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedQuantity == preset) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Line Total:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val displayedQty = selectedQuantity.coerceAtLeast(1)
                        Text(
                            text = CurrencyFormatter.formatEgp(listing.price * displayedQty),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                PharmaPrimaryButton(
                    onClick = {
                        val finalQty = selectedQuantity.coerceAtLeast(1)
                        cartViewModel.addToCart(
                            medicationId = med.medication.id,
                            brandName = med.medication.brandName,
                            genericName = med.medication.genericName,
                            distributorId = listing.distributorId,
                            distributorName = listing.distributorName,
                            unitPrice = listing.price,
                            quantity = finalQty
                        )
                        Toast.makeText(
                            context,
                            context.getString(R.string.added_to_cart_toast, finalQty, med.medication.brandName),
                            Toast.LENGTH_SHORT
                        ).show()
                        selectedMedListing = null
                    },
                    testTag = "confirm_add_to_cart_btn"
                ) {
                    Icon(imageVector = Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.add_to_cart_btn))
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedMedListing = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Vision Restock Confirmation Dialog
    if (recognizedMedicine != null) {
        val med = recognizedMedicine ?: return
        VisionRestockConfirmationDialog(
            medicine = med,
            actionLabel = "Add to Shopping Cart",
            onConfirm = { qty ->
                cartViewModel.addToCart(
                    medicationId = med.medicationId,
                    brandName = med.brandName,
                    genericName = med.genericName,
                    distributorId = "dist_1",
                    distributorName = "United Company for Pharmacists (UCP)",
                    unitPrice = med.estimatedPrice,
                    quantity = qty
                )
                Toast.makeText(
                    context,
                    "Added $qty boxes of ${med.brandName} to Cart!",
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
            onDismissRequest = { /* non-dismissible during scanning */ },
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
                        text = "Extracting packaging text, dosage, and matching Egyptian catalog…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun MedicationCard(
    medicationWithListings: MedicationWithListings,
    onOrderClick: (ProductListing) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val med = medicationWithListings.medication
    val topOffer = medicationWithListings.topOffer
    val otherOffers = medicationWithListings.listings.drop(1)

    PharmaCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("med_card_${med.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = med.brandName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${med.genericName} • ${med.strength}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = med.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Inline Top Offer (Section 8.2 Requirement)
            if (topOffer != null) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.top_offer),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = topOffer.distributorName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${topOffer.distributorReputation} ★",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "• ${stringResource(R.string.in_stock, topOffer.stockQuantity)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = CurrencyFormatter.formatEgp(topOffer.price),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clickable { onOrderClick(topOffer) }
                                    .testTag("order_top_offer_${topOffer.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddShoppingCart,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.add_to_cart_btn),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Compare N offers expandable button
            if (otherOffers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpanded) {
                            stringResource(R.string.hide_offers)
                        } else {
                            stringResource(R.string.compare_offers, medicationWithListings.listings.size)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        otherOffers.forEach { offer ->
                            PharmaOutlinedCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = offer.distributorName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${offer.distributorReputation}★ • ${stringResource(R.string.in_stock, offer.stockQuantity)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = CurrencyFormatter.formatEgp(offer.price),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(end = 10.dp)
                                        )
                                        Surface(
                                            shape = MaterialTheme.shapes.small,
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier
                                                .clickable { onOrderClick(offer) }
                                                .testTag("order_other_offer_${offer.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AddShoppingCart,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = stringResource(R.string.add_to_cart_btn),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
