package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FaceRetouchingNatural
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPhone
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CraftFilterCategory
import com.example.data.model.DamascusEmergencyContacts
import com.example.data.model.HayAlWurudSectors
import com.example.data.model.ServiceCategory
import com.example.data.model.StoreCategory
import com.example.data.model.UtilityStatus
import com.example.data.model.UtilityType
import com.example.ui.theme.StatusAvailableBg
import com.example.ui.theme.StatusAvailableGreen
import com.example.ui.theme.StatusCrowdedAmber
import com.example.ui.theme.StatusCrowdedBg
import com.example.ui.theme.StatusCutBg
import com.example.ui.theme.StatusCutRed
import com.example.ui.theme.StatusScheduledBg
import com.example.ui.theme.StatusScheduledBlue
import com.google.firebase.Timestamp
import java.util.concurrent.TimeUnit

fun dialPhoneNumber(context: Context, phone: String) {
    val sanitized = phone.filter { it.isDigit() || it == '+' }
    if (sanitized.isBlank()) {
        Toast.makeText(context, "رقم الهاتف غير متوفر", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$sanitized"))
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "تعذّر فتح تطبيق الاتصال", Toast.LENGTH_SHORT).show()
    }
}

fun openWhatsAppMessage(context: Context, phone: String, message: String) {
    val digits = phone.filter { it.isDigit() }
    val normalized = when {
        digits.startsWith("09") && digits.length == 10 -> "963${digits.drop(1)}"
        digits.startsWith("00") -> digits.drop(2)
        else -> digits
    }
    if (normalized.isBlank()) {
        Toast.makeText(context, "رقم الواتساب غير متوفر", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val encoded = Uri.encode(message)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$normalized?text=$encoded"))
        context.startActivity(intent)
    } catch (_: Exception) {
        dialPhoneNumber(context, phone)
    }
}

fun formatArabicRelativeTime(timestamp: Timestamp?): String {
    val ts = timestamp ?: return "الآن"
    val diffMs = System.currentTimeMillis() - ts.toDate().time
    if (diffMs < 60_000L) return "الآن"
    val mins = TimeUnit.MILLISECONDS.toMinutes(diffMs)
    if (mins < 60) return "منذ $mins دقيقة"
    val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
    if (hours < 24) return "منذ $hours ساعة"
    val days = TimeUnit.MILLISECONDS.toDays(diffMs)
    return if (days == 1L) "أمس" else "منذ $days أيام"
}

fun utilityIcon(type: UtilityType): ImageVector = when (type) {
    UtilityType.WATER -> Icons.Filled.WaterDrop
    UtilityType.ELECTRICITY -> Icons.Filled.Bolt
    UtilityType.BREAD -> Icons.Filled.BakeryDining
    UtilityType.GAS -> Icons.Filled.LocalFireDepartment
    UtilityType.TRANSPORT -> Icons.Filled.DirectionsBus
}

fun serviceCategoryIcon(category: ServiceCategory): ImageVector = when (category) {
    ServiceCategory.MAINTENANCE -> Icons.Filled.Handyman
    ServiceCategory.HEALTH -> Icons.Filled.LocalHospital
    ServiceCategory.FOOD -> Icons.Filled.BakeryDining
    ServiceCategory.TRANSPORT -> Icons.Filled.LocalShipping
    ServiceCategory.EDUCATION -> Icons.Filled.School
    ServiceCategory.OTHER -> Icons.Filled.HomeRepairService
}

fun craftFilterIcon(filter: CraftFilterCategory): ImageVector = when (filter) {
    CraftFilterCategory.ALL -> Icons.Filled.Apps
    CraftFilterCategory.PLUMBING -> Icons.Filled.Plumbing
    CraftFilterCategory.ELECTRICAL -> Icons.Filled.ElectricalServices
    CraftFilterCategory.MAINTENANCE -> Icons.Filled.Handyman
    CraftFilterCategory.HEALTH -> Icons.Filled.LocalHospital
    CraftFilterCategory.FOOD -> Icons.Filled.BakeryDining
    CraftFilterCategory.TRANSPORT -> Icons.Filled.LocalShipping
    CraftFilterCategory.EDUCATION -> Icons.Filled.School
    CraftFilterCategory.OTHER -> Icons.Filled.HomeRepairService
}

fun serviceIconForCraft(
    category: ServiceCategory,
    name: String,
    specialty: String
): ImageVector = craftFilterIcon(
    CraftFilterCategory.resolveCraftCategory(
        category = category,
        name = name,
        specialty = specialty
    )
)

fun storeCategoryIcon(category: StoreCategory): ImageVector = when (category) {
    StoreCategory.CLOTHING -> Icons.Filled.Checkroom
    StoreCategory.SHOES -> Icons.Filled.ShoppingBag
    StoreCategory.COSMETICS -> Icons.Filled.FaceRetouchingNatural
    StoreCategory.STATIONERY -> Icons.Filled.MenuBook
    StoreCategory.TECH_ACCESSORIES -> Icons.Filled.Computer
    StoreCategory.HOME_GIFTS -> Icons.Filled.CardGiftcard
    StoreCategory.OTHER -> Icons.Filled.Storefront
}

@Composable
fun CraftCategoryFilterChipGroup(
    selectedFilter: CraftFilterCategory,
    onSelectFilter: (CraftFilterCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("services_category_filter_group"),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(CraftFilterCategory.entries, key = { it.name }) { filter ->
            val selected = selectedFilter == filter
            FilterChip(
                selected = selected,
                onClick = {
                    val nextFilter = if (selected && filter != CraftFilterCategory.ALL) {
                        CraftFilterCategory.ALL
                    } else {
                        filter
                    }
                    onSelectFilter(nextFilter)
                },
                label = {
                    Text(
                        text = filter.arabicLabel,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = craftFilterIcon(filter),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}")
            )
        }
    }
}

@Composable
fun UtilityStatusBadge(status: UtilityStatus, modifier: Modifier = Modifier) {
    val (bg, fg, icon) = when (status) {
        UtilityStatus.AVAILABLE -> Triple(StatusAvailableBg, StatusAvailableGreen, Icons.Filled.CheckCircle)
        UtilityStatus.CUT -> Triple(StatusCutBg, StatusCutRed, Icons.Filled.ErrorOutline)
        UtilityStatus.CROWDED -> Triple(StatusCrowdedBg, StatusCrowdedAmber, Icons.Filled.WarningAmber)
        UtilityStatus.SCHEDULED -> Triple(StatusScheduledBg, StatusScheduledBlue, Icons.Filled.Schedule)
    }
    Surface(
        modifier = modifier,
        color = bg,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, fg.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = status.arabicLabel,
                tint = fg,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = status.arabicLabel,
                style = MaterialTheme.typography.labelMedium,
                color = fg,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SectorFilterChips(
    selectedSector: String?,
    onSelectSector: (String?) -> Unit,
    includeAll: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (includeAll) {
            FilterChip(
                selected = selectedSector == null,
                onClick = { onSelectSector(null) },
                label = { Text("كل الحارات") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
        HayAlWurudSectors.ALL.forEach { sector ->
            val selected = selectedSector == sector
            FilterChip(
                selected = selected,
                onClick = { onSelectSector(if (selected && includeAll) null else sector) },
                label = { Text(sector) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
fun StarterNeighborhoodBanner(
    isMutating: Boolean,
    onLoadStarterData: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ابدأ بدليل حي الورود الجاهز",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "أضف قائمة تأسيسية تضم أشهر الحرفيين، حالة المرافق، وإعلانات الجيران لتبدأ فوراً.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onLoadStarterData,
                enabled = !isMutating,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("load_starter_data_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = Color.White
                )
            ) {
                if (isMutating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("تعبئة دليل حي الورود الأساسي")
                }
            }
        }
    }
}

@Composable
fun OfflineStatusBanner(
    isOnline: Boolean,
    hasPendingWrites: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !isOnline || hasPendingWrites,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier
    ) {
        val bg = if (!isOnline) StatusCrowdedBg else StatusScheduledBg
        val fg = if (!isOnline) StatusCrowdedAmber else StatusScheduledBlue
        val icon = if (!isOnline) Icons.Filled.WifiOff else Icons.Filled.Sync
        val message = when {
            !isOnline && hasPendingWrites ->
                "أنت بدون إنترنت — لديك تحديثات محفوظة محلياً وستُرفع تلقائياً فور عودة الشبكة."
            !isOnline ->
                "الوضع المحلي (بدون إنترنت) — يتم عرض دليل الحي المحفوظ وأرقام الطوارئ."
            else ->
                "جارٍ مزامنة تعديلاتك المحلية مع السحابة..."
        }

        Surface(
            color = bg,
            border = BorderStroke(1.dp, fg.copy(alpha = 0.25f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("offline_status_banner")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = fg,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = fg,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun EmergencyQuickDialRow(
    context: Context,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DamascusEmergencyContacts.ALL.forEach { contact ->
            Surface(
                modifier = Modifier
                    .clickable { dialPhoneNumber(context, contact.number) }
                    .testTag("emergency_chip_${contact.number}"),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalPhone,
                        contentDescription = "اتصال بـ ${contact.title}",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = contact.title,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = contact.number,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GuestModeBanner(
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
        modifier = modifier
            .fillMaxWidth()
            .testTag("guest_mode_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.PersonOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "وضع المعاينة المفتوح (بدون تسجيل) — التغييرات مؤقتة",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = onSignInClick,
                modifier = Modifier.testTag("guest_banner_sign_in_button")
            ) {
                Text(
                    text = "تسجيل Google",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

