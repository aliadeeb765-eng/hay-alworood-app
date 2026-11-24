package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.CustomOptionsPrefs
import com.example.data.model.DeliveryVehicleType
import com.example.data.model.PhoneEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinDeliveryDriverDialog(
    onDismiss: () -> Unit,
    onSubmitDriver: (fullName: String, phones: List<PhoneEntry>, vehicleTypeLabel: String) -> Unit
) {
    val context = LocalContext.current
    var fullName by remember { mutableStateOf("") }
    var driverPhones by remember { mutableStateOf(listOf(PhoneEntry("", "موبايل"))) }
    var selectedVehicle by remember { mutableStateOf(DeliveryVehicleType.MOTORCYCLE) }
    var customVehicles by remember {
        mutableStateOf(CustomOptionsPrefs.getCustomOptions(context, CustomOptionsPrefs.KEY_VEHICLE_TYPES))
    }
    var selectedCustomVehicle by remember { mutableStateOf<String?>(null) }
    var customVehicleDraft by remember { mutableStateOf("") }
    var vehicleDropdownExpanded by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    InAppModalDialog(
        title = "انضم كسائق توصيل أو نقل",
        onDismiss = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val trimmedName = fullName.trim()
                    val validPhones = driverPhones.filter { it.number.trim().isNotBlank() }
                        .map { it.copy(number = it.number.trim()) }
                    if (selectedVehicle == DeliveryVehicleType.OTHER &&
                        selectedCustomVehicle == null &&
                        customVehicleDraft.trim().isNotBlank()
                    ) {
                        val newVeh = customVehicleDraft.trim()
                        CustomOptionsPrefs.addCustomOption(context, CustomOptionsPrefs.KEY_VEHICLE_TYPES, newVeh)
                        customVehicles = CustomOptionsPrefs.getCustomOptions(context, CustomOptionsPrefs.KEY_VEHICLE_TYPES)
                        selectedCustomVehicle = newVeh
                    }
                    when {
                        trimmedName.length < 2 ->
                            validationError = "يرجى إدخال الاسم الكامل (حرفين على الأقل)"
                        validPhones.none { it.number.length >= 4 } ->
                            validationError = "يرجى إدخال رقم هاتف صحيح للتواصل"
                        selectedVehicle == DeliveryVehicleType.OTHER && selectedCustomVehicle == null ->
                            validationError = "يرجى كتابة نوع المركبة الجديد وحفظه"
                        else -> {
                            val vehicleLabel = selectedCustomVehicle ?: selectedVehicle.arabicLabel
                            onSubmitDriver(trimmedName, validPhones, vehicleLabel)
                            onDismiss()
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("submit_driver_join_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "إرسال عبر واتساب",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("cancel_driver_join_button")
            ) {
                Text("إلغاء")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("join_driver_dialog"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "سجّل بياناتك ومركبتك للانضمام إلى فريق التوصيل والنقل السريع في حي الورود:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = fullName,
                onValueChange = {
                    if (it.length <= 80) fullName = it
                    validationError = null
                },
                label = { Text("الاسم الكامل *") },
                placeholder = { Text("مثال: محمد حسن الشامي") },
                leadingIcon = {
                    Icon(Icons.Filled.Badge, contentDescription = null)
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("driver_full_name_input")
            )

            MultiplePhoneInputField(
                phones = driverPhones,
                onPhonesChange = {
                    driverPhones = it
                    validationError = null
                },
                label = "رقم الهاتف *",
                firstFieldTestTag = "driver_phone_input"
            )

            Text(
                text = "نوع المركبة *",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            ExposedDropdownMenuBox(
                expanded = vehicleDropdownExpanded,
                onExpandedChange = { vehicleDropdownExpanded = !vehicleDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedCustomVehicle ?: selectedVehicle.arabicLabel,
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = {
                        Icon(
                            imageVector = if (selectedCustomVehicle != null) {
                                Icons.Filled.LocalShipping
                            } else {
                                deliveryVehicleIcon(selectedVehicle)
                            },
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleDropdownExpanded)
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                        .testTag("driver_vehicle_dropdown")
                )
                ExposedDropdownMenu(
                    expanded = vehicleDropdownExpanded,
                    onDismissRequest = { vehicleDropdownExpanded = false }
                ) {
                    DeliveryVehicleType.entries.filter { it != DeliveryVehicleType.OTHER }.forEach { vehicle ->
                        DropdownMenuItem(
                            text = { Text("${vehicle.arabicLabel} (${vehicle.capacityHint})") },
                            leadingIcon = {
                                Icon(
                                    imageVector = deliveryVehicleIcon(vehicle),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                selectedVehicle = vehicle
                                selectedCustomVehicle = null
                                vehicleDropdownExpanded = false
                                validationError = null
                            },
                            modifier = Modifier.testTag("driver_vehicle_option_${vehicle.name.lowercase()}")
                        )
                    }
                    customVehicles.forEachIndexed { idx, customVeh ->
                        DropdownMenuItem(
                            text = { Text(customVeh) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.LocalShipping,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                selectedVehicle = DeliveryVehicleType.OTHER
                                selectedCustomVehicle = customVeh
                                vehicleDropdownExpanded = false
                                validationError = null
                            },
                            modifier = Modifier.testTag("driver_vehicle_custom_$idx")
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(DeliveryVehicleType.OTHER.arabicLabel) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.LocalShipping,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            selectedVehicle = DeliveryVehicleType.OTHER
                            selectedCustomVehicle = null
                            vehicleDropdownExpanded = false
                            validationError = null
                        },
                        modifier = Modifier.testTag("driver_vehicle_option_other")
                    )
                }
            }

            if (selectedVehicle == DeliveryVehicleType.OTHER && selectedCustomVehicle == null) {
                CustomOptionInputRow(
                    prefKey = CustomOptionsPrefs.KEY_VEHICLE_TYPES,
                    onCustomAdded = { newVeh ->
                        customVehicles = CustomOptionsPrefs.getCustomOptions(
                            context,
                            CustomOptionsPrefs.KEY_VEHICLE_TYPES
                        )
                        selectedVehicle = DeliveryVehicleType.OTHER
                        selectedCustomVehicle = newVeh
                        validationError = null
                    },
                    onDraftChange = { customVehicleDraft = it }
                )
            }

            validationError?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("driver_validation_error")
                )
            }
        }
    }
}
