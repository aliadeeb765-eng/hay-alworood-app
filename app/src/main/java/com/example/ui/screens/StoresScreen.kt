package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.NeighborhoodStore
import com.example.data.model.StoreCategory
import com.example.data.model.StoreProduct
import com.example.data.model.StoreRatingSummary
import com.example.data.model.StoreReview
import com.example.data.model.computeStoreRatingSummaries
import com.example.data.model.formatRatingScore
import com.example.ui.components.SectorFilterChips
import com.example.ui.components.storeCategoryIcon
import com.example.ui.state.UiState
import com.example.ui.theme.DamaskGold
import com.example.ui.theme.DamaskGoldContainer
import com.example.ui.theme.OnDamaskGoldContainer
import com.example.ui.theme.StatusAvailableBg
import com.example.ui.theme.StatusAvailableGreen
import com.example.ui.theme.StatusCutBg
import com.example.ui.theme.StatusCutRed

@Composable
fun StoresScreen(
    currentUserId: String,
    storesState: UiState<List<NeighborhoodStore>>,
    storeProductsState: UiState<List<StoreProduct>>,
    storeReviewsState: UiState<List<StoreReview>>,
    searchQuery: String,
    selectedCategory: StoreCategory?,
    selectedSector: String?,
    showOffersOnly: Boolean,
    sortByRating: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSelectCategory: (StoreCategory?) -> Unit,
    onSelectSector: (String?) -> Unit,
    onToggleOffersOnly: () -> Unit,
    onToggleSortByRating: () -> Unit,
    onOpenRegisterStore: () -> Unit,
    onOpenAddProduct: (NeighborhoodStore) -> Unit,
    onOpenRateStore: (NeighborhoodStore, StoreReview?) -> Unit,
    onDeleteStoreReview: (String) -> Unit,
    onToggleStoreOpen: (String, Boolean) -> Unit,
    onDeleteStore: (String) -> Unit,
    onToggleProductOfferAndStock: (String, Boolean, Boolean) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onOrderDeliveryFromStore: (NeighborhoodStore) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var activeStoreId by rememberSaveable { mutableStateOf<String?>(null) }

    val allStores = (storesState as? UiState.Success)?.data.orEmpty()
    val allProducts = (storeProductsState as? UiState.Success)?.data.orEmpty()
    val allReviews = (storeReviewsState as? UiState.Success)?.data.orEmpty()
    val ratingSummaries = remember(allReviews, currentUserId) {
        computeStoreRatingSummaries(allReviews, currentUserId)
    }
    val selectedStore = remember(allStores, activeStoreId) {
        allStores.firstOrNull { it.id == activeStoreId }
    }

    if (selectedStore != null) {
        val storeProducts = remember(allProducts, selectedStore.id) {
            allProducts.filter { it.storeId == selectedStore.id }
        }
        val storeReviews = remember(allReviews, selectedStore.id) {
            allReviews.filter { it.storeId == selectedStore.id }
        }
        val storeSummary = ratingSummaries[selectedStore.id]
        StoreDetailView(
            store = selectedStore,
            products = storeProducts,
            reviews = storeReviews,
            ratingSummary = storeSummary,
            currentUserId = currentUserId,
            onBack = { activeStoreId = null },
            onAddProductClick = { onOpenAddProduct(selectedStore) },
            onRateStoreClick = { onOpenRateStore(selectedStore, storeSummary?.myReview) },
            onDeleteMyReview = { onDeleteStoreReview(selectedStore.id) },
            onToggleStoreOpen = onToggleStoreOpen,
            onDeleteStore = onDeleteStore,
            onToggleProductOfferAndStock = onToggleProductOfferAndStock,
            onDeleteProduct = onDeleteProduct,
            onOrderDeliveryClick = { onOrderDeliveryFromStore(selectedStore) },
            modifier = modifier
        )
        return
    }

    val productsByStore = remember(allProducts) {
        allProducts.groupBy { it.storeId }
    }
    val featuredOffers = remember(allProducts, selectedCategory, selectedSector) {
        allProducts.filter { prod ->
            prod.onOffer &&
                prod.inStock &&
                (selectedCategory == null || prod.parsedStoreCategory == selectedCategory) &&
                (selectedSector == null || prod.storeSector == selectedSector)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenRegisterStore,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("register_store_fab")
            ) {
                Icon(Icons.Filled.AddBusiness, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("سجّل محلك")
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
                            text = "أسواق ومتاجر الحي",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "${allStores.size} متجر",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = sortByRating,
                            onClick = onToggleSortByRating,
                            label = { Text("الأعلى تقييماً") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.testTag("stores_sort_rating_chip"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DamaskGoldContainer,
                                selectedLabelColor = OnDamaskGoldContainer
                            )
                        )
                        FilterChip(
                            selected = showOffersOnly,
                            onClick = onToggleOffersOnly,
                            label = { Text("عروض فقط") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.LocalOffer,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.testTag("stores_offers_only_chip"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("store_category_filter_group"),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { onSelectCategory(null) },
                                label = { Text("كل المتاجر") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Apps,
                                        contentDescription = null,
                                        modifier = Modifier.size(17.dp)
                                    )
                                },
                                modifier = Modifier.testTag("store_filter_all")
                            )
                        }
                        items(StoreCategory.entries, key = { it.name }) { cat ->
                            val selected = selectedCategory == cat
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectCategory(if (selected) null else cat) },
                                label = { Text(cat.arabicName) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = storeCategoryIcon(cat),
                                        contentDescription = null,
                                        modifier = Modifier.size(17.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.testTag("store_filter_${cat.name.lowercase()}")
                            )
                        }
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("stores_lazy_column"),
                contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text("ابحث عن محل ألبسة، أحذية، مكياج، مكتبة، كمبيوتر...") },
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
                                .testTag("stores_search_input")
                        )

                        SectorFilterChips(
                            selectedSector = selectedSector,
                            onSelectSector = onSelectSector,
                            includeAll = true
                        )
                    }
                }

                if (featuredOffers.isNotEmpty() && searchQuery.isBlank()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "عروض وتخفيضات اليوم في الحي",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${featuredOffers.size} عرض",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(featuredOffers, key = { it.id }) { offer ->
                                    DailyOfferCard(
                                        product = offer,
                                        onClick = { activeStoreId = offer.storeId }
                                    )
                                }
                            }
                        }
                    }
                }

                when (storesState) {
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
                                modifier = Modifier.padding(horizontal = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    text = storesState.message,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                    is UiState.Success -> {
                        val baseFiltered = allStores.filter { store ->
                            val matchesCat = selectedCategory == null || store.parsedCategory == selectedCategory
                            val matchesSec = selectedSector == null || store.sector == selectedSector
                            val storeProds = productsByStore[store.id].orEmpty()
                            val matchesOffer = !showOffersOnly || storeProds.any { it.onOffer && it.inStock }
                            val q = searchQuery.trim()
                            val matchesQuery = q.isEmpty() ||
                                store.name.contains(q, ignoreCase = true) ||
                                (store.description?.contains(q, ignoreCase = true) == true) ||
                                storeProds.any { it.title.contains(q, ignoreCase = true) }
                            matchesCat && matchesSec && matchesOffer && matchesQuery
                        }
                        val filteredStores = if (sortByRating) {
                            baseFiltered.sortedWith(
                                compareByDescending<NeighborhoodStore> {
                                    ratingSummaries[it.id]?.overallAverage ?: 0.0
                                }.thenByDescending {
                                    ratingSummaries[it.id]?.reviewCount ?: 0
                                }
                            )
                        } else {
                            baseFiltered
                        }

                        if (filteredStores.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Storefront,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Text(
                                            text = "لا توجد متاجر مطابقة حالياً. يمكن لأصحاب المحلات تسجيل متجرهم وعرض بضائعهم من زر «سجّل محلك».",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            items(filteredStores, key = { it.id }) { store ->
                                val storeProds = productsByStore[store.id].orEmpty()
                                val activeOfferCount = storeProds.count { it.onOffer && it.inStock }
                                StoreDirectoryCard(
                                    store = store,
                                    productCount = storeProds.size,
                                    offerCount = activeOfferCount,
                                    ratingSummary = ratingSummaries[store.id],
                                    isOwnedByMe = store.userId == currentUserId,
                                    onClick = { activeStoreId = store.id },
                                    onOrderDeliveryClick = { onOrderDeliveryFromStore(store) },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyOfferCard(
    product: StoreProduct,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick)
            .testTag("daily_offer_card_${product.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "عرض خاص",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Icon(
                    imageVector = storeCategoryIcon(product.parsedStoreCategory),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = product.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = product.storeName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.priceText,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                if (!product.originalPriceText.isNullOrBlank()) {
                    Text(
                        text = product.originalPriceText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = TextDecoration.LineThrough
                    )
                }
            }
        }
    }
}

@Composable
private fun StoreDirectoryCard(
    store: NeighborhoodStore,
    productCount: Int,
    offerCount: Int,
    ratingSummary: StoreRatingSummary?,
    isOwnedByMe: Boolean,
    onClick: () -> Unit,
    onOrderDeliveryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = store.parsedCategory
    val (badgeBg, badgeTint) = when (category) {
        StoreCategory.COSMETICS -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.secondary
        StoreCategory.TECH_ACCESSORIES, StoreCategory.STATIONERY ->
            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("store_card_${store.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            width = if (offerCount > 0) 1.3.dp else 1.dp,
            color = if (offerCount > 0) DamaskGold.copy(alpha = 0.65f) else MaterialTheme.colorScheme.outline
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
                            imageVector = storeCategoryIcon(category),
                            contentDescription = category.arabicName,
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
                                text = category.arabicName,
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeTint,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            color = if (store.storeOpen) StatusAvailableBg else StatusCutBg,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = if (store.storeOpen) "مفتوح" else "مغلق",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (store.storeOpen) StatusAvailableGreen else StatusCutRed,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        if (isOwnedByMe) {
                            Surface(
                                color = DamaskGoldContainer,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "متجرك",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnDamaskGoldContainer,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = store.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    store.description?.takeIf { it.isNotBlank() }?.let { desc ->
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (ratingSummary != null && ratingSummary.reviewCount > 0) {
                            Surface(
                                color = DamaskGoldContainer,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.testTag("store_card_rating_${store.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = DamaskGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "${formatRatingScore(ratingSummary.overallAverage)} (${ratingSummary.reviewCount} تقييم)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnDamaskGoldContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            if (ratingSummary.offerCredibilityAverage >= 4.5) {
                                Surface(
                                    color = StatusAvailableBg,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Verified,
                                            contentDescription = null,
                                            tint = StatusAvailableGreen,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "عروض موثوقة",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = StatusAvailableGreen,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "★ جديد • بانتظار أول تقييم",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.testTag("store_card_rating_${store.id}")
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(store.sector, style = MaterialTheme.typography.bodySmall)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Inventory2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (offerCount > 0) "$productCount بضاعة • $offerCount عرض" else "$productCount بضاعة",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Call,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(store.phone, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            store.workingHours?.takeIf { it.isNotBlank() }?.let { hours ->
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "الدوام: $hours",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            FilledTonalButton(
                onClick = onOrderDeliveryClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("order_delivery_store_${store.id}")
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
