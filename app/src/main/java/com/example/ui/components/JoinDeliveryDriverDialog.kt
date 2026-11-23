package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.DeliveryVehicleType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinDeliveryDriverDialog(
    onDismiss: () -> Unit,
    onSubmitDriver: (fullName: String, phone: String, vehicleType: DeliveryVehicleType) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedVehicle by remember { mutableStateOf(DeliveryVehicleType.MOTORCYCLE) }
    var vehicleDropdownExpanded by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    InAppModalDialog(
        title = "انضم كسائق توصيل أو نقل",
        onDismiss = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val trimmedName = fullName.trim()
                    val trimmedPhone = phone.trim()
                    when {
                        trimmedName.length < 2 ->
                            validationError = "يرجى إدخال الاسم الكامل (حرفين على الأقل)"
                        trimmedPhone.length < 4 ->
                            validationError = "يرجى إدخال رقم هاتف صحيح للتواصل"
                        else -> {
                            onSubmitDriver(trimmedName, trimmedPhone, selectedVehicle)
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

            OutlinedTextField(
                value = phone,
                onValueChange = {
                    if (it.length <= 25) phone = it
                    validationError = null
                },
                label = { Text("رقم الهاتف *") },
                placeholder = { Text("09XXXXXXXX") },
                leadingIcon = {
                    Icon(Icons.Filled.Phone, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("driver_phone_input")
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                        value = selectedVehicle.arabicLabel,
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = {
                            Icon(
                                imageVector = deliveryVehicleIcon(selectedVehicle),
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
                        DeliveryVehicleType.entries.forEach { vehicle ->
                            DropdownMenuItem(
                                text = { Text(vehicle.arabicLabel) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = deliveryVehicleIcon(vehicle),
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    selectedVehicle = vehicle
                                    vehicleDropdownExpanded = false
                                },
                                modifier = Modifier.testTag("driver_vehicle_option_${vehicle.name.lowercase()}")
                            )
                        }
                    }
                }
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
