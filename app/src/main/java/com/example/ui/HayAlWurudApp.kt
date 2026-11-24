package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.BuildConfig
import com.example.R
import com.example.auth.AuthScreen
import com.example.auth.signOutUser
import com.example.data.firebase.HayAlWurudRepository
import com.example.data.location.LocationHelper
import com.example.data.model.CommunityPost
import com.example.data.model.CraftFilterCategory
import com.example.data.model.DELIVERY_ADMIN_WHATSAPP_PHONE
import com.example.data.model.DeliveryOrderType
import com.example.data.model.DeliveryPrefill
import com.example.data.model.DeliveryRequest
import com.example.data.model.DeliveryStatus
import com.example.data.model.DeliveryVehicleType
import com.example.data.model.DeliveryZone
import com.example.data.model.FavoriteService
import com.example.data.model.HayAlWurudSectors
import com.example.data.model.NeighborhoodStore
import com.example.data.model.PostCategory
import com.example.data.model.ServiceCategory
import com.example.data.model.ServiceProvider
import com.example.data.model.ServiceRecommendation
import com.example.data.model.StoreCategory
import com.example.data.model.StoreProduct
import com.example.data.model.StoreReview
import com.example.data.model.UtilityStatus
import com.example.data.model.UtilityType
import com.example.data.model.UtilityUpdate
import com.example.data.model.buildDriverRegistrationWhatsAppMessage
import com.example.data.model.suggestedDeliveryOrderType
import com.example.data.network.ConnectivityObserver
import com.example.data.update.AppUpdateChecker
import com.example.data.update.AppUpdateInfo
import com.example.ui.components.AddCommunityPostDialog
import com.example.ui.components.AddServiceDialog
import com.example.ui.components.AddStoreProductDialog
import com.example.ui.components.AddUtilityReportDialog
import com.example.ui.components.GuestModeBanner
import com.example.ui.components.InAppModalDialog
import com.example.ui.components.JoinDeliveryDriverDialog
import com.example.ui.components.OfflineStatusBanner
import com.example.ui.components.RateStoreDialog
import com.example.ui.components.RegisterStoreDialog
import com.example.ui.components.openWhatsAppMessage
import com.example.ui.screens.CommunityBoardMode
import com.example.ui.screens.CommunityScreen
import com.example.ui.screens.DeliveryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ServicesScreen
import com.example.ui.screens.StoresScreen
import com.example.ui.state.UiState
import com.example.ui.theme.HayAlWurudTheme
import com.example.ui.viewmodel.GuestNeighborhoodViewModel
import com.example.ui.viewmodel.HayAlWurudViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

enum class NeighborhoodTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    SERVICES("الخدمات", Icons.Filled.Build, Icons.Outlined.Build, "nav_services"),
    STORES("المتاجر", Icons.Filled.Storefront, Icons.Outlined.Storefront, "nav_stores"),
    DELIVERY("التوصيل", Icons.Filled.LocalShipping, Icons.Outlined.LocalShipping, "nav_delivery"),
    SCHEDULES("المواعيد", Icons.Filled.Schedule, Icons.Outlined.Schedule, "nav_schedules"),
    ANNOUNCEMENTS("الإعلانات", Icons.Filled.Campaign, Icons.Outlined.Campaign, "nav_announcements"),
    FAZAA("فزعة الجيران", Icons.Filled.Handshake, Icons.Outlined.Handshake, "nav_fazaa")
}

