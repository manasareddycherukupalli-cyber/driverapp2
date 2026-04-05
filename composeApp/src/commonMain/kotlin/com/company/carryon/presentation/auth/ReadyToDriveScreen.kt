package com.company.carryon.presentation.auth

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen

private val Blue = Color(0xFF2F80ED)
private val Bg = Color(0xFFF9F9FF)

@Composable
fun ReadyToDriveScreen(navigator: AppNavigator) {
    var accepted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().background(Bg).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(Modifier.height(36.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.align(Alignment.CenterHorizontally)) {
            repeat(3) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Blue)
                if (it < 2) Box(modifier = Modifier.padding(top = 10.dp).size(width = 56.dp, height = 2.dp).background(Blue))
            }
        }
        Text("Step 3 of 3", fontSize = 12.sp, color = Color(0x99000000))
        Text("You are Ready to Drive", fontWeight = FontWeight.ExtraBold, fontSize = 34.sp)

        Box(
            modifier = Modifier.fillMaxWidth().height(240.dp).background(Color(0xFFD7DCE8), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.DirectionsCar, contentDescription = null, tint = Blue, modifier = Modifier.size(72.dp))
        }

        Text("Your account is now activated. Let's book your load.", fontSize = 18.sp)

        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = accepted, onCheckedChange = { accepted = it })
            Text("I Accept this ", fontSize = 13.sp, color = Color(0xFF9F9A9A))
            Text("Terms and Conditions", fontSize = 13.sp, color = Blue)
        }

        Button(
            onClick = { navigator.navigateAndClearStack(Screen.Home) },
            enabled = accepted,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue)
        ) { Text("Continue", fontSize = 18.sp) }
    }
}
