package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.model.NeighborhoodStore
import com.example.data.model.StoreProduct
import com.example.data.model.StoreRatingSummary
import com.example.data.model.StoreReview
import com.example.data.model.formatRatingScore
import com.example.ui.components.dialPhoneNumber
import com.example.ui.components.openWhatsAppMessage
import com.example.ui.components.storeCategoryIcon
import com.example.ui.theme.DamaskGold
import com.example.ui.theme.DamaskGoldContainer
import com.example.ui.theme.OnDamaskGoldContainer
import com.example.ui.theme.StatusAvailableBg
import com.example.ui.theme.StatusAvailableGreen
import com.example.ui.theme.StatusCutBg
import com.example.ui.theme.StatusCutRed

@Composable
fun StoreDetailView(
    store: NeighborhoodStore,
    products: List<StoreProduct>,
    reviews: List<StoreReview>,
    ratingSummary: StoreRatingSummary?,
    currentUserId: String,
    onBack: () -> Unit,
    onAddProductClick: () -> Unit,
    onRateStoreClick: () -> Unit,
    onDeleteMyReview: () -> Unit,
    onToggleStoreOpen: (String, Boolean) -> Unit,
    onDeleteStore: (String) -> Unit,
    onToggleProductOfferAndStock: (String, Boolean, Boolean) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onOrderDeliveryClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val isOwner = store.userId == currentUserId
    val contactPhone = store.whatsappPhone?.takeIf { it.isNotBlank() } ?: store.phone

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (isOwner) {
                ExtendedFloatingActionButton(
                    onClick = onAddProductClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_store_product_fab")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("أضف بضاعة أو عرض")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("store_detail_lazy_column"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("store_detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("العودة للمتاجر")
                    }

                    if (isOwner) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilterChip(
                                selected = store.storeOpen,
                                onClick = { onToggleStoreOpen(store.id, !store.storeOpen) },
                                label = { Text(if (store.storeOpen) "المحل مفتوح" else "مغلق مؤقتاً") },
                                modifier = Modifier.testTag("toggle_store_open_chip")
                            )
                            IconButton(
                                onClick = {
                                    onDeleteStore(store.id)
                                    onBack()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = "حذف المحل",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("store_detail_header"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = storeCategoryIcon(store.parsedCategory),
                                        contentDescription = store.parsedCategory.arabicName,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = store.parsedCategory.arabicName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                        )
                                    }
                                    Surface(
                                        color = if (store.storeOpen) StatusAvailableBg else StatusCutBg,
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = if (store.storeOpen) "مفتوح الآن" else "مغلق",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (store.storeOpen) StatusAvailableGreen else StatusCutRed,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = store.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        store.description?.takeIf { it.isNotBlank() }?.let { desc ->
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = store.sector,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            store.workingHours?.takeIf { it.isNotBlank() }?.let { hours ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = hours,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { dialPhoneNumber(context, store.phone) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("اتصال • ${store.phone}", maxLines = 1)
                            }
                            FilledTonalButton(
                                onClick = {
                                    openWhatsAppMessage(
                                        context,
                                        contactPhone,
                                        "مرحباً، رأيت متجركم (${store.name}) في تطبيق حي الورود وأرغب بالاستفسار عن البضائع."
                                    )
                                },
                                modifier = Modifier.height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = "واتساب",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("واتساب")
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        FilledTonalButton(
                            onClick = onOrderDeliveryClick,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("order_delivery_detail_button")
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

            item {
                StoreRatingSummaryCard(
                    ratingSummary = ratingSummary,
                    isOwner = isOwner,
                    onRateStoreClick = onRateStoreClick
                )
            }

            item {
                Text(
                    text = "البضائع والعروض المتوفرة (${products.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (products.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                imageVector = Icons.Filled.Inventory2,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = if (isOwner) {
                                    "لم تضف أي بضائع بعد. اضغط على زر «أضف بضاعة أو عرض» لعرض منتجاتك لأهل الحي."
                                } else {
                                    "لا توجد بضائع معروضة حالياً في هذا المحل."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(products, key = { it.id }) { product ->
                    StoreProductCard(
                        product = product,
                        isOwner = isOwner,
                        onCallClick = { dialPhoneNumber(context, store.phone) },
                        onWhatsAppClick = {
                            openWhatsAppMessage(
                                context,
                                contactPhone,
                                "مرحباً، أستفسر عن (${product.title}) بسعر ${product.priceText} من متجر ${store.name} عبر تطبيق حي الورود."
                            )
                        },
                        onToggleOffer = {
                            onToggleProductOfferAndStock(product.id, !product.onOffer, product.inStock)
                        },
                        onToggleStock = {
                            onToggleProductOfferAndStock(product.id, product.onOffer, !product.inStock)
                        },
                        onDelete = { onDeleteProduct(product.id) }
                    )
                }
            }

            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "تقييمات وآراء أهالي الحي (${reviews.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (reviews.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.RateReview,
                                contentDescription = null,
                                tint = DamaskGold,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "لا توجد تقييمات بعد لهذا المتجر. شارك جيرانك تجربتك حول جودة الخدمة ومصداقية العروض.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(reviews, key = { it.id }) { review ->
                    StoreReviewCard(
                        review = review,
                        isMine = review.userId == currentUserId,
                        onDeleteClick = onDeleteMyReview
                    )
                }
            }
        }
    }
}

@Composable
private fun StoreRatingSummaryCard(
    ratingSummary: StoreRatingSummary?,
    isOwner: Boolean,
    onRateStoreClick: () -> Unit
) {
    val count = ratingSummary?.reviewCount ?: 0
    val overall = ratingSummary?.overallAverage ?: 0.0
    val quality = ratingSummary?.serviceQualityAverage ?: 0.0
    val credibility = ratingSummary?.offerCredibilityAverage ?: 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("store_rating_summary_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        border = BorderStroke(1.dp, DamaskGold.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "تقييم أهالي الحي",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (count > 0) "بناءً على $count تقييم" else "بانتظار أول تقييم من الجيران",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = DamaskGoldContainer,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = DamaskGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = if (count > 0) formatRatingScore(overall) else "—",
                            style = MaterialTheme.typography.titleLarge,
                            color = OnDamaskGoldContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (count > 0) {
                RatingBreakdownBar(
                    label = "جودة الخدمة والتعامل",
                    score = quality
                )
                RatingBreakdownBar(
                    label = "مصداقية العروض والأسعار",
                    score = credibility
                )
            }

            if (!isOwner) {
                Button(
                    onClick = onRateStoreClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("rate_store_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (ratingSummary?.myReview != null) Icons.Filled.RateReview else Icons.Filled.Star,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (ratingSummary?.myReview != null) {
                            "تعديل تقييمك للمتجر"
                        } else {
                            "قيّم جودة الخدمة ومصداقية العروض"
                        }
                    )
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "أنت صاحب هذا المتجر — يمكنك متابعة تقييمات الزبائن ومؤشر مصداقية العروض هنا.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RatingBreakdownBar(
    label: String,
    score: Double
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = "${formatRatingScore(score)} / 5",
                style = MaterialTheme.typography.labelMedium,
                color = DamaskGold,
                fontWeight = FontWeight.Bold
            )
        }
        LinearProgressIndicator(
            progress = { (score / 5.0).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp),
            color = DamaskGold,
            trackColor = DamaskGoldContainer.copy(alpha = 0.65f)
        )
    }
}

@Composable
private fun StoreReviewCard(
    review: StoreReview,
    isMine: Boolean,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("store_review_card_${review.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isMine) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = review.reviewerName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (isMine) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "تقييمك",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = DamaskGoldContainer,
                        shape = RoundedCornerShape(50)
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
                                text = formatRatingScore(review.overallRating),
                                style = MaterialTheme.typography.labelSmall,
                                color = OnDamaskGoldContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (isMine) {
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.testTag("delete_review_button_${review.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = "حذف التقييم",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "جودة الخدمة: ${review.serviceQualityRating}/5",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    color = if (review.offerCredibilityRating >= 4) StatusAvailableBg else MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (review.offerCredibilityRating >= 4) {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = null,
                                tint = StatusAvailableGreen,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            text = "مصداقية العروض: ${review.offerCredibilityRating}/5",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (review.offerCredibilityRating >= 4) StatusAvailableGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            review.comment?.takeIf { it.isNotBlank() }?.let { commentText ->
                Text(
                    text = commentText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun StoreProductCard(
    product: StoreProduct,
    isOwner: Boolean,
    onCallClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onToggleOffer: () -> Unit,
    onToggleStock: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("store_product_card_${product.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        border = BorderStroke(
            width = if (product.onOffer) 1.5.dp else 1.dp,
            color = if (product.onOffer) MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (product.onOffer) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocalOffer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "عرض خاص",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Surface(
                        color = if (product.inStock) StatusAvailableBg else StatusCutBg,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = if (product.inStock) "متوفر" else "نفدت الكمية",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (product.inStock) StatusAvailableGreen else StatusCutRed,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }

                if (isOwner) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "حذف المنتج",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = product.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.priceText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                if (product.onOffer && !product.originalPriceText.isNullOrBlank()) {
                    Text(
                        text = product.originalPriceText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = TextDecoration.LineThrough
                    )
                }
            }

            product.specsText?.takeIf { it.isNotBlank() }?.let { specs ->
                Spacer(Modifier.height(6.dp))
                Surface(
                    color = DamaskGoldContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = specs,
                        style = MaterialTheme.typography.labelSmall,
                        color = OnDamaskGoldContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            product.description?.takeIf { it.isNotBlank() }?.let { desc ->
                Spacer(Modifier.height(6.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(10.dp))
            if (isOwner) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onToggleOffer,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (product.onOffer) "إلغاء العرض" else "تمييز كعرض", maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = onToggleStock,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (product.inStock) "تحديد كنفد" else "تحديد كمتوفر", maxLines = 1)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = onWhatsAppClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("طلب عبر واتساب")
                    }
                    OutlinedButton(
                        onClick = onCallClick,
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Call, contentDescription = "اتصال")
                    }
                }
            }
        }
    }
}
