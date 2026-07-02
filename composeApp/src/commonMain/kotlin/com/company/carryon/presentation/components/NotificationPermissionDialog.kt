package com.company.carryon.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.presentation.theme.CarryBlue
import com.company.carryon.presentation.theme.CarryBlueLight

/**
 * Themed pre-permission primer shown once, before the OS notification prompt.
 * The real system dialog can't be restyled — this screen carries our branding
 * so the unavoidable native prompt that follows "Allow" reads as a formality.
 *
 * Buttons are built into the `text` slot (rather than AlertDialog's
 * confirmButton/dismissButton, which lay out side-by-side) so they can be
 * full-width and stacked, matching the rest of the app's dialogs/screens.
 */
@Composable
fun NotificationPermissionDialog(
    onAllow: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(CarryBlueLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.NotificationsActive,
                        contentDescription = null,
                        tint = CarryBlue,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Stay on top of your deliveries",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF181C23),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                Text(
                    text = "Turn on notifications to get alerted the moment a new job comes in, and for updates on your active deliveries.",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 20.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onAllow,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CarryBlue)
                    ) {
                        Text("Allow notifications", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CarryBlue),
                        border = BorderStroke(1.5.dp, CarryBlue)
                    ) {
                        Text("Not now", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        },
        confirmButton = {}
    )
}
