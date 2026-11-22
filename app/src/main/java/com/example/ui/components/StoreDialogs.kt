package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.NeighborhoodStore
import com.example.data.model.StoreCategory

@Composable
fun InAppModalDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    BackHandler { onDismiss() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .heightIn(max = 640.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    content()
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dismissButton()
                    Spacer(Modifier.widthIn(min = 8.dp))
                    confirmButton()
                }
            }
        }
    }
}

@Composable
fun RegisterStoreDialog(
    defaultSector: String,
    onDismiss: () -> Unit,
    onSubmit: (
        name: String,
        category: StoreCategory,
        sector: String,
        phone: String,
        whatsappPhone: String?,
        workingHours: String?,
        description: String?
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(StoreCategory.CLOTHING) }
    var sector by remember { mutableStateOf(defaultSector) }
    var phone by remember { mutableStateOf("") }
    var whatsappPhone by remember { mutableStateOf("") }
    var workingHours by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    InAppModalDialog(
        title = "تسجيل محل تجاري في الحي",
        onDismiss = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val trimmedName = name.trim()
                    val trimmedPhone = phone.trim()
                    when {
                        trimmedName.length < 2 -> validationError = "يرجى إدخال اسم محل من حرفين على الأقل"
                        trimmedPhone.length < 4 -> validationError = "يرجى إدخال رقم هاتف صحيح"
                        else -> {
                            onSubmit(
                                trimmedName,
                                category,
                                sector,
                                trimmedPhone,
                                whatsappPhone,
                                workingHours,
                                description
                            )
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.testTag("submit_store_button")
            ) {
                Text("تسجيل المحل")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 100) name = it; validationError = null },
                label = { Text("اسم المحل التجاري *") },
                placeholder = { Text("مثال: بوتيك الشام للألبسة والأحذية") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("store_name_input")
            )

            Text("تصنيف المحل:", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StoreCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat.arabicName) }
                    )
                }
            }

            OutlinedTextField(
                value = phone,
                onValueChange = { if (it.length <= 30) phone = it; validationError = null },
                label = { Text("رقم هاتف المحل *") },
                placeholder = { Text("09XXXXXXXX") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("store_phone_input")
            )

            OutlinedTextField(
                value = whatsappPhone,
                onValueChange = { if (it.length <= 30) whatsappPhone = it },
                label = { Text("رقم واتساب للطلبات (اختياري)") },
                placeholder = { Text("يُترك فارغاً إذا كان نفس رقم الهاتف") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("الحارة / الموقع:", style = MaterialTheme.typography.labelLarge)
            SectorFilterChips(
                selectedSector = sector,
                onSelectSector = { if (it != null) sector = it },
                includeAll = false
            )

            OutlinedTextField(
                value = workingHours,
                onValueChange = { if (it.length <= 80) workingHours = it },
                label = { Text("ساعات الدوام (اختياري)") },
                placeholder = { Text("مثال: 10 صباحاً - 9 مساءً") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { if (it.length <= 300) description = it },
                label = { Text("نبذة عن البضائع المتوفرة (اختياري)") },
                placeholder = { Text("ألبسة، أحذية، مستحضرات تجميل، قرطاسية...") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            validationError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun AddStoreProductDialog(
    store: NeighborhoodStore,
    onDismiss: () -> Unit,
    onSubmit: (
        title: String,
        priceText: String,
        originalPriceText: String?,
        onOffer: Boolean,
        specsText: String?,
        description: String?
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var onOffer by remember { mutableStateOf(false) }
    var originalPriceText by remember { mutableStateOf("") }
    var specsText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    InAppModalDialog(
        title = "إضافة بضاعة إلى ${store.name}",
        onDismiss = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val trimmedTitle = title.trim()
                    val trimmedPrice = priceText.trim()
                    when {
                        trimmedTitle.length < 2 -> validationError = "يرجى إدخال اسم البضاعة من حرفين على الأقل"
                        trimmedPrice.isEmpty() -> validationError = "يرجى إدخال السعر"
                        else -> {
                            onSubmit(
                                trimmedTitle,
                                trimmedPrice,
                                originalPriceText,
                                onOffer,
                                specsText,
                                description
                            )
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.testTag("submit_product_button")
            ) {
                Text("نشر البضاعة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { if (it.length <= 100) title = it; validationError = null },
                label = { Text("اسم المنتج أو البضاعة *") },
                placeholder = { Text("مثال: حذاء رياضي / طقم مكياج / ماوس لاسلكي") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_title_input")
            )

            OutlinedTextField(
                value = priceText,
                onValueChange = { if (it.length <= 60) priceText = it; validationError = null },
                label = { Text("السعر الحالي *") },
                placeholder = { Text("مثال: 150 ألف ل.س") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_price_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تفعيل ضمن عروض وتخفيضات اليوم",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = onOffer,
                    onCheckedChange = { onOffer = it },
                    modifier = Modifier.testTag("product_offer_switch")
                )
            }

            if (onOffer) {
                OutlinedTextField(
                    value = originalPriceText,
                    onValueChange = { if (it.length <= 60) originalPriceText = it },
                    label = { Text("السعر قبل الخصم (اختياري)") },
                    placeholder = { Text("مثال: 195 ألف ل.س") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = specsText,
                onValueChange = { if (it.length <= 100) specsText = it },
                label = { Text("المقاسات / الألوان / المواصفات (اختياري)") },
                placeholder = { Text("مثال: نمرة 38-43 / ضمان 6 أشهر") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { if (it.length <= 300) description = it },
                label = { Text("وصف مختصر (اختياري)") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            validationError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
