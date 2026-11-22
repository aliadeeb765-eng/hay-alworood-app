package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.CommunityPost
import com.example.data.model.PostCategory
import com.example.data.model.PostStatus
import com.example.ui.components.SectorFilterChips
import com.example.ui.components.formatArabicRelativeTime
import com.example.ui.state.UiState
import com.example.ui.theme.StatusAvailableBg
import com.example.ui.theme.StatusAvailableGreen

enum class CommunityBoardMode {
    ANNOUNCEMENTS,
    FAZAA
}

@Composable
fun CommunityScreen(
    currentUserId: String,
    postsState: UiState<List<CommunityPost>>,
    searchQuery: String,
    selectedCategory: PostCategory?,
    selectedSector: String?,
    showResolvedPosts: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSelectCategory: (PostCategory?) -> Unit,
    onSelectSector: (String?) -> Unit,
    onToggleShowResolved: () -> Unit,
    onMarkResolved: (String) -> Unit,
    onDeletePost: (String) -> Unit,
    onOpenAddPost: () -> Unit,
    boardMode: CommunityBoardMode = CommunityBoardMode.ANNOUNCEMENTS,
    modifier: Modifier = Modifier
) {
    val isFazaa = boardMode == CommunityBoardMode.FAZAA
    val announcementCategories = listOf(
        PostCategory.NOTICE,
        PostCategory.MARKET,
        PostCategory.LOST_FOUND
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenAddPost,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(if (isFazaa) "طلب أو عرض فزعة" else "نشر إعلان") },
                containerColor = if (isFazaa) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondary
                },
                contentColor = if (isFazaa) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSecondary
                },
                modifier = Modifier.testTag(if (isFazaa) "add_fazaa_fab" else "add_post_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isFazaa) "فزعة الجيران والتكافل" else "إعلانات وسوق حي الورود",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = onToggleShowResolved) {
                        Icon(
                            imageVector = if (showResolvedPosts) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(if (showResolvedPosts) "إظهار المنتهي" else "المفتوح فقط")
                    }
                }
                if (isFazaa) {
                    Spacer(Modifier.height(6.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                        )
                    ) {
                        Text(
                            text = "يداً بيد في حي الورود — شارك طلب مساعدة، استعارة عدة، أو مبادرة تطوعية لجيرانك في الحارة.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            if (isFazaa) "ابحث في طلبات ومبادرات فزعة الجيران..."
                            else "ابحث في الإعلانات، السوق، أو المفقودات..."
                        )
                    },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "مسح البحث")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(if (isFazaa) "fazaa_search_input" else "community_search_input")
                )
            }

            if (!isFazaa) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { onSelectCategory(null) },
                            label = { Text("الكل") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                        announcementCategories.forEach { cat ->
                            val selected = selectedCategory == cat
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectCategory(if (selected) null else cat) },
                                label = { Text(cat.arabicName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }
                }
            }

            item {
                SectorFilterChips(
                    selectedSector = selectedSector,
                    onSelectSector = onSelectSector,
                    includeAll = true
                )
            }

            when (postsState) {
                is UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
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
                                text = postsState.message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
                is UiState.Success -> {
                    val filtered = postsState.data.filter { post ->
                        val matchesMode = if (isFazaa) {
                            post.parsedCategory == PostCategory.HELP
                        } else {
                            post.parsedCategory != PostCategory.HELP
                        }
                        val matchesCat = isFazaa || selectedCategory == null || post.parsedCategory == selectedCategory
                        val matchesSec = selectedSector == null || post.sector == selectedSector
                        val matchesStatus = showResolvedPosts || post.parsedStatus == PostStatus.OPEN
                        val q = searchQuery.trim()
                        val matchesQuery = q.isEmpty() ||
                            post.title.contains(q, ignoreCase = true) ||
                            post.description.contains(q, ignoreCase = true)
                        matchesMode && matchesCat && matchesSec && matchesStatus && matchesQuery
                    }

                    if (filtered.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_community_empty_1795316305575),
                                        contentDescription = "جلسة جيران في حي الورود",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(160.dp)
                                            .clip(RoundedCornerShape(18.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        text = if (isFazaa) "لا توجد طلبات فزعة مطابقة حالياً" else "لا توجد إعلانات مطابقة حالياً",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isFazaa) {
                                            "اضغط على «طلب أو عرض فزعة» لمساعدة جيرانك أو طلب المساعدة."
                                        } else {
                                            "اضغط على «نشر إعلان» لمشاركة أخبار الحي أو سوق الحارة."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(filtered, key = { it.id }) { post ->
                            CommunityPostCard(
                                post = post,
                                isOwnedByMe = post.userId == currentUserId,
                                onMarkResolved = { onMarkResolved(post.id) },
                                onDelete = { onDeletePost(post.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommunityPostCard(
    post: CommunityPost,
    isOwnedByMe: Boolean,
    onMarkResolved: () -> Unit,
    onDelete: () -> Unit
) {
    val isResolved = post.parsedStatus == PostStatus.RESOLVED
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isResolved) {
                MaterialTheme.colorScheme.surfaceContainerLow
            } else {
                MaterialTheme.colorScheme.surfaceContainerLowest
            }
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = post.parsedCategory.arabicName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                    Surface(
                        color = if (isResolved) StatusAvailableBg else MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = post.parsedStatus.arabicLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isResolved) StatusAvailableGreen else MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }

                if (isOwnedByMe) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "حذف المنشور",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = post.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = post.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            post.priceText?.takeIf { it.isNotBlank() }?.let { price ->
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "السعر / العرض: $price",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            post.contactNote?.takeIf { it.isNotBlank() }?.let { contact ->
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "التواصل: $contact",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${post.sector} • ${post.authorName} • ${formatArabicRelativeTime(post.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isOwnedByMe && !isResolved) {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onMarkResolved,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("resolve_post_${post.id}"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("تمت المساعدة / إغلاق المنشور")
                }
            }
        }
    }
}
