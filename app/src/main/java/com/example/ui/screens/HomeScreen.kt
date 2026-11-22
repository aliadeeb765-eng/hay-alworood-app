package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalPhone
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.CommunityPost
import com.example.data.model.ServiceProvider
import com.example.data.model.UtilityType
import com.example.data.model.UtilityUpdate
import com.example.ui.components.EmergencyQuickDialRow
import com.example.ui.components.SectionHeader
import com.example.ui.components.StarterNeighborhoodBanner
import com.example.ui.components.UtilityStatusBadge
import com.example.ui.components.dialPhoneNumber
import com.example.ui.components.formatArabicRelativeTime
import com.example.ui.components.utilityIcon
import com.example.ui.state.UiState

@Composable
fun HomeScreen(
    residentName: String,
    preferredSector: String,
    currentUserId: String,
    utilitiesState: UiState<List<UtilityUpdate>>,
    servicesState: UiState<List<ServiceProvider>>,
    postsState: UiState<List<CommunityPost>>,
    isMutating: Boolean,
    onOpenAddUtility: () -> Unit,
    onOpenAddPost: () -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onDeleteUtilityReport: (String) -> Unit,
    onLoadStarterData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val utilitiesList = (utilitiesState as? UiState.Success)?.data ?: emptyList()
    val servicesList = (servicesState as? UiState.Success)?.data ?: emptyList()
    val postsList = (postsState as? UiState.Success)?.data ?: emptyList()
    val allEmpty = utilitiesState is UiState.Success &&
        servicesState is UiState.Success &&
        postsState is UiState.Success &&
        utilitiesList.isEmpty() && servicesList.isEmpty() && postsList.isEmpty()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Welcome Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hay_hero_1795316291784),
                    contentDescription = "حي الورود بدمشق",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0x33000000), Color(0xD90A2E23))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(18.dp)
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "حارتك: $preferredSector",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "يسعد صباحك يا $residentName",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "أخبار المياه، الكهرباء، الفرن، وخدمات حي الورود بين يديك",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // 2. Quick Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenAddUtility,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("home_update_utility_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddCircleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("حدّث موعد / حالة", maxLines = 1)
                }
                OutlinedButton(
                    onClick = onOpenAddPost,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("home_new_post_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Campaign,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "نشر إعلان / فزعة",
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1
                    )
                }
            }
        }

        // Starter Data Banner if Firestore is empty
        if (allEmpty) {
            item {
                StarterNeighborhoodBanner(
                    isMutating = isMutating,
                    onLoadStarterData = onLoadStarterData
                )
            }
        }

        // 3. Daily Neighborhood Schedules Overview
        item {
            SectionHeader(
                title = "المواعيد اليومية المعتادة في الحي",
                actionLabel = "تحديث",
                onAction = onOpenAddUtility
            )
            Spacer(Modifier.height(8.dp))
            DailySchedulesCard(preferredSector = preferredSector)
        }

        // 4. Live Utilities Dashboard
        item {
            SectionHeader(
                title = "بلاغات المرافق المباشرة من الجيران",
                actionLabel = "إبلاغ عن حالة",
                onAction = onOpenAddUtility
            )
        }

        when (utilitiesState) {
            is UiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is UiState.Error -> {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = utilitiesState.message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
            is UiState.Success -> {
                val latestByType = UtilityType.entries.map { type ->
                    val preferredMatch = utilitiesList.firstOrNull {
                        it.parsedType == type && it.sector == preferredSector
                    }
                    val anyMatch = utilitiesList.firstOrNull { it.parsedType == type }
                    type to (preferredMatch ?: anyMatch)
                }
                items(latestByType, key = { it.first.name }) { (type, latestReport) ->
                    UtilitySummaryCard(
                        type = type,
                        report = latestReport,
                        canDelete = latestReport?.userId == currentUserId,
                        onUpdateClick = onOpenAddUtility,
                        onDeleteClick = { latestReport?.id?.let(onDeleteUtilityReport) }
                    )
                }
            }
        }

        // 5. Quick Emergency Numbers
        item {
            SectionHeader(
                title = "أرقام الطوارئ السريعة",
                actionLabel = "دليل الحي",
                onAction = onNavigateToServices
            )
            Spacer(Modifier.height(8.dp))
            EmergencyQuickDialRow(context = context)
        }

        // 5. Latest Community Highlights
        item {
            SectionHeader(
                title = "أحدث إعلانات وفزعة الجيران",
                actionLabel = "كل المنشورات",
                onAction = onNavigateToCommunity
            )
        }

        val recentPosts = postsList.take(3)
        if (recentPosts.isEmpty() && postsState is UiState.Success) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Text(
                        text = "لا توجد إعلانات حالياً. شارك جيرانك أي خبر أو طلب فزعة!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(recentPosts, key = { it.id }) { post ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToCommunity() },
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = post.parsedCategory.arabicName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = "${post.sector} • ${formatArabicRelativeTime(post.createdAt)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = post.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = post.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Bottom spacer for comfortable scrolling
        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun UtilitySummaryCard(
    type: UtilityType,
    report: UtilityUpdate?,
    canDelete: Boolean,
    onUpdateClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUpdateClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = utilityIcon(type),
                            contentDescription = type.arabicName,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = type.arabicName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = report?.let { "${it.sector} • ${formatArabicRelativeTime(it.createdAt)}" }
                                ?: type.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (report != null) {
                    UtilityStatusBadge(status = report.parsedStatus)
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "لا يوجد بلاغ بعد",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (report?.note?.isNotBlank() == true) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "«${report.note}» — ${report.authorName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (canDelete) {
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = "حذف التحديث",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailySchedulesCard(preferredSector: String) {
    val scheduleItems = listOf(
        Triple(UtilityType.WATER, "ضخ المياه ($preferredSector)", "6:00 ص – 11:00 ص (كل يومين)"),
        Triple(UtilityType.ELECTRICITY, "برنامج الكهرباء والأمبيرات", "ساعتان وصل / 4 ساعات تقنين • أمبيرات حتى 12 ليلاً"),
        Triple(UtilityType.BREAD, "توزيع الخبز والأفران", "5:30 فجراً – 7:00 مساءً (استراحة ظهراً)"),
        Triple(UtilityType.GAS, "تسليم أسطوانات الغاز", "9:00 ص – 3:00 عصراً عند المعتمدين"),
        Triple(UtilityType.TRANSPORT, "سرافيس حي الورود – البرامكة", "6:00 ص – 11:00 ليلاً كل 5 دقائق")
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_schedules_card"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            scheduleItems.forEachIndexed { index, (type, title, timeWindow) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = utilityIcon(type),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = timeWindow,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (index < scheduleItems.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

