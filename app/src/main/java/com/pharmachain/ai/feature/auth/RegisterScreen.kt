package com.pharmachain.ai.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pharmachain.ai.R
import com.pharmachain.ai.core.designsystem.components.PharmaPrimaryButton
import com.pharmachain.ai.core.designsystem.components.PharmaTextField
import com.pharmachain.ai.core.model.Role
import com.pharmachain.ai.core.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit,
    onAuthenticated: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedRole by viewModel.selectedRole.collectAsStateWithLifecycle()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var regOrLicenseNumber by remember { mutableStateOf("") }
    var governorate by remember { mutableStateOf("Cairo") }
    var city by remember { mutableStateOf("Nasr City") }
    var address by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Authenticated) {
            onAuthenticated((uiState as AuthUiState.Authenticated).user)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.register_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("register_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_btn)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (selectedRole == Role.PHARMACIST) {
                    stringResource(R.string.register_pharmacist_sub)
                } else {
                    stringResource(R.string.register_distributor_sub)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            PharmaTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = stringResource(R.string.full_name_label),
                leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                testTag = "register_fullname_input"
            )

            Spacer(modifier = Modifier.height(12.dp))

            PharmaTextField(
                value = email,
                onValueChange = { email = it },
                label = stringResource(R.string.email_label),
                leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                testTag = "register_email_input"
            )

            Spacer(modifier = Modifier.height(12.dp))

            PharmaTextField(
                value = phone,
                onValueChange = { phone = it },
                label = stringResource(R.string.phone_label),
                leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                testTag = "register_phone_input"
            )

            Spacer(modifier = Modifier.height(12.dp))

            PharmaTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = if (selectedRole == Role.PHARMACIST) {
                    stringResource(R.string.pharmacy_name_label)
                } else {
                    stringResource(R.string.distributor_name_label)
                },
                leadingIcon = { Icon(imageVector = Icons.Default.Business, contentDescription = null) },
                testTag = "register_business_name_input"
            )

            Spacer(modifier = Modifier.height(12.dp))

            PharmaTextField(
                value = regOrLicenseNumber,
                onValueChange = { regOrLicenseNumber = it },
                label = if (selectedRole == Role.PHARMACIST) {
                    stringResource(R.string.license_number_label)
                } else {
                    stringResource(R.string.tax_id_label)
                },
                leadingIcon = { Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null) },
                testTag = "register_license_input"
            )

            Spacer(modifier = Modifier.height(12.dp))

            PharmaTextField(
                value = address,
                onValueChange = { address = it },
                label = stringResource(R.string.address_label),
                leadingIcon = { Icon(imageVector = Icons.Default.LocationOn, contentDescription = null) },
                testTag = "register_address_input"
            )

            if (uiState is AuthUiState.Error) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = (uiState as AuthUiState.Error).message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            PharmaPrimaryButton(
                onClick = {
                    viewModel.register(
                        fullName = fullName,
                        email = email,
                        phone = phone,
                        businessName = businessName,
                        regOrLicenseNumber = regOrLicenseNumber,
                        governorate = governorate,
                        city = city,
                        address = address
                    )
                },
                enabled = uiState !is AuthUiState.Loading,
                modifier = Modifier.fillMaxWidth(),
                testTag = "register_submit_button"
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.padding(4.dp)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.sign_up_btn),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.testTag("switch_to_login_button")
            ) {
                Text(
                    text = stringResource(R.string.switch_to_login),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
