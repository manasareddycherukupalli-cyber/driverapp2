package com.company.carryon.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import com.company.carryon.i18n.LocalStrings
import com.company.carryon.i18n.SupportedLanguages
import com.company.carryon.presentation.theme.CarryBlue

@Composable
fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    val strings = LocalStrings.current
    var selectedCode by remember(currentLanguage) {
        mutableStateOf(SupportedLanguages.normalize(currentLanguage))
    }

    AlertDialog(
        onDismissRequest = {},
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = strings.selectYourLanguage,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF181C23)
            )
        },
        text = {
            Column {
                SupportedLanguages.all.forEach { language ->
                    val selected = selectedCode == language.code
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .clickable { selectedCode = language.code },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) Color(0x33A6D2F3) else Color(0xFFF8FAFC)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(language.nativeName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                Text(language.description, color = Color(0xFF64748B), fontSize = 14.sp)
                            }
                            RadioButton(
                                selected = selected,
                                onClick = { selectedCode = language.code },
                                colors = RadioButtonDefaults.colors(selectedColor = CarryBlue)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onLanguageSelected(selectedCode) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CarryBlue)
            ) {
                Text(strings.continueText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

