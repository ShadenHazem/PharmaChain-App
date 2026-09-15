package com.pharmachain.ai.feature.distributor_portal

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pharmachain.ai.R
import com.pharmachain.ai.core.common.util.CurrencyFormatter
import com.pharmachain.ai.core.common.util.DateUtils
import com.pharmachain.ai.core.designsystem.components.PharmaCard
import com.pharmachain.ai.core.model.Order
import com.pharmachain.ai.core.model.OrderStatus
import com.pharmachain.ai.feature.pharmacist_dashboard.OrderStatusChip

@Composable
fun DistributorOrdersTab(
    orders: List<Order>,
    selectedStatusFilter: OrderStatus?,
    onStatusFilterSelected: (OrderStatus?) -> Unit,
    onApproveOrder: (String) -> Unit,
    onPromptRejectOrder: (Order) -> Unit,
    onAdvanceStatus: (String, OrderStatus) -> Unit,
    orderToReject: Order?,
    rejectionReasonText: String,
    onRejectionReasonChange: (String) -> Unit,
    onConfirmReject: () -> Unit,
    onDismissRejectDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pendingCount = orders.count { it.status == OrderStatus.PENDING_APPROVAL }

    val filteredOrders = remember(orders, selectedStatusFilter) {
        if (selectedStatusFilter == null) orders
        else orders.filter { it.status == selectedStatusFilter }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("distributor_orders_list"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        item {
            // Status Filters Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedStatusFilter == null,
                    onClick = { onStatusFilterSelected(null) },
                    label = { Text("${stringResource(R.string.filter_all)} (${orders.size})") },
                    modifier = Modifier.testTag("filter_orders_all")
                )

                FilterChip(
                    selected = selectedStatusFilter == OrderStatus.PENDING_APPROVAL,
                    onClick = { onStatusFilterSelected(OrderStatus.PENDING_APPROVAL) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.filter_pending_approval))
                            if (pendingCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "$pendingCount",
                                            color = MaterialTheme.colorScheme.onError,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.testTag("filter_orders_pending")
                )

                FilterChip(
                    selected = selectedStatusFilter == OrderStatus.CONFIRMED,
                    onClick = { onStatusFilterSelected(OrderStatus.CONFIRMED) },
                    label = { Text(stringResource(R.string.filter_confirmed)) },
                    modifier = Modifier.testTag("filter_orders_confirmed")
                )

                FilterChip(
                    selected = selectedStatusFilter == OrderStatus.SHIPPED,
                    onClick = { onStatusFilterSelected(OrderStatus.SHIPPED) },
                    label = { Text(stringResource(R.string.filter_shipped)) },
                    modifier = Modifier.testTag("filter_orders_shipped")
                )

                FilterChip(
                    selected = selectedStatusFilter == OrderStatus.DELIVERED,
                    onClick = { onStatusFilterSelected(OrderStatus.DELIVERED) },
                    label = { Text(stringResource(R.string.filter_delivered)) },
                    modifier = Modifier.testTag("filter_orders_delivered")
                )

                FilterChip(
                    selected = selectedStatusFilter == OrderStatus.REJECTED,
                    onClick = { onStatusFilterSelected(OrderStatus.REJECTED) },
                    label = { Text(stringResource(R.string.filter_rejected)) },
                    modifier = Modifier.testTag("filter_orders_rejected")
                )
            }
        }

        if (filteredOrders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_orders_found),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredOrders, key = { it.id }) { order ->
                DistributorOrderCard(
                    order = order,
                    onApprove = { onApproveOrder(order.id) },
                    onReject = { onPromptRejectOrder(order) },
                    onAdvanceStatus = { nextStatus -> onAdvanceStatus(order.id, nextStatus) }
                )
            }
        }
    }

    // Rejection Reason Modal Dialog
    if (orderToReject != null) {
        RejectOrderDialog(
            order = orderToReject,
            reasonText = rejectionReasonText,
            onReasonChange = onRejectionReasonChange,
            onConfirm = onConfirmReject,
            onDismiss = onDismissRejectDialog
        )
    }
}

