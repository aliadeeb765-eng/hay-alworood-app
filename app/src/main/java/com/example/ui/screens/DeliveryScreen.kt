package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AirportShuttle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.CustomOptionsPrefs
import com.example.data.local.DeliveryCustomerPrefs
import com.example.data.location.LocationHelper
import com.example.data.model.DELIVERY_ADMIN_WHATSAPP_PHONE
import com.example.data.model.DeliveryFareEstimate
import com.example.data.model.DeliveryOrderType
import com.example.data.model.DeliveryPrefill
import com.example.data.model.DeliveryRequest
import com.example.data.model.DeliveryStatus
import com.example.data.model.DeliveryVehicleType
import com.example.data.model.DeliveryZone
import com.example.data.model.PhoneEntry
import com.example.data.model.buildDeliveryOrderWhatsAppMessage
import com.example.data.model.calculateEstimatedDeliveryFare
import com.example.ui.components.CustomOptionInputRow
import com.example.ui.components.MultiplePhoneInputField
import com.example.ui.components.deliveryVehicleIcon
import com.example.ui.components.dialPhoneNumber
import com.example.ui.components.openWhatsAppMessage
import com.example.ui.state.UiState
import com.example.ui.theme.DamaskGold
import com.example.ui.theme.DamaskGoldContainer
import com.example.ui.theme.OnDamaskGoldContainer
import com.example.ui.theme.StatusAvailableBg
import com.example.ui.theme.StatusAvailableGreen
import com.example.ui.theme.StatusCrowdedAmber
import com.example.ui.theme.StatusCrowdedBg
import com.example.ui.theme.StatusCutBg
import com.example.ui.theme.StatusCutRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DeliveryScreen(
    currentUserId: String,
    defaultCustomerName: String,
    deliveryRequestsState: UiState<List<DeliveryRequest>>,
    prefill: DeliveryPrefill?,
    isMutating: Boolean,
    onPrefillConsumed: () -> Unit,
    onSubmitDeliveryRequest: (
        orderType: DeliveryOrderType,
        vehicleType: DeliveryVehicleType,
        pickupLocation: String,
        orderDetails: String,
        deliveryZone: DeliveryZone,
        detailedAddress: String,
        gpsMapsUrl: String?,
        customerName: String,
        customerPhone: String
    ) -> Unit,
    onUpdateDeliveryStatus: (String, DeliveryStatus) -> Unit,
    onDeleteDeliveryRequest: (String) -> Unit,
    gpsLocationFetcher: suspend (android.content.Context) -> Result<String> = { ctx ->
        LocationHelper.fetchCurrentGoogleMapsUrl(ctx)
    },
    onOpenWhatsApp: (android.content.Context, String, String) -> Unit = { ctx, phone, msg ->
        openWhatsAppMessage(ctx, phone, msg)
    },
    onOpenJoinDriverDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var selectedOrderType by rememberSaveable { mutableStateOf(DeliveryOrderType.GROCERY) }
    var customOrderTypes by remember {
        mutableStateOf(CustomOptionsPrefs.getCustomOptions(context, CustomOptionsPrefs.KEY_ORDER_TYPES))
    }
    var selectedCustomOrderType by rememberSaveable { mutableStateOf<String?>(null) }
    var customOrderTypeDraft by rememberSaveable { mutableStateOf("") }

    var selectedVehicleType by rememberSaveable { mutableStateOf(DeliveryVehicleType.MOTORCYCLE) }
    var customVehicleTypes by remember {
        mutableStateOf(CustomOptionsPrefs.getCustomOptions(context, CustomOptionsPrefs.KEY_VEHICLE_TYPES))
    }
    var selectedCustomVehicleType by rememberSaveable { mutableStateOf<String?>(null) }
    var customVehicleTypeDraft by rememberSaveable { mutableStateOf("") }

    var pickupLocation by rememberSaveable { mutableStateOf("") }
    var orderDetails by rememberSaveable { mutableStateOf("") }

    var selectedDeliveryZone by rememberSaveable { mutableStateOf(DeliveryZone.INSIDE_HAY_AL_WURUD) }
    var customDeliveryZones by remember {
        mutableStateOf(CustomOptionsPrefs.getCustomOptions(context, CustomOptionsPrefs.KEY_DELIVERY_ZONES))
    }
    var selectedCustomZone by rememberSaveable { mutableStateOf<String?>(null) }
    var customZoneDraft by rememberSaveable { mutableStateOf("") }
    var zoneDropdownExpanded by remember { mutableStateOf(false) }

    var detailedAddress by rememberSaveable { mutableStateOf("") }
    var gpsMapsUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var isFetchingGps by remember { mutableStateOf(false) }
    var gpsErrorMessage by remember { mutableStateOf<String?>(null) }
    var customerName by rememberSaveable { mutableStateOf(defaultCustomerName) }
    var customerPhones by remember { mutableStateOf(listOf(PhoneEntry("", "موبايل"))) }
    var hasSavedCustomerData by remember { mutableStateOf(false) }
    var prefsLoaded by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var prefillNoticeStore by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val saved = DeliveryCustomerPrefs.readSavedCustomerInfo(context)
        if (saved.hasSavedData) {
            if (saved.customerName.isNotBlank()) customerName = saved.customerName
            if (saved.phones.isNotEmpty()) {
                customerPhones = saved.phones
            } else if (saved.customerPhone.isNotBlank()) {
                customerPhones = listOf(PhoneEntry(saved.customerPhone, "موبايل"))
            }
            selectedDeliveryZone = saved.deliveryZone
            selectedCustomZone = saved.customZoneLabel.takeIf {
                it.isNotBlank() && saved.deliveryZone == DeliveryZone.OTHER
            }
            if (saved.detailedAddress.isNotBlank()) detailedAddress = saved.detailedAddress
            hasSavedCustomerData = true
        }
        prefsLoaded = true
    }

    LaunchedEffect(prefill?.requestNonce) {
        if (prefill != null) {
            pickupLocation = prefill.pickupLocation
            selectedOrderType = prefill.orderType
            selectedCustomOrderType = null
            prefillNoticeStore = prefill.pickupLocation
            validationError = null
            onPrefillConsumed()
            listState.animateScrollToItem(0)
        }
    }

    LaunchedEffect(defaultCustomerName, prefsLoaded) {
        if (prefsLoaded && customerName.isBlank() && defaultCustomerName.isNotBlank()) {
            customerName = defaultCustomerName
        }
    }

    fun triggerGpsFetch() {
        if (isFetchingGps) return
        scope.launch {
            isFetchingGps = true
            gpsErrorMessage = null
            val result = gpsLocationFetcher(context)
            isFetchingGps = false
            result.fold(
                onSuccess = { url ->
                    gpsMapsUrl = url
                    validationError = null
                },
                onFailure = { err ->
                    gpsErrorMessage = err.message ?: "تعذّر تحديد الموقع الحالي"
                }
            )
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            triggerGpsFetch()
        } else {
            gpsErrorMessage = "تم رفض صلاحية الموقع. يمكنك كتابة العنوان التفصيلي يدوياً أو منح الصلاحية من الإعدادات."
        }
    }

    val allRequests = (deliveryRequestsState as? UiState.Success)?.data.orEmpty()

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .testTag("delivery_lazy_column"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 92.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Atmospheric Header Hero Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("delivery_hero_banner"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.LocalShipping,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "خدمة التوصيل والنقل السريع",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "من متاجر وصيدليات ومطاعم حي الورود إلى باب منزلك أو مناطق دمشق",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = DamaskGoldContainer,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "دراجة • سيارة • سوزوكي • فان • بيك أب • شاحنة",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnDamaskGoldContainer,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Express Delivery Order Form
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("delivery_order_form_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "نموذج طلب توصيل أو نقل سريع",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    AnimatedVisibility(visible = prefillNoticeStore != null) {
                        prefillNoticeStore?.let { storeTitle ->
                            Surface(
                                color = StatusAvailableBg,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("delivery_prefill_banner")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Storefront,
                                            contentDescription = null,
                                            tint = StatusAvailableGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "طلب توصيل من: $storeTitle",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = StatusAvailableGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    IconButton(
                                        onClick = { prefillNoticeStore = null }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "إخفاء الإشعار",
                                            tint = StatusAvailableGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2a. Order Type Quick Selection
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "نوع الطلب *",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.testTag("delivery_order_type_group")
                        ) {
                            DeliveryOrderType.entries.filter { it != DeliveryOrderType.OTHER }.forEach { type ->
                                val selected = selectedOrderType == type && selectedCustomOrderType == null
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        selectedOrderType = type
                                        selectedCustomOrderType = null
                                    },
                                    label = { Text(type.arabicLabel) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = orderTypeIcon(type),
                                            contentDescription = null,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.testTag("order_type_${type.name.lowercase()}")
                                )
                            }
                            customOrderTypes.forEachIndexed { idx, customType ->
                                val selected = selectedOrderType == DeliveryOrderType.OTHER &&
                                    selectedCustomOrderType == customType
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        selectedOrderType = DeliveryOrderType.OTHER
                                        selectedCustomOrderType = customType
                                    },
                                    label = { Text(customType) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.LocalOffer,
                                            contentDescription = null,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.testTag("order_type_custom_$idx")
                                )
                            }
                            val otherSelected = selectedOrderType == DeliveryOrderType.OTHER &&
                                selectedCustomOrderType == null
                            FilterChip(
                                selected = otherSelected,
                                onClick = {
                                    selectedOrderType = DeliveryOrderType.OTHER
                                    selectedCustomOrderType = null
                                },
                                label = { Text(DeliveryOrderType.OTHER.arabicLabel) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = orderTypeIcon(DeliveryOrderType.OTHER),
                                        contentDescription = null,
                                        modifier = Modifier.size(17.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.testTag("order_type_other")
                            )
                        }
                        if (selectedOrderType == DeliveryOrderType.OTHER && selectedCustomOrderType == null) {
                            CustomOptionInputRow(
                                prefKey = CustomOptionsPrefs.KEY_ORDER_TYPES,
                                onCustomAdded = { newType ->
                                    customOrderTypes = CustomOptionsPrefs.getCustomOptions(
                                        context,
                                        CustomOptionsPrefs.KEY_ORDER_TYPES
                                    )
                                    selectedOrderType = DeliveryOrderType.OTHER
                                    selectedCustomOrderType = newType
                                },
                                onDraftChange = { customOrderTypeDraft = it }
                            )
                        }
                    }

                    // 2b. Vehicle Type Selection with Icons
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "اختر نوع المركبة المناسبة *",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        val vehicleChoices = buildList {
                            DeliveryVehicleType.entries
                                .filter { it != DeliveryVehicleType.OTHER }
                                .forEach { add(VehicleGridChoice.Standard(it)) }
                            customVehicleTypes.forEachIndexed { idx, label ->
                                add(VehicleGridChoice.Custom(idx, label))
                            }
                            add(VehicleGridChoice.Standard(DeliveryVehicleType.OTHER))
                        }
                        val vehicleRows = vehicleChoices.chunked(2)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.testTag("delivery_vehicle_grid")
                        ) {
                            vehicleRows.forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowItems.forEach { itemChoice ->
                                        when (itemChoice) {
                                            is VehicleGridChoice.Standard -> {
                                                val v = itemChoice.vehicle
                                                val selected = selectedVehicleType == v &&
                                                    selectedCustomVehicleType == null
                                                VehicleSelectionCard(
                                                    title = v.arabicLabel,
                                                    hint = v.capacityHint,
                                                    icon = deliveryVehicleIcon(v),
                                                    selected = selected,
                                                    onClick = {
                                                        selectedVehicleType = v
                                                        selectedCustomVehicleType = null
                                                    },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .testTag("vehicle_${v.name.lowercase()}")
                                                )
                                            }
                                            is VehicleGridChoice.Custom -> {
                                                val selected = selectedVehicleType == DeliveryVehicleType.OTHER &&
                                                    selectedCustomVehicleType == itemChoice.label
                                                VehicleSelectionCard(
                                                    title = itemChoice.label,
                                                    hint = "مركبة مخصصة",
                                                    icon = Icons.Filled.LocalShipping,
                                                    selected = selected,
                                                    onClick = {
                                                        selectedVehicleType = DeliveryVehicleType.OTHER
                                                        selectedCustomVehicleType = itemChoice.label
                                                    },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .testTag("vehicle_custom_${itemChoice.index}")
                                                )
                                            }
                                        }
                                    }
                                    if (rowItems.size == 1) {
                                        Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                        if (selectedVehicleType == DeliveryVehicleType.OTHER && selectedCustomVehicleType == null) {
                            CustomOptionInputRow(
                                prefKey = CustomOptionsPrefs.KEY_VEHICLE_TYPES,
                                onCustomAdded = { newVeh ->
                                    customVehicleTypes = CustomOptionsPrefs.getCustomOptions(
                                        context,
                                        CustomOptionsPrefs.KEY_VEHICLE_TYPES
                                    )
                                    selectedVehicleType = DeliveryVehicleType.OTHER
                                    selectedCustomVehicleType = newVeh
                                },
                                onDraftChange = { customVehicleTypeDraft = it }
                            )
                        }
                    }

                    // 2c. Pickup Location
                    OutlinedTextField(
                        value = pickupLocation,
                        onValueChange = {
                            if (it.length <= 160) pickupLocation = it
                            validationError = null
                        },
                        label = { Text("مكان الاستلام *") },
                        placeholder = { Text("اسم المحل، المطعم، الصيدلية أو عنوان الاستلام") },
                        leadingIcon = {
                            Icon(Icons.Filled.Storefront, contentDescription = null)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pickup_location_input")
                    )

                    // 2d. Multi-line Order or Cargo Details
                    OutlinedTextField(
                        value = orderDetails,
                        onValueChange = {
                            if (it.length <= 600) orderDetails = it
                            validationError = null
                        },
                        label = { Text("تفاصيل الطلب أو الحمولة *") },
                        placeholder = { Text("اكتب الأغراض المطلوبة، الكمية، الوزن التقريبي أو أي ملاحظة للسائق...") },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("order_details_input")
                    )

                    // 2e. Delivery Zone Dropdown
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "منطقة التوصيل *",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        ExposedDropdownMenuBox(
                            expanded = zoneDropdownExpanded,
                            onExpandedChange = { zoneDropdownExpanded = !zoneDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedCustomZone ?: selectedDeliveryZone.arabicLabel,
                                onValueChange = {},
                                readOnly = true,
                                leadingIcon = {
                                    Icon(Icons.Filled.LocationOn, contentDescription = null)
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = zoneDropdownExpanded)
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                                    .testTag("delivery_zone_dropdown")
                            )
                            ExposedDropdownMenu(
                                expanded = zoneDropdownExpanded,
                                onDismissRequest = { zoneDropdownExpanded = false }
                            ) {
                                DeliveryZone.entries.filter { it != DeliveryZone.OTHER }.forEach { zone ->
                                    DropdownMenuItem(
                                        text = { Text(zone.arabicLabel) },
                                        onClick = {
                                            selectedDeliveryZone = zone
                                            selectedCustomZone = null
                                            zoneDropdownExpanded = false
                                        },
                                        modifier = Modifier.testTag("delivery_zone_option_${zone.name.lowercase()}")
                                    )
                                }
                                customDeliveryZones.forEachIndexed { idx, customZone ->
                                    DropdownMenuItem(
                                        text = { Text(customZone) },
                                        onClick = {
                                            selectedDeliveryZone = DeliveryZone.OTHER
                                            selectedCustomZone = customZone
                                            zoneDropdownExpanded = false
                                        },
                                        modifier = Modifier.testTag("delivery_zone_custom_$idx")
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text(DeliveryZone.OTHER.arabicLabel) },
                                    onClick = {
                                        selectedDeliveryZone = DeliveryZone.OTHER
                                        selectedCustomZone = null
                                        zoneDropdownExpanded = false
                                    },
                                    modifier = Modifier.testTag("delivery_zone_option_other")
                                )
                            }
                        }

                        if (selectedDeliveryZone == DeliveryZone.OTHER && selectedCustomZone == null) {
                            CustomOptionInputRow(
                                prefKey = CustomOptionsPrefs.KEY_DELIVERY_ZONES,
                                onCustomAdded = { newZone ->
                                    customDeliveryZones = CustomOptionsPrefs.getCustomOptions(
                                        context,
                                        CustomOptionsPrefs.KEY_DELIVERY_ZONES
                                    )
                                    selectedDeliveryZone = DeliveryZone.OTHER
                                    selectedCustomZone = newZone
                                },
                                onDraftChange = { customZoneDraft = it }
                            )
                        }
                    }

                    // 2f. Detailed Address + GPS Auto-Locate Button
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = detailedAddress,
                                onValueChange = {
                                    if (it.length <= 240) detailedAddress = it
                                    validationError = null
                                },
                                label = { Text("العنوان التفصيلي *") },
                                placeholder = { Text("الشارع، البناء، الطابق أو معلم قريب") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("detailed_address_input")
                            )

                            FilledTonalButton(
                                onClick = {
                                    if (LocationHelper.hasLocationPermission(context)) {
                                        triggerGpsFetch()
                                    } else {
                                        locationPermissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                },
                                enabled = !isFetchingGps,
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .widthIn(min = 124.dp, max = 152.dp)
                                    .heightIn(min = 56.dp)
                                    .testTag("gps_locate_button")
                            ) {
                                if (isFetchingGps) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.MyLocation,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        text = "تحديد موقعي تلقائياً GPS",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        gpsMapsUrl?.let { url ->
                            Surface(
                                color = StatusAvailableBg,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("gps_maps_link_badge")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Map,
                                                contentDescription = null,
                                                tint = StatusAvailableGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "تم إرفاق موقع خرائط جوجل:",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = StatusAvailableGreen,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = url,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = StatusAvailableGreen,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        TextButton(
                                            onClick = { LocationHelper.openGoogleMapsUrl(context, url) }
                                        ) {
                                            Text("فتح")
                                        }
                                        IconButton(
                                            onClick = { gpsMapsUrl = null }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Close,
                                                contentDescription = "إزالة رابط الموقع",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        gpsErrorMessage?.let { err ->
                            Text(
                                text = err,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // 2g. Customer Name & Multiple Phone Numbers
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = {
                                if (it.length <= 80) customerName = it
                                validationError = null
                            },
                            label = { Text("اسم العميل *") },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("customer_name_input")
                        )

                        MultiplePhoneInputField(
                            phones = customerPhones,
                            onPhonesChange = {
                                customerPhones = it
                                validationError = null
                            },
                            label = "رقم الهاتف *",
                            firstFieldTestTag = "customer_phone_input"
                        )
                    }

                    if (hasSavedCustomerData) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("saved_customer_data_bar")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "تم حفظ بياناتك تلقائياً",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(
                                    onClick = {
                                        DeliveryCustomerPrefs.clearCustomerInfoAsync(context)
                                        customerName = ""
                                        customerPhones = listOf(PhoneEntry("", "موبايل"))
                                        selectedDeliveryZone = DeliveryZone.INSIDE_HAY_AL_WURUD
                                        selectedCustomZone = null
                                        detailedAddress = ""
                                        hasSavedCustomerData = false
                                        validationError = null
                                    },
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("clear_saved_delivery_data_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DeleteSweep,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "مسح البيانات المحفوظة",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // 2g-bis. Instant Estimated Fare Calculator Card
                    val effectiveVehicleForFare = if (selectedCustomVehicleType != null) {
                        DeliveryVehicleType.OTHER
                    } else {
                        selectedVehicleType
                    }
                    val effectiveZoneForFare = if (selectedCustomZone != null) {
                        DeliveryZone.OTHER
                    } else {
                        selectedDeliveryZone
                    }
                    val fareEstimate = remember(effectiveVehicleForFare, effectiveZoneForFare) {
                        calculateEstimatedDeliveryFare(effectiveVehicleForFare, effectiveZoneForFare)
                    }
                    EstimatedFareCard(
                        vehicleLabel = selectedCustomVehicleType ?: selectedVehicleType.arabicLabel,
                        zoneLabel = selectedCustomZone ?: selectedDeliveryZone.arabicLabel,
                        fareEstimate = fareEstimate
                    )

                    validationError?.let { msg ->
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("delivery_validation_error")
                        ) {
                            Text(
                                text = msg,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // 2h. Submit Order Button
                    Button(
                        onClick = {
                            val trimmedPickup = pickupLocation.trim()
                            val trimmedDetails = orderDetails.trim()
                            val trimmedAddress = detailedAddress.trim()
                            val trimmedName = customerName.trim()
                            val validPhones = customerPhones
                                .filter { it.number.trim().isNotBlank() }
                                .map { it.copy(number = it.number.trim()) }

                            // Auto-save non-blank custom inputs if user pressed submit directly
                            if (selectedOrderType == DeliveryOrderType.OTHER &&
                                selectedCustomOrderType == null &&
                                customOrderTypeDraft.trim().isNotBlank()
                            ) {
                                val newType = customOrderTypeDraft.trim()
                                CustomOptionsPrefs.addCustomOption(context, CustomOptionsPrefs.KEY_ORDER_TYPES, newType)
                                customOrderTypes = CustomOptionsPrefs.getCustomOptions(context, CustomOptionsPrefs.KEY_ORDER_TYPES)
                                selectedCustomOrderType = newType
                                customOrderTypeDraft = ""
                            }
                            if (selectedVehicleType == DeliveryVehicleType.OTHER &&
                                selectedCustomVehicleType == null &&
                                customVehicleTypeDraft.trim().isNotBlank()
                            ) {
                                val newVeh = customVehicleTypeDraft.trim()
                                CustomOptionsPrefs.addCustomOption(context, CustomOptionsPrefs.KEY_VEHICLE_TYPES, newVeh)
                                customVehicleTypes = CustomOptionsPrefs.getCustomOptions(context, CustomOptionsPrefs.KEY_VEHICLE_TYPES)
                                selectedCustomVehicleType = newVeh
                                customVehicleTypeDraft = ""
                            }
                            if (selectedDeliveryZone == DeliveryZone.OTHER &&
                                selectedCustomZone == null &&
                                customZoneDraft.trim().isNotBlank()
                            ) {
                                val newZone = customZoneDraft.trim()
                                CustomOptionsPrefs.addCustomOption(context, CustomOptionsPrefs.KEY_DELIVERY_ZONES, newZone)
                                customDeliveryZones = CustomOptionsPrefs.getCustomOptions(context, CustomOptionsPrefs.KEY_DELIVERY_ZONES)
                                selectedCustomZone = newZone
                                customZoneDraft = ""
                            }

                            when {
                                trimmedPickup.length < 2 ->
                                    validationError = "يرجى إدخال مكان الاستلام (اسم المحل أو العنوان)"
                                trimmedDetails.length < 2 ->
                                    validationError = "يرجى كتابة تفاصيل الطلب أو الحمولة"
                                trimmedAddress.length < 2 && gpsMapsUrl.isNullOrBlank() ->
                                    validationError = "يرجى إدخال العنوان التفصيلي أو استخدام زر GPS"
                                trimmedName.length < 2 ->
                                    validationError = "يرجى إدخال اسم العميل"
                                validPhones.none { it.number.length >= 4 } ->
                                    validationError = "يرجى إدخال رقم هاتف صحيح للتواصل"
                                else -> {
                                    val finalOrderTypeLabel = selectedCustomOrderType ?: selectedOrderType.arabicLabel
                                    val finalVehicleLabel = selectedCustomVehicleType ?: selectedVehicleType.arabicLabel
                                    val finalZoneLabel = selectedCustomZone ?: selectedDeliveryZone.arabicLabel
                                    val finalFareEstimate = calculateEstimatedDeliveryFare(
                                        if (selectedCustomVehicleType != null) DeliveryVehicleType.OTHER else selectedVehicleType,
                                        if (selectedCustomZone != null) DeliveryZone.OTHER else selectedDeliveryZone
                                    )
                                    val formattedPhones = PhoneEntry.formatForDisplay(validPhones)

                                    DeliveryCustomerPrefs.saveCustomerInfoAsync(
                                        context = context,
                                        customerName = trimmedName,
                                        phones = validPhones,
                                        deliveryZone = selectedDeliveryZone,
                                        detailedAddress = trimmedAddress,
                                        customZoneLabel = selectedCustomZone.orEmpty()
                                    )
                                    hasSavedCustomerData = true
                                    val whatsAppMessage = buildDeliveryOrderWhatsAppMessage(
                                        orderTypeLabel = finalOrderTypeLabel,
                                        vehicleTypeLabel = finalVehicleLabel,
                                        pickupLocation = trimmedPickup,
                                        orderDetails = trimmedDetails,
                                        deliveryZoneLabel = finalZoneLabel,
                                        detailedAddress = trimmedAddress,
                                        gpsMapsUrl = gpsMapsUrl,
                                        customerName = trimmedName,
                                        customerPhones = validPhones,
                                        fareEstimate = finalFareEstimate
                                    )
                                    onSubmitDeliveryRequest(
                                        selectedOrderType,
                                        selectedVehicleType,
                                        trimmedPickup,
                                        trimmedDetails,
                                        selectedDeliveryZone,
                                        trimmedAddress,
                                        gpsMapsUrl,
                                        trimmedName,
                                        formattedPhones
                                    )
                                    onOpenWhatsApp(
                                        context,
                                        DELIVERY_ADMIN_WHATSAPP_PHONE,
                                        whatsAppMessage
                                    )
                                    pickupLocation = ""
                                    orderDetails = ""
                                    gpsMapsUrl = null
                                    prefillNoticeStore = null
                                    validationError = null
                                }
                            }
                        },
                        enabled = !isMutating,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_delivery_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "إرسال الطلب عبر واتساب",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. Active & Recent Delivery Requests Section
        item {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "طلبات التوصيل والنقل في الحي",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "${allRequests.size} طلب",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        when (deliveryRequestsState) {
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
                            text = deliveryRequestsState.message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            is UiState.Success -> {
                if (allRequests.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Text(
                                text = "لا توجد طلبات توصيل حالياً. املأ النموذج أعلاه لإرسال أول طلب توصيل أو نقل سريع.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(18.dp)
                            )
                        }
                    }
                } else {
                    items(allRequests, key = { it.id }) { request ->
                        DeliveryRequestCard(
                            request = request,
                            isMine = request.userId == currentUserId,
                            onOpenMaps = { url -> LocationHelper.openGoogleMapsUrl(context, url) },
                            onCallCustomer = { dialPhoneNumber(context, request.customerPhone) },
                            onMarkCompleted = {
                                onUpdateDeliveryStatus(request.id, DeliveryStatus.COMPLETED)
                            },
                            onCancel = {
                                onUpdateDeliveryStatus(request.id, DeliveryStatus.CANCELLED)
                            },
                            onDelete = { onDeleteDeliveryRequest(request.id) }
                        )
                    }
                }
            }
        }

        // 4. Join as Delivery / Transport Driver Recruitment Section
        item(key = "join_driver_section") {
            JoinDeliveryDriverSectionCard(onOpenJoinDriverDialog = onOpenJoinDriverDialog)
        }
    }
}

private sealed interface VehicleGridChoice {
    data class Standard(val vehicle: DeliveryVehicleType) : VehicleGridChoice
    data class Custom(val index: Int, val label: String) : VehicleGridChoice
}

@Composable
private fun VehicleSelectionCard(
    title: String,
    hint: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .heightIn(min = 66.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        border = BorderStroke(
            width = if (selected) 1.6.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = hint,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DeliveryRequestCard(
    request: DeliveryRequest,
    isMine: Boolean,
    onOpenMaps: (String) -> Unit,
    onCallCustomer: () -> Unit,
    onMarkCompleted: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    val status = request.parsedStatus
    val (statusBg, statusFg) = when (status) {
        DeliveryStatus.PENDING -> StatusCrowdedBg to StatusCrowdedAmber
        DeliveryStatus.COMPLETED -> StatusAvailableBg to StatusAvailableGreen
        DeliveryStatus.CANCELLED -> StatusCutBg to StatusCutRed
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("delivery_request_card_${request.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        border = BorderStroke(
            width = if (isMine) 1.3.dp else 1.dp,
            color = if (isMine) MaterialTheme.colorScheme.primary.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outline
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = deliveryVehicleIcon(request.parsedVehicleType),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = request.parsedVehicleType.arabicLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        color = statusBg,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = status.arabicLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusFg,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                        )
                    }
                }

                if (isMine) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_delivery_${request.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "حذف الطلب",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = orderTypeIcon(request.parsedOrderType),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${request.parsedOrderType.arabicLabel} • من: ${request.pickupLocation}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = request.orderDetails,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "الوجهة: ${request.parsedDeliveryZone.arabicLabel} — ${request.detailedAddress}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "العميل: ${request.customerName} • ${request.customerPhone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onCallCustomer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("اتصال")
                }

                request.gpsMapsUrl?.takeIf { it.isNotBlank() }?.let { mapsUrl ->
                    FilledTonalButton(
                        onClick = { onOpenMaps(mapsUrl) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("خرائط GPS")
                    }
                }
            }

            if (isMine && status == DeliveryStatus.PENDING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = onMarkCompleted,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("تم التوصيل")
                    }
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.heightIn(min = 48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("إلغاء الطلب")
                    }
                }
            }
        }
    }
}

private fun orderTypeIcon(type: DeliveryOrderType): ImageVector = when (type) {
    DeliveryOrderType.GROCERY -> Icons.Filled.ShoppingBasket
    DeliveryOrderType.RESTAURANT -> Icons.Filled.Restaurant
    DeliveryOrderType.PHARMACY -> Icons.Filled.Medication
    DeliveryOrderType.CARGO_FURNITURE -> Icons.Filled.Inventory2
    DeliveryOrderType.SPECIAL_ITEM -> Icons.Filled.LocalOffer
    DeliveryOrderType.OTHER -> Icons.Filled.LocalShipping
}

@Composable
private fun JoinDeliveryDriverSectionCard(
    onOpenJoinDriverDialog: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .testTag("join_driver_section_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
        ),
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.LocalShipping,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "هل تمتلك مركبة وتبحث عن عمل؟",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "سجّل بياناتك الآن للعمل في خدمة التوصيل والنقل السريع داخل حي الورود ودمشق.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                    )
                }
            }

            Button(
                onClick = onOpenJoinDriverDialog,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("open_join_driver_dialog_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "انضم كسائق توصيل أو نقل",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EstimatedFareCard(
    vehicleLabel: String,
    zoneLabel: String,
    fareEstimate: DeliveryFareEstimate
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("estimated_fare_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DamaskGoldContainer),
        border = BorderStroke(1.5.dp, DamaskGold.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Payments,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "التكلفة التقديرية للتوصيل",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnDamaskGoldContainer
                        )
                        Text(
                            text = "$vehicleLabel • $zoneLabel",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnDamaskGoldContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Text(
                text = fareEstimate.headlinePrice,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("estimated_fare_value")
            )

            fareEstimate.zoneDetails?.let { details ->
                Text(
                    text = details,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnDamaskGoldContainer
                )
            }

            fareEstimate.disclaimerNote?.let { disclaimer ->
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = disclaimer,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = StatusCrowdedAmber,
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("estimated_fare_note")
                    )
                }
            }
        }
    }
}
