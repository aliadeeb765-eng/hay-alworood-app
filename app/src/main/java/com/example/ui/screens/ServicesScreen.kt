package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.data.model.CraftFilterCategory
import com.example.data.model.FavoriteService
import com.example.data.model.NeighborhoodService
import com.example.data.model.NeighborhoodServicesCatalog
import com.example.data.model.ServiceAvailability
import com.example.data.model.ServiceCategory
import com.example.data.model.ServiceProvider
import com.example.data.model.ServiceRecommendation
import com.example.data.model.supportsStoreDelivery
import com.example.ui.components.CraftCategoryFilterChipGroup
import com.example.ui.components.SectorFilterChips
import com.example.ui.components.StarterNeighborhoodBanner
import com.example.ui.components.dialPhoneNumber
import com.example.ui.components.serviceIconForCraft
import com.example.ui.state.UiState
import com.example.ui.theme.DamaskGold
import com.example.ui.theme.DamaskGoldContainer
import com.example.ui.theme.HayAlWurudTheme
import com.example.ui.theme.OnDamaskGoldContainer
import com.example.ui.theme.StatusAvailableBg
import com.example.ui.theme.StatusAvailableGreen
import com.example.ui.theme.StatusCrowdedAmber
import com.example.ui.theme.StatusCrowdedBg

