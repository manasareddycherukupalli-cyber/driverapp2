package com.company.carryon.presentation.ratings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.*
import com.company.carryon.presentation.components.*
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.theme.*

/**
 * RatingsScreen — Driver ratings overview with star distribution and feedback.
 */
@Composable
fun RatingsScreen(navigator: AppNavigator) {
    val viewModel = remember { RatingsViewModel() }
    val ratingState by viewModel.ratingInfo.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DriveAppTopBar(
            title = "My Ratings",
            onBackClick = { navigator.goBack() }
        )

        when (val state = ratingState) {
            is UiState.Loading -> LoadingScreen()
            is UiState.Error -> ErrorState(state.message) { viewModel.loadRatings() }
            is UiState.Success -> RatingsContent(state.data)
            is UiState.Idle -> LoadingScreen()
        }
    }
}

@Composable
private fun RatingsContent(info: RatingInfo) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Average rating card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${info.averageRating}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp,
                        color = Orange500
                    )
                    // Star display
                    Row {
                        repeat(5) { index ->
                            val filled = index < info.averageRating.toInt()
                            Icon(
                                imageVector = if (filled) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = null,
                                tint = if (filled) Yellow500 else Gray300,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Based on ${info.totalRatings} ratings",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Star distribution
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Rating Distribution", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(Modifier.height(12.dp))
                    StarDistributionRow(5, info.fiveStarCount, info.totalRatings)
                    StarDistributionRow(4, info.fourStarCount, info.totalRatings)
                    StarDistributionRow(3, info.threeStarCount, info.totalRatings)
                    StarDistributionRow(2, info.twoStarCount, info.totalRatings)
                    StarDistributionRow(1, info.oneStarCount, info.totalRatings)
                }
            }
        }

        // Recent feedback
        item {
            Text(
                text = "Recent Feedback",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(info.recentFeedback) { feedback ->
            FeedbackCard(feedback)
        }
    }
}

@Composable
private fun StarDistributionRow(stars: Int, count: Int, total: Int) {
    val fraction = if (total > 0) count.toFloat() / total else 0f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$stars",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(16.dp)
        )
        Icon(Icons.Filled.Star, contentDescription = null, tint = Yellow500, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .weight(1f)
                .height(8.dp),
            color = Yellow500,
            trackColor = Gray200,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$count",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp)
        )
    }
}

@Composable
private fun FeedbackCard(feedback: FeedbackItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = feedback.customerName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Row {
                    repeat(feedback.rating) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = Yellow500, modifier = Modifier.size(16.dp))
                    }
                    repeat(5 - feedback.rating) {
                        Icon(Icons.Filled.StarBorder, contentDescription = null, tint = Gray300, modifier = Modifier.size(16.dp))
                    }
                }
            }
            feedback.comment?.let { comment ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "\"$comment\"",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Job #${feedback.jobId}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
