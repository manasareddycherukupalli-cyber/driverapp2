package com.example.drive_app.presentation.support

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drive_app.data.model.*
import com.example.drive_app.presentation.components.*
import com.example.drive_app.presentation.navigation.AppNavigator
import com.example.drive_app.presentation.theme.*

/**
 * RaiseTicketScreen — Create a new support ticket.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaiseTicketScreen(navigator: AppNavigator) {
    val viewModel = remember { SupportViewModel() }
    val createState by viewModel.createTicketState.collectAsState()

    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TicketCategory.GENERAL) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(createState) {
        if (createState is UiState.Success) navigator.goBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DriveAppTopBar(
            title = "Raise a Ticket",
            onBackClick = { navigator.goBack() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "How can we help?",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Describe your issue and we'll get back to you shortly",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Category dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    TicketCategory.entries.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.displayName) },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Subject
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Describe your issue") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 6
            )

            Spacer(Modifier.height(8.dp))

            PrimaryButton(
                text = "Submit Ticket",
                onClick = { viewModel.createTicket(subject, selectedCategory, description) },
                enabled = subject.isNotBlank() && description.isNotBlank(),
                isLoading = createState is UiState.Loading
            )

            if (createState is UiState.Error) {
                Text(
                    text = (createState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }
        }
    }
}