@Composable
fun ServicesScreen(
    currentUserId: String,
    servicesState: UiState<List<ServiceProvider>>,
    favoritesState: UiState<List<FavoriteService>>,
    recommendationsState: UiState<List<ServiceRecommendation>>,
    searchQuery: String,
    selectedCategory: CraftFilterCategory,
    selectedSector: String?,
    sortByTrust: Boolean,
    isMutating: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSelectCategory: (CraftFilterCategory) -> Unit,
    onSelectSector: (String?) -> Unit,
    onToggleSortByTrust: () -> Unit,
    onToggleFavorite: (ServiceProvider, Boolean) -> Unit,
    onToggleRecommend: (ServiceProvider, Boolean) -> Unit,
    onDeleteService: (String) -> Unit,
    onOpenAddService: () -> Unit,
    onLoadStarterData: () -> Unit,
    onOrderDeliveryFromService: (ServiceProvider) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val favoritesList = (favoritesState as? UiState.Success)?.data ?: emptyList()
    val favoriteIds = remember(favoritesList) { favoritesList.map { it.serviceId }.toSet() }

    val recommendationsList = (recommendationsState as? UiState.Success)?.data ?: emptyList()
    val recommendationCounts = remember(recommendationsList) {
        recommendationsList.groupingBy { it.serviceId }.eachCount()
    }
    val myRecommendedIds = remember(recommendationsList, currentUserId) {
        recommendationsList.filter { it.userId == currentUserId }.map { it.serviceId }.toSet()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenAddService,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_service_fab")
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("أضف محلك وعروضك")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "دليل حرفيي وخدمات الحي",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        FilterChip(
                            selected = sortByTrust,
                            onClick = onToggleSortByTrust,
                            label = { Text(if (sortByTrust) "الأكثر توصية" else "الأحدث") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Verified,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.testTag("sort_by_trust_chip"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DamaskGoldContainer,
                                selectedLabelColor = OnDamaskGoldContainer
                            )
                        )
                    }

                    CraftCategoryFilterChipGroup(
                        selectedFilter = selectedCategory,
                        onSelectFilter = onSelectCategory
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("services_lazy_column"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("ابحث عن كهربائي، سباك، صيدلية، فرن...") },
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
                            .testTag("services_search_input")
                    )
                }

                item {
                    SectorFilterChips(
                        selectedSector = selectedSector,
                        onSelectSector = onSelectSector,
                        includeAll = true
                    )
                }

                when (servicesState) {
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
                                    text = servicesState.message,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                    is UiState.Success -> {
                        val allServices = servicesState.data
                        if (allServices.isEmpty()) {
                            item {
                                StarterNeighborhoodBanner(
                                    isMutating = isMutating,
                                    onLoadStarterData = onLoadStarterData
                                )
                            }
                        } else {
                            val filtered = allServices.filter { item ->
                                val matchesCat = selectedCategory.matches(item)
                                val matchesSec = selectedSector == null || item.sector == selectedSector
                                val q = searchQuery.trim()
                                val matchesQuery = q.isEmpty() ||
                                    item.name.contains(q, ignoreCase = true) ||
                                    item.specialty.contains(q, ignoreCase = true) ||
                                    (item.notes?.contains(q, ignoreCase = true) == true)
                                matchesCat && matchesSec && matchesQuery
                            }

                        val displayed = if (sortByTrust) {
                            filtered.sortedWith(
                                compareByDescending<ServiceProvider> { recommendationCounts[it.id] ?: 0 }
                                    .thenByDescending { it.createdAt?.seconds ?: 0L }
                            )
                        } else {
                            filtered
                        }

                        if (displayed.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                    )
                                ) {
                                    Text(
                                        text = "لا توجد خدمات مطابقة للفلتر الحالي. يمكنك إضافة حرفي أو محل جديد من الزر بالأسفل.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(18.dp)
                                    )
                                }
                            }
                        } else {
                            items(displayed, key = { it.id }) { service ->
                                val isFav = favoriteIds.contains(service.id)
                                val isRec = myRecommendedIds.contains(service.id)
                                val recCount = recommendationCounts[service.id] ?: 0
                                ServiceProviderCard(
                                    service = service,
                                    isFavorited = isFav,
                                    isRecommendedByMe = isRec,
                                    recommendationCount = recCount,
                                    isOwnedByMe = service.userId == currentUserId,
                                    onCallClick = { dialPhoneNumber(context, service.workPhone) },
                                    onToggleFavorite = { onToggleFavorite(service, isFav) },
                                    onToggleRecommend = { onToggleRecommend(service, isRec) },
                                    onDeleteClick = { onDeleteService(service.id) },
                                    onOrderDeliveryClick = { onOrderDeliveryFromService(service) }
                                )
                            }
                        }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceProviderCard(
    service: ServiceProvider,
    isFavorited: Boolean,
    isRecommendedByMe: Boolean,
    recommendationCount: Int,
    isOwnedByMe: Boolean,
    onCallClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleRecommend: () -> Unit,
    onDeleteClick: () -> Unit,
    onOrderDeliveryClick: () -> Unit
) {
    val category = service.parsedCategory
    val resolvedCraft = CraftFilterCategory.resolveCraftCategory(
        category = category,
        name = service.name,
        specialty = service.specialty
    )
    val (badgeBg, badgeTint) = when (category) {
        ServiceCategory.HEALTH -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.secondary
        ServiceCategory.FOOD -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("service_card_${service.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            width = if (recommendationCount >= 3) 1.5.dp else 1.dp,
            color = if (recommendationCount >= 3) DamaskGold.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = serviceIconForCraft(category, service.name, service.specialty),
                            contentDescription = resolvedCraft.arabicLabel,
                            tint = badgeTint,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = badgeBg,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = resolvedCraft.arabicLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeTint,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }

                        if (recommendationCount > 0) {
                            Surface(
                                color = DamaskGoldContainer,
                                shape = RoundedCornerShape(50)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Verified,
                                        contentDescription = null,
                                        tint = DamaskGold,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = if (recommendationCount >= 3) {
                                            "موثوق من الجيران ($recommendationCount)"
                                        } else {
                                            "توصية جيران ($recommendationCount)"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnDamaskGoldContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = service.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = service.specialty,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (isOwnedByMe) {
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "حذف الخدمة",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Call,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "رقم التواصل: ${service.workPhone}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = service.sector,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    service.workingHours?.takeIf { it.isNotBlank() }?.let { hours ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "الدوام: $hours",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            service.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onCallClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("call_service_${service.id}"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("اتصال • ${service.workPhone}", maxLines = 1)
                }

                FilledTonalButton(
                    onClick = onToggleRecommend,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("recommend_service_${service.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isRecommendedByMe) DamaskGoldContainer else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isRecommendedByMe) OnDamaskGoldContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = if (isRecommendedByMe) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = if (isRecommendedByMe) "إلغاء التوصية" else "أنصح به",
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (isRecommendedByMe) "مُوصى ($recommendationCount)" else "أنصح به ($recommendationCount)",
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("favorite_service_${service.id}"),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorited) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = if (isFavorited) "إزالة من المحفوظات" else "حفظ الرقم",
                        tint = if (isFavorited) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (service.supportsStoreDelivery()) {
                Spacer(Modifier.height(10.dp))
                FilledTonalButton(
                    onClick = onOrderDeliveryClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("order_delivery_service_${service.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalShipping,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("اطلب توصيل من هذا المحل", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Initial standalone services & craftsmen list screen powered by LazyColumn and [NeighborhoodService].
 */
@Composable
fun InitialNeighborhoodServicesScreen(
    services: List<NeighborhoodService> = NeighborhoodServicesCatalog.initialServices,
    onCallService: (NeighborhoodService) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedFilter by rememberSaveable { mutableStateOf(CraftFilterCategory.ALL) }
    val filteredServices = remember(services, selectedFilter) {
        services.filter { selectedFilter.matches(it) }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(top = 12.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "الخدمات والحرفيون المتوفرون في الحي",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "قائمة مبدئية بأهم المهن والخدمات في حي الورود بدمشق",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                CraftCategoryFilterChipGroup(
                    selectedFilter = selectedFilter,
                    onSelectFilter = { selectedFilter = it }
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("initial_services_lazy_column"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredServices, key = { it.id }) { service ->
                InitialNeighborhoodServiceCard(
                    service = service,
                    onCallClick = { onCallService(service) }
                )
            }
        }
    }
}

@Composable
private fun InitialNeighborhoodServiceCard(
    service: NeighborhoodService,
    onCallClick: () -> Unit
) {
    val isAvailable = service.availability == ServiceAvailability.AVAILABLE_NOW
    val resolvedCraft = CraftFilterCategory.resolveCraftCategory(
        category = service.category,
        name = service.name,
        specialty = service.profession
    )
    val (badgeBg, badgeTint) = when (service.category) {
        ServiceCategory.HEALTH -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.secondary
        ServiceCategory.FOOD -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("initial_service_card_${service.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = serviceIconForCraft(
                                service.category,
                                service.name,
                                service.profession
                            ),
                            contentDescription = resolvedCraft.arabicLabel,
                            tint = badgeTint,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = badgeBg,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = resolvedCraft.arabicLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeTint,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            color = if (isAvailable) StatusAvailableBg else StatusCrowdedBg,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = service.availability.arabicLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isAvailable) StatusAvailableGreen else StatusCrowdedAmber,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = service.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = service.profession,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Call,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "رقم التواصل: ${service.phoneNumber}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = service.sector,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    service.workingHours?.let { hours ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "الدوام: $hours",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            service.description?.takeIf { it.isNotBlank() }?.let { desc ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onCallClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("اتصال • ${service.phoneNumber}", maxLines = 1)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InitialNeighborhoodServicesScreenPreview() {
    HayAlWurudTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            InitialNeighborhoodServicesScreen()
        }
    }
}

