package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.HayAlWurudSectors
import com.example.data.model.PostCategory
import com.example.data.model.ServiceCategory
import com.example.data.model.UtilityStatus
import com.example.data.model.UtilityType

@Composable
fun AddUtilityReportDialog(
    defaultSector: String,
    onDismiss: () -> Unit,
    onSubmit: (UtilityType, String, UtilityStatus, String?) -> Unit
) {
    var selectedType by remember { mutableStateOf(UtilityType.WATER) }
    var selectedSector by remember { mutableStateOf(defaultSector) }
    var selectedStatus by remember { mutableStateOf(UtilityStatus.AVAILABLE) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تحديث حالة خدمة في الحي",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("نوع الخدمة:", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UtilityType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.arabicName) }
                        )
                    }
                }

                Text("الحالة الحالية:", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UtilityStatus.entries.forEach { status ->
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = { selectedStatus = status },
                            label = { Text(status.arabicLabel) }
                        )
                    }
                }

                Text("الحارة / المنطقة:", style = MaterialTheme.typography.labelLarge)
                SectorFilterChips(
                    selectedSector = selectedSector,
                    onSelectSector = { if (it != null) selectedSector = it },
                    includeAll = false
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { if (it.length <= 300) note = it },
                    label = { Text("ملاحظة مفيدة للجيران (اختياري)") },
                    placeholder = { Text("مثال: الضخ قوي وصل للخزانات / الفرن شبه فاضي") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("utility_note_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(selectedType, selectedSector, selectedStatus, note)
                    onDismiss()
                },
                modifier = Modifier.testTag("submit_utility_button")
            ) {
                Text("نشر التحديث")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun AddServiceDialog(
    defaultSector: String,
    onDismiss: () -> Unit,
    onSubmit: (
        name: String,
        category: ServiceCategory,
        specialty: String,
        sector: String,
        workPhone: String,
        workingHours: String?,
        notes: String?
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ServiceCategory.MAINTENANCE) }
    var specialty by remember { mutableStateOf("") }
    var sector by remember { mutableStateOf(defaultSector) }
    var workPhone by remember { mutableStateOf("") }
    var workingHours by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "إضافة حرفي أو محل للدليل",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 100) name = it; validationError = null },
                    label = { Text("الاسم أو اسم المحل *") },
                    placeholder = { Text("مثال: المعلم أبو محمود السباك") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("service_name_input")
                )

                Text("التصنيف:", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ServiceCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.arabicName) }
                        )
                    }
                }

                OutlinedTextField(
                    value = specialty,
                    onValueChange = { if (it.length <= 100) specialty = it; validationError = null },
                    label = { Text("المهنة / التخصص *") },
                    placeholder = { Text("مثال: تصليح غسالات وبرادات") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("service_specialty_input")
                )

                OutlinedTextField(
                    value = workPhone,
                    onValueChange = { if (it.length <= 30) workPhone = it; validationError = null },
                    label = { Text("رقم هاتف العمل *") },
                    placeholder = { Text("09XXXXXXXX") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("service_phone_input")
                )

                Text("الموقع في الحي:", style = MaterialTheme.typography.labelLarge)
                SectorFilterChips(
                    selectedSector = sector,
                    onSelectSector = { if (it != null) sector = it },
                    includeAll = false
                )

                OutlinedTextField(
                    value = workingHours,
                    onValueChange = { if (it.length <= 80) workingHours = it },
                    label = { Text("أوقات الدوام (اختياري)") },
                    placeholder = { Text("مثال: 9 صباحاً - 9 مساءً") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { if (it.length <= 300) notes = it },
                    label = { Text("ملاحظات إضافية (اختياري)") },
                    maxLines = 2,
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
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedName = name.trim()
                    val trimmedSpec = specialty.trim()
                    val trimmedPhone = workPhone.trim()
                    when {
                        trimmedName.length < 2 -> validationError = "يرجى إدخال اسم من حرفين على الأقل"
                        trimmedSpec.length < 2 -> validationError = "يرجى إدخال التخصص من حرفين على الأقل"
                        trimmedPhone.length < 4 -> validationError = "يرجى إدخال رقم هاتف صحيح"
                        else -> {
                            onSubmit(trimmedName, category, trimmedSpec, sector, trimmedPhone, workingHours, notes)
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.testTag("submit_service_button")
            ) {
                Text("حفظ في الدليل")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun AddCommunityPostDialog(
    defaultSector: String,
    defaultCategory: PostCategory = PostCategory.HELP,
    onDismiss: () -> Unit,
    onSubmit: (
        title: String,
        description: String,
        category: PostCategory,
        sector: String,
        priceText: String?,
        contactNote: String?
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(defaultCategory) }
    var sector by remember { mutableStateOf(defaultSector) }
    var priceText by remember { mutableStateOf("") }
    var contactNote by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (defaultCategory == PostCategory.HELP) {
                    "نشر طلب أو عرض فزعة"
                } else {
                    "نشر إعلان في حي الورود"
                },
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("القسم:", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PostCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.arabicName) }
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 120) title = it; validationError = null },
                    label = { Text("عنوان المنشور *") },
                    placeholder = { Text("مثال: مطلوب سلم ألمنيوم لساعة / ضاع مفتاح") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("post_title_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= 600) description = it; validationError = null },
                    label = { Text("التفاصيل *") },
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("post_description_input")
                )

                Text("الحارة:", style = MaterialTheme.typography.labelLarge)
                SectorFilterChips(
                    selectedSector = sector,
                    onSelectSector = { if (it != null) sector = it },
                    includeAll = false
                )

                if (category == PostCategory.MARKET) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { if (it.length <= 60) priceText = it },
                        label = { Text("السعر أو مجاناً (اختياري)") },
                        placeholder = { Text("مثال: 150 ألف ل.س أو تبرع مجاني") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = contactNote,
                    onValueChange = { if (it.length <= 120) contactNote = it },
                    label = { Text("طريقة التواصل أو نقطة اللقاء (اختياري)") },
                    placeholder = { Text("مثال: عند سمانة الساحة أو 09XXXXXXXX") },
                    singleLine = true,
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
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedTitle = title.trim()
                    val trimmedDesc = description.trim()
                    when {
                        trimmedTitle.length < 2 -> validationError = "يرجى كتابة عنوان واضح"
                        trimmedDesc.length < 2 -> validationError = "يرجى كتابة التفاصيل"
                        else -> {
                            onSubmit(
                                trimmedTitle,
                                trimmedDesc,
                                category,
                                sector,
                                priceText.takeIf { category == PostCategory.MARKET },
                                contactNote
                            )
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.testTag("submit_post_button")
            ) {
                Text("نشر في الحي")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
