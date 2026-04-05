package com.company.carryon.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.UiState
import com.company.carryon.presentation.navigation.AppNavigator

@Composable
fun EditProfileScreen(navigator: AppNavigator) {
    val viewModel = remember { ProfileViewModel() }
    val driver by viewModel.currentDriver.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    var name by remember { mutableStateOf(driver?.name ?: "") }
    var email by remember { mutableStateOf(driver?.email ?: "") }
    var emergencyContact by remember { mutableStateOf(driver?.emergencyContact ?: "") }
    val phone = driver?.phone ?: "+1 (555) 928-3401"
    val residentialAddress = emergencyContact.ifBlank { "742 Evergreen Terrace,\nSpringfield, IL 62704" }

    LaunchedEffect(driver) {
        driver?.let {
            if (name.isBlank()) name = it.name
            if (email.isBlank()) email = it.email
            if (emergencyContact.isBlank()) emergencyContact = it.emergencyContact
        }
    }

    LaunchedEffect(updateState) {
        if (updateState is UiState.Success) navigator.goBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navigator.goBack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF4474DE)
                )
            }
            Text(
                text = "Settings",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color(0xFF1D232F)
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "DRIVER PORTAL",
                fontSize = 10.sp,
                color = Color(0xFF9AA5BC),
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Edit Profile",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                lineHeight = 40.sp,
                color = Color(0xFF2B3242)
            )
            Text(
                text = "Update your professional details and driver credentials.",
                color = Color(0xFF7A859D),
                fontSize = 13.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(96.dp),
                        shape = RoundedCornerShape(48.dp),
                        color = Color(0xFFEAF0FB)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = Color(0xFF5E6C89),
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { }) {
                        Text(
                            text = "Change Photo",
                            color = Color(0xFF4474DE),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF1FF))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "ACCOUNT STATUS",
                        fontSize = 10.sp,
                        color = Color(0xFF7E90B1),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Badge,
                            contentDescription = null,
                            tint = Color(0xFF4474DE),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Verified Partner",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3852)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Your profile information is visible to dispatchers and warehouse managers to ensure secure hand-offs.",
                        fontSize = 12.sp,
                        color = Color(0xFF6E7991),
                        lineHeight = 17.sp
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ReadOnlyField("Full Name", name.ifBlank { "Marcus Thompson" }, Icons.Filled.Person)
                    EditableField("Email Address", email, Icons.Filled.Email, onChange = { email = it })
                    ReadOnlyField("Phone Number", phone, Icons.Filled.Phone)
                    ReadOnlyField("Residential Address", residentialAddress, Icons.Filled.LocationOn, singleLine = false)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF1FF))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LockClock, contentDescription = null, tint = Color(0xFF5E6C89))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Update Password", fontWeight = FontWeight.SemiBold, color = Color(0xFF2C3852))
                            Text("Last changed 4 months ago", fontSize = 11.sp, color = Color(0xFF6E7991))
                        }
                    }
                    Text("Change", color = Color(0xFF4474DE), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }

            Button(
                onClick = { viewModel.updateProfile(name, email, emergencyContact) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = name.isNotBlank() && email.isNotBlank() && updateState !is UiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A78E5)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Changes", fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = { navigator.goBack() },
                modifier = Modifier.fillMaxWidth().height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC5DAF4), contentColor = Color(0xFF4474DE)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel", fontWeight = FontWeight.SemiBold)
            }

            if (updateState is UiState.Error) {
                Text(
                    text = (updateState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ReadOnlyField(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    singleLine: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = Color(0xFF606C85), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF1F4FA), RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFFE2E8F4), RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = if (singleLine) 12.dp else 10.dp),
            verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF8A96AF), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = value,
                color = Color(0xFF303A4F),
                fontSize = 13.sp,
                lineHeight = 17.sp,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private fun EditableField(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = Color(0xFF606C85), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            leadingIcon = { Icon(icon, contentDescription = null, tint = Color(0xFF8A96AF)) },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF1F4FA),
                focusedContainerColor = Color(0xFFF1F4FA),
                unfocusedBorderColor = Color(0xFFE2E8F4),
                focusedBorderColor = Color(0xFF94A8D3)
            )
        )
    }
}