@Composable
private fun DistributorOrderCard(
    order: Order,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onAdvanceStatus: (OrderStatus) -> Unit
) {
    val isPending = order.status == OrderStatus.PENDING_APPROVAL
    val isRejected = order.status == OrderStatus.REJECTED

    PharmaCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dist_order_card_${order.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: ID + Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.id,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.from_pharmacy, order.pharmacyName),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.placed_at, DateUtils.formatDateTime(order.placedAt)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OrderStatusChip(status = order.status)
            }

            // Pending Approval Gate Notice
            if (isPending) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.approval_gate_notice),
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Rejection Reason Notice
            if (isRejected && !order.rejectionReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.rejection_reason_label, order.rejectionReason),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Items List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(10.dp)
            ) {
                order.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${item.quantity}x ${item.medicationName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = CurrencyFormatter.formatEgp(item.lineTotal),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Gross total and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.total_gross_gmv),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatEgp(order.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Action Buttons based on status
                when (order.status) {
                    OrderStatus.PENDING_APPROVAL -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onReject,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.testTag("reject_order_btn_${order.id}")
                            ) {
                                Text(stringResource(R.string.reject_btn))
                            }

                            Button(
                                onClick = onApprove,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2E7D32)
                                ),
                                modifier = Modifier.testTag("approve_order_btn_${order.id}")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.approve_btn))
                            }
                        }
                    }
                    OrderStatus.CONFIRMED -> {
                        Button(
                            onClick = { onAdvanceStatus(OrderStatus.SHIPPED) },
                            modifier = Modifier.testTag("ship_order_btn_${order.id}")
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.dispatch_ship_btn))
                        }
                    }
                    OrderStatus.SHIPPED -> {
                        OutlinedButton(
                            onClick = { onAdvanceStatus(OrderStatus.DELIVERED) },
                            modifier = Modifier.testTag("deliver_order_btn_${order.id}")
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.mark_delivered_btn))
                        }
                    }
                    OrderStatus.DELIVERED -> {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = Color(0xFF2E7D32).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = stringResource(R.string.fulfilled_settled_badge),
                                color = Color(0xFF2E7D32),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                    OrderStatus.REJECTED -> {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        ) {
                            Text(
                                text = stringResource(R.string.order_rejected_badge),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                    OrderStatus.CANCELLED, OrderStatus.EXPIRED -> {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = order.status.name,
                                color = MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RejectOrderDialog(
    order: Order,
    reasonText: String,
    onReasonChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val predefinedReasons = listOf(
        stringResource(R.string.reject_reason_out_of_stock),
        stringResource(R.string.reject_reason_quota_reached),
        stringResource(R.string.reject_reason_outside_zone),
        stringResource(R.string.reject_reason_credit_clearance),
        stringResource(R.string.reject_reason_custom)
    )

    val customReasonPrefix = stringResource(R.string.reject_reason_custom)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.reject_order_dialog_title, order.id),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.reject_order_dialog_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                predefinedReasons.forEach { reasonOption ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = reasonText == reasonOption || (reasonOption == customReasonPrefix && !predefinedReasons.take(4).contains(reasonText)),
                            onClick = {
                                if (reasonOption != customReasonPrefix) {
                                    onReasonChange(reasonOption)
                                } else {
                                    onReasonChange("")
                                }
                            }
                        )
                        Text(
                            text = reasonOption,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                OutlinedTextField(
                    value = reasonText,
                    onValueChange = onReasonChange,
                    label = { Text(stringResource(R.string.rejection_note_label)) },
                    placeholder = { Text(stringResource(R.string.rejection_note_placeholder)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reject_reason_input"),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.testTag("confirm_reject_btn")
            ) {
                Text(stringResource(R.string.confirm_rejection_btn))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
