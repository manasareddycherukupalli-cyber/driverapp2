package com.company.carryon.presentation.map

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.LatLng
import com.company.carryon.presentation.components.MapMarker
import com.company.carryon.presentation.components.MapViewComposable
import com.company.carryon.presentation.components.MarkerColor
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen

private val ITBlue = Color(0xFF2F80ED)
private val ITSoft = Color(0x4DA6D2F3)
private val ITWhite = Color(0xFFFFFFFF)
private val ITBlack = Color(0xFF000000)
private val DefaultCenter = LatLng(12.9716, 77.5946)

@Composable
fun InTransitScreen(navigator: AppNavigator) {
    val viewModel = remember { MapViewModel() }
    val driverLocation by viewModel.driverLocation.collectAsState()
    val mapStyleUrl by viewModel.mapStyleUrl.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(ITWhite)) {
        MapViewComposable(
            modifier = Modifier.fillMaxSize(),
            styleUrl = mapStyleUrl,
            centerLat = driverLocation?.first ?: DefaultCenter.lat,
            centerLng = driverLocation?.second ?: DefaultCenter.lng,
            zoom = if (driverLocation != null) 14.2 else 12.0,
            markers = buildList {
                if (driverLocation != null) {
                    add(
                        MapMarker(
                            id = "driver",
                            lat = driverLocation!!.first,
                            lng = driverLocation!!.second,
                            title = "You",
                            color = MarkerColor.BLUE
                        )
                    )
                }
            }
        )

        Box(modifier = Modifier.fillMaxSize().background(ITSoft))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ITWhite)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.size(36.dp).background(ITBlue, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Navigation, contentDescription = null, tint = ITWhite, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text("1.2 mi - Turn Left on Main St", color = ITBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Icon(Icons.Filled.Search, contentDescription = null, tint = ITBlack.copy(alpha = 0.5f))
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 104.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = ITSoft)
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("55%", color = ITBlack, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                    Text("DELIVERY PROGRESS", color = ITBlue, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
                Box(modifier = Modifier.fillMaxWidth().height(10.dp).background(ITWhite.copy(alpha = 0.75f), RoundedCornerShape(99.dp))) {
                    Box(modifier = Modifier.fillMaxWidth(0.55f).height(10.dp).background(ITBlue, RoundedCornerShape(99.dp)))
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 40.dp)
                .size(180.dp)
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF265D91), Color(0xFF6CAFEA))),
                    RoundedCornerShape(topStart = 90.dp, topEnd = 90.dp)
                )
        )

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 64.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ITWhite)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("ESTIMATED ARRIVAL", color = ITBlack.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("12", color = ITBlue, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                            Spacer(Modifier.width(4.dp))
                            Text("min", color = ITBlue, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.width(10.dp))
                            Text("ETA", color = ITBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                    Column {
                        Text("DISTANCE", color = ITBlack.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("1.8", color = ITBlue, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                            Spacer(Modifier.width(4.dp))
                            Text("km", color = ITBlue, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = ITSoft), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(32.dp).background(ITWhite, CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = ITBlue, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("RECIPIENT", color = ITBlack.copy(alpha = 0.55f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                Text("Nurul Ain", color = ITBlue, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                        }
                        Box(modifier = Modifier.size(34.dp).background(ITWhite, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.ChatBubbleOutline, contentDescription = null, tint = ITBlue, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ITWhite, contentColor = ITBlue)
                    ) {
                        Text("Report\nIssue", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = {
                            navigator.selectedJobId = navigator.selectedJobId ?: "CR-4872"
                            navigator.navigateTo(Screen.ArrivedAtDrop)
                        },
                        modifier = Modifier.weight(2f).height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ITBlue)
                    ) {
                        Text("Arrived at Drop", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 248.dp)
                .size(36.dp)
                .background(ITWhite, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.GpsFixed, contentDescription = null, tint = ITBlue, modifier = Modifier.size(16.dp))
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(ITWhite, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TransitTab("MAP", true)
            TransitTab("TASKS", false)
            TransitTab("EARNINGS", false)
            TransitTab("PROFILE", false)
        }
    }
}

@Composable
private fun TransitTab(label: String, selected: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(22.dp).background(if (selected) ITBlue else Color.Transparent, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Navigation, contentDescription = null, tint = if (selected) ITWhite else ITBlack.copy(alpha = 0.45f), modifier = Modifier.size(12.dp))
        }
        Spacer(Modifier.width(4.dp))
        Text(label, color = if (selected) ITBlue else ITBlack.copy(alpha = 0.55f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}