private fun resolveFirebaseAuth(context: android.content.Context): FirebaseAuth? {
    return try {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        if (FirebaseApp.getApps(context).isNotEmpty()) {
            FirebaseAuth.getInstance()
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}

@Composable
fun HayAlWurudApp(
    updateChecker: suspend () -> AppUpdateInfo? = { AppUpdateChecker.checkForUpdate() },
    gpsLocationFetcher: suspend (android.content.Context) -> Result<String> = { ctx ->
        LocationHelper.fetchCurrentGoogleMapsUrl(ctx)
    },
    onOpenWhatsApp: (android.content.Context, String, String) -> Unit = { ctx, phone, msg ->
        openWhatsAppMessage(ctx, phone, msg)
    }
) {
    HayAlWurudTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val credentialManager = remember(context) { CredentialManager.create(context) }
            val firebaseAuth = remember(context) { resolveFirebaseAuth(context) }

            var currentUser by remember { mutableStateOf<FirebaseUser?>(firebaseAuth?.currentUser) }
            // Allow immediate entry for any user initially; user can sign in with Google anytime.
            var showAuthScreen by rememberSaveable { mutableStateOf(false) }
            var availableUpdate by remember { mutableStateOf<AppUpdateInfo?>(null) }
            var updatePromptDismissed by rememberSaveable { mutableStateOf(false) }

            LaunchedEffect(updateChecker) {
                availableUpdate = updateChecker()
            }

            DisposableEffect(firebaseAuth) {
                if (firebaseAuth == null) {
                    onDispose {}
                } else {
                    val listener = FirebaseAuth.AuthStateListener { auth ->
                        currentUser = auth.currentUser
                        if (auth.currentUser != null) {
                            showAuthScreen = false
                        }
                    }
                    firebaseAuth.addAuthStateListener(listener)
                    onDispose { firebaseAuth.removeAuthStateListener(listener) }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                val signedInUser = currentUser
                if (signedInUser != null) {
                    val uid = signedInUser.uid
                    val displayName = signedInUser.displayName?.takeIf { it.isNotBlank() } ?: "أحد أهالي الحي"
                    val email = signedInUser.email
                    val photoUrl = signedInUser.photoUrl?.toString()

                    key(uid) {
                        val viewModel: HayAlWurudViewModel = viewModel(
                            key = uid,
                            factory = viewModelFactory {
                                initializer {
                                    val app = checkNotNull(this[APPLICATION_KEY]) {
                                        "APPLICATION_KEY missing from CreationExtras"
                                    }
                                    val databaseId = app.getString(R.string.firestore_database_id)
                                    val db = FirebaseFirestore.getInstance(databaseId)
                                    HayAlWurudViewModel(
                                        repository = HayAlWurudRepository(db),
                                        connectivityObserver = ConnectivityObserver(app),
                                        currentUserId = uid,
                                        currentUserName = displayName,
                                        currentUserEmail = email
                                    )
                                }
                            }
                        )

                        AuthenticatedHayAlWurudShell(
                            viewModel = viewModel,
                            displayName = displayName,
                            email = email,
                            photoUrl = photoUrl,
                            gpsLocationFetcher = gpsLocationFetcher,
                            onOpenWhatsApp = onOpenWhatsApp,
                            onSignOut = {
                                signOutUser(credentialManager, scope) {
                                    currentUser = null
                                }
                            }
                        )
                    }
                } else if (showAuthScreen) {
                    AuthScreen(
                        onAuthSuccess = {
                            currentUser = firebaseAuth?.currentUser
                            showAuthScreen = false
                        },
                        onContinueAsGuest = { showAuthScreen = false }
                    )
                } else {
                    val guestViewModel: GuestNeighborhoodViewModel = viewModel(
                        key = "guest_session",
                        factory = viewModelFactory {
                            initializer {
                                val app = checkNotNull(this[APPLICATION_KEY]) {
                                    "APPLICATION_KEY missing from CreationExtras"
                                }
                                GuestNeighborhoodViewModel(ConnectivityObserver(app))
                            }
                        }
                    )
                    GuestHayAlWurudShell(
                        viewModel = guestViewModel,
                        gpsLocationFetcher = gpsLocationFetcher,
                        onOpenWhatsApp = onOpenWhatsApp,
                        onOpenSignIn = { showAuthScreen = true }
                    )
                }

                val pendingUpdate = availableUpdate
                if (pendingUpdate != null && !updatePromptDismissed) {
                    InAppModalDialog(
                        title = "يتوفر تحديث جديد لتطبيق حي الورود",
                        onDismiss = { updatePromptDismissed = true },
                        confirmButton = {
                            androidx.compose.material3.Button(
                                onClick = {
                                    AppUpdateChecker.openUpdateDownloadUrl(context, pendingUpdate.apkUrl)
                                    updatePromptDismissed = true
                                },
                                modifier = Modifier.testTag("update_download_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("تحميل التحديث")
                            }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(
                                onClick = { updatePromptDismissed = true },
                                modifier = Modifier.testTag("update_dismiss_button")
                            ) {
                                Text("لاحقاً")
                            }
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("update_available_dialog"),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SystemUpdate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "الإصدار الجديد: ${pendingUpdate.latestVersion}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "إصدارك الحالي: ${BuildConfig.VERSION_CODE}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "تتوفر نسخة أحدث من تطبيق حي الورود تتضمن تحسينات ومزايا جديدة. اضغط على «تحميل التحديث» لتنزيل الملف وتثبيته.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthenticatedHayAlWurudShell(
    viewModel: HayAlWurudViewModel,
    displayName: String,
    email: String?,
    photoUrl: String?,
    gpsLocationFetcher: suspend (android.content.Context) -> Result<String>,
    onOpenWhatsApp: (android.content.Context, String, String) -> Unit,
    onSignOut: () -> Unit
) {
    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val favoritesState by viewModel.favoritesState.collectAsStateWithLifecycle()
    val utilitiesState by viewModel.utilitiesState.collectAsStateWithLifecycle()
    val servicesState by viewModel.servicesState.collectAsStateWithLifecycle()
    val recommendationsState by viewModel.recommendationsState.collectAsStateWithLifecycle()
    val postsState by viewModel.postsState.collectAsStateWithLifecycle()
    val isMutating by viewModel.isMutating.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val hasPendingWrites by viewModel.hasPendingWrites.collectAsStateWithLifecycle()

    val serviceSearchQuery by viewModel.serviceSearchQuery.collectAsStateWithLifecycle()
    val selectedServiceCategory by viewModel.selectedServiceCategory.collectAsStateWithLifecycle()
    val selectedSectorFilter by viewModel.selectedSectorFilter.collectAsStateWithLifecycle()
    val sortServicesByTrust by viewModel.sortServicesByTrust.collectAsStateWithLifecycle()
    val postSearchQuery by viewModel.postSearchQuery.collectAsStateWithLifecycle()
    val selectedPostCategory by viewModel.selectedPostCategory.collectAsStateWithLifecycle()
    val showResolvedPosts by viewModel.showResolvedPosts.collectAsStateWithLifecycle()
    val storesState by viewModel.storesState.collectAsStateWithLifecycle()
    val storeProductsState by viewModel.storeProductsState.collectAsStateWithLifecycle()
    val storeReviewsState by viewModel.storeReviewsState.collectAsStateWithLifecycle()
    val deliveryRequestsState by viewModel.deliveryRequestsState.collectAsStateWithLifecycle()
    val storeSearchQuery by viewModel.storeSearchQuery.collectAsStateWithLifecycle()
    val selectedStoreCategory by viewModel.selectedStoreCategory.collectAsStateWithLifecycle()
    val showOffersOnly by viewModel.showOffersOnly.collectAsStateWithLifecycle()
    val sortStoresByRating by viewModel.sortStoresByRating.collectAsStateWithLifecycle()

    val preferredSector = (profileState as? UiState.Success)?.data?.preferredSector
        ?: HayAlWurudSectors.ALL.first()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.snackbarMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    NeighborhoodShellContent(
        isGuest = false,
        currentUserId = viewModel.currentUserId,
        displayName = displayName,
        email = email,
        photoUrl = photoUrl,
        preferredSector = preferredSector,
        isOnline = isOnline,
        hasPendingWrites = hasPendingWrites,
        isMutating = isMutating,
        favoritesState = favoritesState,
        utilitiesState = utilitiesState,
        servicesState = servicesState,
        recommendationsState = recommendationsState,
        postsState = postsState,
        storesState = storesState,
        storeProductsState = storeProductsState,
        storeReviewsState = storeReviewsState,
        deliveryRequestsState = deliveryRequestsState,
        gpsLocationFetcher = gpsLocationFetcher,
        onOpenWhatsApp = onOpenWhatsApp,
        serviceSearchQuery = serviceSearchQuery,
        selectedServiceCategory = selectedServiceCategory,
        selectedSectorFilter = selectedSectorFilter,
        sortServicesByTrust = sortServicesByTrust,
        postSearchQuery = postSearchQuery,
        selectedPostCategory = selectedPostCategory,
        showResolvedPosts = showResolvedPosts,
        storeSearchQuery = storeSearchQuery,
        selectedStoreCategory = selectedStoreCategory,
        showOffersOnly = showOffersOnly,
        sortStoresByRating = sortStoresByRating,
        snackbarHostState = snackbarHostState,
        onServiceSearchChange = viewModel::updateServiceSearch,
        onSelectServiceCategory = viewModel::selectServiceCategory,
        onSelectSector = viewModel::selectSectorFilter,
        onToggleSortByTrust = viewModel::toggleSortServicesByTrust,
        onPostSearchChange = viewModel::updatePostSearch,
        onSelectPostCategory = viewModel::selectPostCategory,
        onToggleShowResolved = viewModel::toggleShowResolvedPosts,
        onStoreSearchChange = viewModel::updateStoreSearch,
        onSelectStoreCategory = viewModel::selectStoreCategory,
        onToggleOffersOnly = viewModel::toggleShowOffersOnly,
        onToggleSortStoresByRating = viewModel::toggleSortStoresByRating,
        onToggleFavorite = viewModel::toggleFavorite,
        onToggleRecommend = viewModel::toggleServiceRecommendation,
        onDeleteService = viewModel::deleteServiceProvider,
        onDeleteUtility = viewModel::deleteUtilityUpdate,
        onMarkPostResolved = viewModel::markPostResolved,
        onDeletePost = viewModel::deleteCommunityPost,
        onUpdatePreferredSector = viewModel::updatePreferredSector,
        onSubmitUtility = viewModel::submitUtilityUpdate,
        onSubmitService = viewModel::submitServiceProvider,
        onSubmitPost = viewModel::submitCommunityPost,
        onRegisterStore = viewModel::registerStore,
        onToggleStoreOpen = viewModel::toggleStoreOpen,
        onDeleteStore = viewModel::deleteStore,
        onAddStoreProduct = viewModel::addStoreProduct,
        onToggleProductOfferAndStock = viewModel::toggleProductOfferAndStock,
        onDeleteStoreProduct = viewModel::deleteStoreProduct,
        onSubmitStoreReview = viewModel::submitStoreReview,
        onDeleteStoreReview = viewModel::deleteStoreReview,
        onSubmitDeliveryRequest = viewModel::submitDeliveryRequest,
        onUpdateDeliveryStatus = viewModel::updateDeliveryStatus,
        onDeleteDeliveryRequest = viewModel::deleteDeliveryRequest,
        onLoadStarterData = viewModel::loadStarterNeighborhoodData,
        onSignInClick = {},
        onSignOut = onSignOut
    )
}

@Composable
private fun GuestHayAlWurudShell(
    viewModel: GuestNeighborhoodViewModel,
    gpsLocationFetcher: suspend (android.content.Context) -> Result<String>,
    onOpenWhatsApp: (android.content.Context, String, String) -> Unit,
    onOpenSignIn: () -> Unit
) {
    val preferredSector by viewModel.preferredSector.collectAsStateWithLifecycle()
    val favoritesState by viewModel.favoritesState.collectAsStateWithLifecycle()
    val utilitiesState by viewModel.utilitiesState.collectAsStateWithLifecycle()
    val servicesState by viewModel.servicesState.collectAsStateWithLifecycle()
    val recommendationsState by viewModel.recommendationsState.collectAsStateWithLifecycle()
    val postsState by viewModel.postsState.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    val serviceSearchQuery by viewModel.serviceSearchQuery.collectAsStateWithLifecycle()
    val selectedServiceCategory by viewModel.selectedServiceCategory.collectAsStateWithLifecycle()
    val selectedSectorFilter by viewModel.selectedSectorFilter.collectAsStateWithLifecycle()
    val sortServicesByTrust by viewModel.sortServicesByTrust.collectAsStateWithLifecycle()
    val postSearchQuery by viewModel.postSearchQuery.collectAsStateWithLifecycle()
    val selectedPostCategory by viewModel.selectedPostCategory.collectAsStateWithLifecycle()
    val showResolvedPosts by viewModel.showResolvedPosts.collectAsStateWithLifecycle()
    val storesState by viewModel.storesState.collectAsStateWithLifecycle()
    val storeProductsState by viewModel.storeProductsState.collectAsStateWithLifecycle()
    val storeReviewsState by viewModel.storeReviewsState.collectAsStateWithLifecycle()
    val deliveryRequestsState by viewModel.deliveryRequestsState.collectAsStateWithLifecycle()
    val storeSearchQuery by viewModel.storeSearchQuery.collectAsStateWithLifecycle()
    val selectedStoreCategory by viewModel.selectedStoreCategory.collectAsStateWithLifecycle()
    val showOffersOnly by viewModel.showOffersOnly.collectAsStateWithLifecycle()
    val sortStoresByRating by viewModel.sortStoresByRating.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.snackbarMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    NeighborhoodShellContent(
        isGuest = true,
        currentUserId = viewModel.guestUserId,
        displayName = viewModel.guestUserName,
        email = "تصفح محلي مفتوح",
        photoUrl = null,
        preferredSector = preferredSector,
        isOnline = isOnline,
        hasPendingWrites = false,
        isMutating = false,
        favoritesState = favoritesState,
        utilitiesState = utilitiesState,
        servicesState = servicesState,
        recommendationsState = recommendationsState,
        postsState = postsState,
        storesState = storesState,
        storeProductsState = storeProductsState,
        storeReviewsState = storeReviewsState,
        deliveryRequestsState = deliveryRequestsState,
        gpsLocationFetcher = gpsLocationFetcher,
        onOpenWhatsApp = onOpenWhatsApp,
        serviceSearchQuery = serviceSearchQuery,
        selectedServiceCategory = selectedServiceCategory,
        selectedSectorFilter = selectedSectorFilter,
        sortServicesByTrust = sortServicesByTrust,
        postSearchQuery = postSearchQuery,
        selectedPostCategory = selectedPostCategory,
        showResolvedPosts = showResolvedPosts,
        storeSearchQuery = storeSearchQuery,
        selectedStoreCategory = selectedStoreCategory,
        showOffersOnly = showOffersOnly,
        sortStoresByRating = sortStoresByRating,
        snackbarHostState = snackbarHostState,
        onServiceSearchChange = viewModel::updateServiceSearch,
        onSelectServiceCategory = viewModel::selectServiceCategory,
        onSelectSector = viewModel::selectSectorFilter,
        onToggleSortByTrust = viewModel::toggleSortServicesByTrust,
        onPostSearchChange = viewModel::updatePostSearch,
        onSelectPostCategory = viewModel::selectPostCategory,
        onToggleShowResolved = viewModel::toggleShowResolvedPosts,
        onStoreSearchChange = viewModel::updateStoreSearch,
        onSelectStoreCategory = viewModel::selectStoreCategory,
        onToggleOffersOnly = viewModel::toggleShowOffersOnly,
        onToggleSortStoresByRating = viewModel::toggleSortStoresByRating,
        onToggleFavorite = viewModel::toggleFavorite,
        onToggleRecommend = viewModel::toggleServiceRecommendation,
        onDeleteService = viewModel::deleteServiceProvider,
        onDeleteUtility = viewModel::deleteUtilityUpdate,
        onMarkPostResolved = viewModel::markPostResolved,
        onDeletePost = viewModel::deleteCommunityPost,
        onUpdatePreferredSector = viewModel::updatePreferredSector,
        onSubmitUtility = viewModel::submitUtilityUpdate,
        onSubmitService = viewModel::submitServiceProvider,
        onSubmitPost = viewModel::submitCommunityPost,
        onRegisterStore = viewModel::registerStore,
        onToggleStoreOpen = viewModel::toggleStoreOpen,
        onDeleteStore = viewModel::deleteStore,
        onAddStoreProduct = viewModel::addStoreProduct,
        onToggleProductOfferAndStock = viewModel::toggleProductOfferAndStock,
        onDeleteStoreProduct = viewModel::deleteStoreProduct,
        onSubmitStoreReview = viewModel::submitStoreReview,
        onDeleteStoreReview = viewModel::deleteStoreReview,
        onSubmitDeliveryRequest = viewModel::submitDeliveryRequest,
        onUpdateDeliveryStatus = viewModel::updateDeliveryStatus,
        onDeleteDeliveryRequest = viewModel::deleteDeliveryRequest,
        onLoadStarterData = {},
        onSignInClick = onOpenSignIn,
        onSignOut = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NeighborhoodShellContent(
    isGuest: Boolean,
    currentUserId: String,
    displayName: String,
    email: String?,
    photoUrl: String?,
    preferredSector: String,
    isOnline: Boolean,
    hasPendingWrites: Boolean,
    isMutating: Boolean,
    favoritesState: UiState<List<FavoriteService>>,
    utilitiesState: UiState<List<UtilityUpdate>>,
    servicesState: UiState<List<ServiceProvider>>,
    recommendationsState: UiState<List<ServiceRecommendation>>,
    postsState: UiState<List<CommunityPost>>,
    storesState: UiState<List<NeighborhoodStore>>,
    storeProductsState: UiState<List<StoreProduct>>,
    storeReviewsState: UiState<List<StoreReview>>,
    deliveryRequestsState: UiState<List<DeliveryRequest>>,
    gpsLocationFetcher: suspend (android.content.Context) -> Result<String>,
    onOpenWhatsApp: (android.content.Context, String, String) -> Unit,
    serviceSearchQuery: String,
    selectedServiceCategory: CraftFilterCategory,
    selectedSectorFilter: String?,
    sortServicesByTrust: Boolean,
    postSearchQuery: String,
    selectedPostCategory: PostCategory?,
    showResolvedPosts: Boolean,
    storeSearchQuery: String,
    selectedStoreCategory: StoreCategory?,
    showOffersOnly: Boolean,
    sortStoresByRating: Boolean,
    snackbarHostState: SnackbarHostState,
    onServiceSearchChange: (String) -> Unit,
    onSelectServiceCategory: (CraftFilterCategory) -> Unit,
    onSelectSector: (String?) -> Unit,
    onToggleSortByTrust: () -> Unit,
    onPostSearchChange: (String) -> Unit,
    onSelectPostCategory: (PostCategory?) -> Unit,
    onToggleShowResolved: () -> Unit,
    onStoreSearchChange: (String) -> Unit,
    onSelectStoreCategory: (StoreCategory?) -> Unit,
    onToggleOffersOnly: () -> Unit,
    onToggleSortStoresByRating: () -> Unit,
    onToggleFavorite: (ServiceProvider, Boolean) -> Unit,
    onToggleRecommend: (ServiceProvider, Boolean) -> Unit,
    onDeleteService: (String) -> Unit,
    onDeleteUtility: (String) -> Unit,
    onMarkPostResolved: (String) -> Unit,
    onDeletePost: (String) -> Unit,
    onUpdatePreferredSector: (String) -> Unit,
    onSubmitUtility: (UtilityType, String, UtilityStatus, String?) -> Unit,
    onSubmitService: (String, ServiceCategory, String, String, String, String?, String?) -> Unit,
    onSubmitPost: (String, String, PostCategory, String, String?, String?) -> Unit,
    onRegisterStore: (String, StoreCategory, String, String, String?, String?, String?) -> Unit,
    onToggleStoreOpen: (String, Boolean) -> Unit,
    onDeleteStore: (String) -> Unit,
    onAddStoreProduct: (NeighborhoodStore, String, String, String?, Boolean, String?, String?) -> Unit,
    onToggleProductOfferAndStock: (String, Boolean, Boolean) -> Unit,
    onDeleteStoreProduct: (String) -> Unit,
    onSubmitStoreReview: (NeighborhoodStore, Int, Int, String?) -> Unit,
    onDeleteStoreReview: (String) -> Unit,
    onSubmitDeliveryRequest: (
        DeliveryOrderType,
        DeliveryVehicleType,
        String,
        String,
        DeliveryZone,
        String,
        String?,
        String,
        String
    ) -> Unit,
    onUpdateDeliveryStatus: (String, DeliveryStatus) -> Unit,
    onDeleteDeliveryRequest: (String) -> Unit,
    onLoadStarterData: () -> Unit,
    onSignInClick: () -> Unit,
    onSignOut: () -> Unit
) {
    var activeTab by rememberSaveable { mutableStateOf(NeighborhoodTab.SERVICES) }
    var showProfile by rememberSaveable { mutableStateOf(false) }
    var showAddUtilityDialog by remember { mutableStateOf(false) }
    var showAddServiceDialog by remember { mutableStateOf(false) }
    var showAddEntryChooserDialog by remember { mutableStateOf(false) }
    var showAddPostDialog by remember { mutableStateOf(false) }
    var showRegisterStoreDialog by remember { mutableStateOf(false) }
    var productTargetStore by remember { mutableStateOf<NeighborhoodStore?>(null) }
    var rateTargetStorePair by remember { mutableStateOf<Pair<NeighborhoodStore, StoreReview?>?>(null) }
    var pendingDeliveryPrefill by remember { mutableStateOf<DeliveryPrefill?>(null) }
    var showJoinDriverDialog by remember { mutableStateOf(false) }
    var addPostDefaultCategory by remember { mutableStateOf(PostCategory.NOTICE) }

    BackHandler(enabled = showProfile || activeTab != NeighborhoodTab.SERVICES) {
        if (showProfile) {
            showProfile = false
        } else {
            activeTab = NeighborhoodTab.SERVICES
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets.navigationBars,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (showProfile) "ملفي ومحفوظاتي" else "حي الورود • دمشق",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        if (showProfile) {
                            IconButton(
                                onClick = { showProfile = false },
                                modifier = Modifier.testTag("profile_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "رجوع"
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showProfile = !showProfile },
                            modifier = Modifier.testTag("top_bar_profile_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "ملفي ومحفوظاتي",
                                tint = if (showProfile) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        NeighborhoodTab.entries.forEach { tab ->
                            val selected = !showProfile && activeTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    showProfile = false
                                    activeTab = tab
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight(),
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        NeighborhoodTab.entries.forEach { tab ->
                            val selected = !showProfile && activeTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    showProfile = false
                                    activeTab = tab
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (isGuest) {
                            GuestModeBanner(onSignInClick = onSignInClick)
                        }
                        OfflineStatusBanner(
                            isOnline = isOnline,
                            hasPendingWrites = hasPendingWrites,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Box(modifier = Modifier.widthIn(max = 760.dp).fillMaxSize()) {
                                if (showProfile) {
                                    ProfileScreen(
                                        displayName = displayName,
                                        email = email,
                                        photoUrl = photoUrl,
                                        preferredSector = preferredSector,
                                        currentUserId = currentUserId,
                                        isGuest = isGuest,
                                        favoritesState = favoritesState,
                                        servicesState = servicesState,
                                        postsState = postsState,
                                        onUpdateSector = onUpdatePreferredSector,
                                        onRemoveFavorite = { fav ->
                                            onToggleFavorite(
                                                ServiceProvider(
                                                    id = fav.serviceId,
                                                    name = fav.serviceName,
                                                    specialty = fav.specialty,
                                                    workPhone = fav.workPhone,
                                                    sector = fav.sector
                                                ),
                                                true
                                            )
                                        },
                                        onSignInClick = onSignInClick,
                                        onSignOut = onSignOut
                                    )
                                } else {
                                    when (activeTab) {
                                        NeighborhoodTab.SERVICES -> ServicesScreen(
                                            currentUserId = currentUserId,
                                            servicesState = servicesState,
                                            favoritesState = favoritesState,
                                            recommendationsState = recommendationsState,
                                            searchQuery = serviceSearchQuery,
                                            selectedCategory = selectedServiceCategory,
                                            selectedSector = selectedSectorFilter,
                                            sortByTrust = sortServicesByTrust,
                                            isMutating = isMutating,
                                            onSearchQueryChange = onServiceSearchChange,
                                            onSelectCategory = onSelectServiceCategory,
                                            onSelectSector = onSelectSector,
                                            onToggleSortByTrust = onToggleSortByTrust,
                                            onToggleFavorite = onToggleFavorite,
                                            onToggleRecommend = onToggleRecommend,
                                            onDeleteService = onDeleteService,
                                            onOpenAddService = { showAddEntryChooserDialog = true },
                                            onOrderDeliveryFromService = { service ->
                                                pendingDeliveryPrefill = DeliveryPrefill(
                                                    pickupLocation = service.name,
                                                    orderType = service.suggestedDeliveryOrderType()
                                                )
                                                showProfile = false
                                                activeTab = NeighborhoodTab.DELIVERY
                                            },
                                            onLoadStarterData = onLoadStarterData
                                        )

                                        NeighborhoodTab.STORES -> StoresScreen(
                                            currentUserId = currentUserId,
                                            storesState = storesState,
                                            storeProductsState = storeProductsState,
                                            storeReviewsState = storeReviewsState,
                                            searchQuery = storeSearchQuery,
                                            selectedCategory = selectedStoreCategory,
                                            selectedSector = selectedSectorFilter,
                                            showOffersOnly = showOffersOnly,
                                            sortByRating = sortStoresByRating,
                                            onSearchQueryChange = onStoreSearchChange,
                                            onSelectCategory = onSelectStoreCategory,
                                            onSelectSector = onSelectSector,
                                            onToggleOffersOnly = onToggleOffersOnly,
                                            onToggleSortByRating = onToggleSortStoresByRating,
                                            onOpenRegisterStore = { showRegisterStoreDialog = true },
                                            onOpenAddProduct = { store -> productTargetStore = store },
                                            onOpenRateStore = { store, existingReview ->
                                                rateTargetStorePair = store to existingReview
                                            },
                                            onOrderDeliveryFromStore = { store ->
                                                pendingDeliveryPrefill = DeliveryPrefill(
                                                    pickupLocation = store.name,
                                                    orderType = store.suggestedDeliveryOrderType()
                                                )
                                                showProfile = false
                                                activeTab = NeighborhoodTab.DELIVERY
                                            },
                                            onDeleteStoreReview = onDeleteStoreReview,
                                            onToggleStoreOpen = onToggleStoreOpen,
                                            onDeleteStore = onDeleteStore,
                                            onToggleProductOfferAndStock = onToggleProductOfferAndStock,
                                            onDeleteProduct = onDeleteStoreProduct
                                        )

                                        NeighborhoodTab.DELIVERY -> DeliveryScreen(
                                            currentUserId = currentUserId,
                                            defaultCustomerName = if (isGuest) "" else displayName,
                                            deliveryRequestsState = deliveryRequestsState,
                                            prefill = pendingDeliveryPrefill,
                                            isMutating = isMutating,
                                            onPrefillConsumed = { pendingDeliveryPrefill = null },
                                            onSubmitDeliveryRequest = onSubmitDeliveryRequest,
                                            onUpdateDeliveryStatus = onUpdateDeliveryStatus,
                                            onDeleteDeliveryRequest = onDeleteDeliveryRequest,
                                            gpsLocationFetcher = gpsLocationFetcher,
                                            onOpenWhatsApp = onOpenWhatsApp,
                                            onOpenJoinDriverDialog = { showJoinDriverDialog = true }
                                        )

                                        NeighborhoodTab.SCHEDULES -> HomeScreen(
                                            residentName = displayName,
                                            preferredSector = preferredSector,
                                            currentUserId = currentUserId,
                                            utilitiesState = utilitiesState,
                                            servicesState = servicesState,
                                            postsState = postsState,
                                            isMutating = isMutating,
                                            onOpenAddUtility = { showAddUtilityDialog = true },
                                            onOpenAddPost = {
                                                addPostDefaultCategory = PostCategory.NOTICE
                                                showAddPostDialog = true
                                            },
                                            onNavigateToServices = { activeTab = NeighborhoodTab.SERVICES },
                                            onNavigateToCommunity = { activeTab = NeighborhoodTab.ANNOUNCEMENTS },
                                            onDeleteUtilityReport = onDeleteUtility,
                                            onLoadStarterData = onLoadStarterData
                                        )

                                        NeighborhoodTab.ANNOUNCEMENTS -> CommunityScreen(
                                            currentUserId = currentUserId,
                                            postsState = postsState,
                                            searchQuery = postSearchQuery,
                                            selectedCategory = selectedPostCategory,
                                            selectedSector = selectedSectorFilter,
                                            showResolvedPosts = showResolvedPosts,
                                            onSearchQueryChange = onPostSearchChange,
                                            onSelectCategory = onSelectPostCategory,
                                            onSelectSector = onSelectSector,
                                            onToggleShowResolved = onToggleShowResolved,
                                            onMarkResolved = onMarkPostResolved,
                                            onDeletePost = onDeletePost,
                                            onOpenAddPost = {
                                                addPostDefaultCategory = PostCategory.NOTICE
                                                showAddPostDialog = true
                                            },
                                            boardMode = CommunityBoardMode.ANNOUNCEMENTS
                                        )

                                        NeighborhoodTab.FAZAA -> CommunityScreen(
                                            currentUserId = currentUserId,
                                            postsState = postsState,
                                            searchQuery = postSearchQuery,
                                            selectedCategory = selectedPostCategory,
                                            selectedSector = selectedSectorFilter,
                                            showResolvedPosts = showResolvedPosts,
                                            onSearchQueryChange = onPostSearchChange,
                                            onSelectCategory = onSelectPostCategory,
                                            onSelectSector = onSelectSector,
                                            onToggleShowResolved = onToggleShowResolved,
                                            onMarkResolved = onMarkPostResolved,
                                            onDeletePost = onDeletePost,
                                            onOpenAddPost = {
                                                addPostDefaultCategory = PostCategory.HELP
                                                showAddPostDialog = true
                                            },
                                            boardMode = CommunityBoardMode.FAZAA
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

    if (showAddUtilityDialog) {
        AddUtilityReportDialog(
            defaultSector = preferredSector,
            onDismiss = { showAddUtilityDialog = false },
            onSubmit = onSubmitUtility
        )
    }

    if (showAddServiceDialog) {
        AddServiceDialog(
            defaultSector = preferredSector,
            onDismiss = { showAddServiceDialog = false },
            onSubmit = onSubmitService
        )
    }

    if (showAddPostDialog) {
        AddCommunityPostDialog(
            defaultSector = preferredSector,
            defaultCategory = addPostDefaultCategory,
            onDismiss = { showAddPostDialog = false },
            onSubmit = onSubmitPost
        )
    }

    if (showAddEntryChooserDialog) {
        InAppModalDialog(
            title = "أضف محلك وعروضك",
            onDismiss = { showAddEntryChooserDialog = false },
            confirmButton = {},
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showAddEntryChooserDialog = false }
                ) {
                    Text("إلغاء")
                }
            }
        ) {
            Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "اختر نوع الإضافة التي تريد تسجيلها في دليل حي الورود:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                androidx.compose.material3.Button(
                    onClick = {
                        showAddEntryChooserDialog = false
                        showRegisterStoreDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("choose_register_store_button")
                ) {
                    Text("تسجيل محلك وعروض البضائع")
                }
                androidx.compose.material3.OutlinedButton(
                    onClick = {
                        showAddEntryChooserDialog = false
                        showAddServiceDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("choose_add_craftsman_button")
                ) {
                    Text("إضافة حرفي أو خدمة صيانة")
                }
            }
        }
    }

    if (showRegisterStoreDialog) {
        RegisterStoreDialog(
            defaultSector = preferredSector,
            onDismiss = { showRegisterStoreDialog = false },
            onSubmit = { name, category, sector, phone, whatsappPhone, workingHours, description ->
                onRegisterStore(name, category, sector, phone, whatsappPhone, workingHours, description)
                activeTab = NeighborhoodTab.STORES
            }
        )
    }

    productTargetStore?.let { targetStore ->
        AddStoreProductDialog(
            store = targetStore,
            onDismiss = { productTargetStore = null },
            onSubmit = { title, priceText, originalPriceText, onOffer, specsText, description ->
                onAddStoreProduct(
                    targetStore,
                    title,
                    priceText,
                    originalPriceText,
                    onOffer,
                    specsText,
                    description
                )
            }
        )
    }

    rateTargetStorePair?.let { (targetStore, existingReview) ->
        RateStoreDialog(
            store = targetStore,
            existingReview = existingReview,
            onDismiss = { rateTargetStorePair = null },
            onSubmit = { quality, credibility, comment ->
                onSubmitStoreReview(targetStore, quality, credibility, comment)
            }
        )
    }

    if (showJoinDriverDialog) {
        val dialogContext = LocalContext.current
        JoinDeliveryDriverDialog(
            onDismiss = { showJoinDriverDialog = false },
            onSubmitDriver = { fullName, phones, vehicleTypeLabel ->
                val message = buildDriverRegistrationWhatsAppMessage(
                    fullName = fullName,
                    phones = phones,
                    vehicleTypeLabel = vehicleTypeLabel
                )
                onOpenWhatsApp(dialogContext, DELIVERY_ADMIN_WHATSAPP_PHONE, message)
            }
        )
    }
    }
}
