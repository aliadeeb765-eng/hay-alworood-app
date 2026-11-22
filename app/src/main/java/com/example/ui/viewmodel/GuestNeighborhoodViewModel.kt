package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.HayAlWurudStarterCatalog
import com.example.data.model.CommunityPost
import com.example.data.model.CraftFilterCategory
import com.example.data.model.FavoriteService
import com.example.data.model.HayAlWurudSectors
import com.example.data.model.NeighborhoodStore
import com.example.data.model.PostCategory
import com.example.data.model.PostStatus
import com.example.data.model.ServiceCategory
import com.example.data.model.ServiceProvider
import com.example.data.model.ServiceRecommendation
import com.example.data.model.StoreCategory
import com.example.data.model.StoreProduct
import com.example.data.model.UtilityStatus
import com.example.data.model.UtilityType
import com.example.data.model.UtilityUpdate
import com.example.data.network.ConnectivityObserver
import com.example.ui.state.UiState
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import java.util.UUID

class GuestNeighborhoodViewModel(
    connectivityObserver: ConnectivityObserver
) : ViewModel() {

    val guestUserId = "guest_resident"
    val guestUserName = "زائر حي الورود"

    val isOnline: StateFlow<Boolean> = connectivityObserver.observeIsOnline()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = connectivityObserver.isCurrentlyOnline()
        )

    private val _preferredSector = MutableStateFlow(HayAlWurudSectors.ALL.first())
    val preferredSector: StateFlow<String> = _preferredSector.asStateFlow()

    private val _utilitiesState = MutableStateFlow<UiState<List<UtilityUpdate>>>(
        UiState.Success(HayAlWurudStarterCatalog.starterUtilities(guestUserId, "أهل حي الورود"))
    )
    val utilitiesState: StateFlow<UiState<List<UtilityUpdate>>> = _utilitiesState.asStateFlow()

    private val _servicesState = MutableStateFlow<UiState<List<ServiceProvider>>>(
        UiState.Success(HayAlWurudStarterCatalog.starterServices(guestUserId, "أهل حي الورود"))
    )
    val servicesState: StateFlow<UiState<List<ServiceProvider>>> = _servicesState.asStateFlow()

    private val _recommendationsState = MutableStateFlow<UiState<List<ServiceRecommendation>>>(
        UiState.Success(HayAlWurudStarterCatalog.starterRecommendations())
    )
    val recommendationsState: StateFlow<UiState<List<ServiceRecommendation>>> = _recommendationsState.asStateFlow()

    private val _postsState = MutableStateFlow<UiState<List<CommunityPost>>>(
        UiState.Success(HayAlWurudStarterCatalog.starterPosts(guestUserId, "أهل حي الورود"))
    )
    val postsState: StateFlow<UiState<List<CommunityPost>>> = _postsState.asStateFlow()

    private val _favoritesState = MutableStateFlow<UiState<List<FavoriteService>>>(
        UiState.Success(emptyList())
    )
    val favoritesState: StateFlow<UiState<List<FavoriteService>>> = _favoritesState.asStateFlow()

    private val _serviceSearchQuery = MutableStateFlow("")
    val serviceSearchQuery: StateFlow<String> = _serviceSearchQuery.asStateFlow()

    private val _selectedServiceCategory = MutableStateFlow(CraftFilterCategory.ALL)
    val selectedServiceCategory: StateFlow<CraftFilterCategory> = _selectedServiceCategory.asStateFlow()

    private val _selectedSectorFilter = MutableStateFlow<String?>(null)
    val selectedSectorFilter: StateFlow<String?> = _selectedSectorFilter.asStateFlow()

    private val _sortServicesByTrust = MutableStateFlow(true)
    val sortServicesByTrust: StateFlow<Boolean> = _sortServicesByTrust.asStateFlow()

    private val _postSearchQuery = MutableStateFlow("")
    val postSearchQuery: StateFlow<String> = _postSearchQuery.asStateFlow()

    private val _selectedPostCategory = MutableStateFlow<PostCategory?>(null)
    val selectedPostCategory: StateFlow<PostCategory?> = _selectedPostCategory.asStateFlow()

    private val _showResolvedPosts = MutableStateFlow(true)
    val showResolvedPosts: StateFlow<Boolean> = _showResolvedPosts.asStateFlow()

    private val _storesState = MutableStateFlow<UiState<List<NeighborhoodStore>>>(
        UiState.Success(HayAlWurudStarterCatalog.starterStores(guestUserId, "أهل حي الورود"))
    )
    val storesState: StateFlow<UiState<List<NeighborhoodStore>>> = _storesState.asStateFlow()

    private val _storeProductsState = MutableStateFlow<UiState<List<StoreProduct>>>(
        UiState.Success(HayAlWurudStarterCatalog.starterStoreProducts(guestUserId))
    )
    val storeProductsState: StateFlow<UiState<List<StoreProduct>>> = _storeProductsState.asStateFlow()

    private val _storeSearchQuery = MutableStateFlow("")
    val storeSearchQuery: StateFlow<String> = _storeSearchQuery.asStateFlow()

    private val _selectedStoreCategory = MutableStateFlow<StoreCategory?>(null)
    val selectedStoreCategory: StateFlow<StoreCategory?> = _selectedStoreCategory.asStateFlow()

    private val _showOffersOnly = MutableStateFlow(false)
    val showOffersOnly: StateFlow<Boolean> = _showOffersOnly.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    fun updateServiceSearch(query: String) {
        _serviceSearchQuery.value = query
    }

    fun selectServiceCategory(category: CraftFilterCategory) {
        _selectedServiceCategory.value = category
    }

    fun selectSectorFilter(sector: String?) {
        _selectedSectorFilter.value = sector
    }

    fun toggleSortServicesByTrust() {
        _sortServicesByTrust.value = !_sortServicesByTrust.value
    }

    fun updatePostSearch(query: String) {
        _postSearchQuery.value = query
    }

    fun selectPostCategory(category: PostCategory?) {
        _selectedPostCategory.value = category
    }

    fun toggleShowResolvedPosts() {
        _showResolvedPosts.value = !_showResolvedPosts.value
    }

    fun updatePreferredSector(sector: String) {
        _preferredSector.value = sector
        _snackbarMessage.tryEmit("تم تحديث حارتك إلى: $sector")
    }

    fun toggleFavorite(service: ServiceProvider, currentlyFavorited: Boolean) {
        val current = (_favoritesState.value as? UiState.Success)?.data.orEmpty()
        val updated = if (currentlyFavorited) {
            current.filterNot { it.serviceId == service.id }
        } else {
            listOf(
                FavoriteService(
                    id = service.id,
                    userId = guestUserId,
                    serviceId = service.id,
                    serviceName = service.name,
                    specialty = service.specialty,
                    workPhone = service.workPhone,
                    sector = service.sector,
                    createdAt = Timestamp.now()
                )
            ) + current
        }
        _favoritesState.value = UiState.Success(updated)
        _snackbarMessage.tryEmit(
            if (currentlyFavorited) "تمت الإزالة من المحفوظات" else "تم حفظ ${service.name} محلياً"
        )
    }

    fun toggleServiceRecommendation(service: ServiceProvider, currentlyRecommended: Boolean) {
        val current = (_recommendationsState.value as? UiState.Success)?.data.orEmpty()
        val updated = if (currentlyRecommended) {
            current.filterNot { it.serviceId == service.id && it.userId == guestUserId }
        } else {
            current + ServiceRecommendation(
                id = "${service.id}_$guestUserId",
                userId = guestUserId,
                serviceId = service.id,
                authorName = guestUserName,
                createdAt = Timestamp.now()
            )
        }
        _recommendationsState.value = UiState.Success(updated)
        _snackbarMessage.tryEmit(
            if (currentlyRecommended) "تمت إزالة توصيتك لـ ${service.name}"
            else "تمت إضافة توصيتك لـ ${service.name}"
        )
    }

    fun submitUtilityUpdate(
        utilityType: UtilityType,
        sector: String,
        status: UtilityStatus,
        note: String?
    ) {
        val current = (_utilitiesState.value as? UiState.Success)?.data.orEmpty()
        val newUpdate = UtilityUpdate(
            id = "guest_u_${UUID.randomUUID()}",
            userId = guestUserId,
            authorName = guestUserName,
            utilityType = utilityType.name,
            sector = sector,
            status = status.name,
            note = note?.trim()?.takeIf { it.isNotBlank() },
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )
        _utilitiesState.value = UiState.Success(listOf(newUpdate) + current)
        _snackbarMessage.tryEmit("تم تحديث حالة ${utilityType.arabicName} (وضع المعاينة المحلي)")
    }

    fun deleteUtilityUpdate(reportId: String) {
        val current = (_utilitiesState.value as? UiState.Success)?.data.orEmpty()
        _utilitiesState.value = UiState.Success(current.filterNot { it.id == reportId })
        _snackbarMessage.tryEmit("تم حذف البلاغ")
    }

    fun submitServiceProvider(
        name: String,
        category: ServiceCategory,
        specialty: String,
        sector: String,
        workPhone: String,
        workingHours: String?,
        notes: String?
    ) {
        val current = (_servicesState.value as? UiState.Success)?.data.orEmpty()
        val newService = ServiceProvider(
            id = "guest_s_${UUID.randomUUID()}",
            userId = guestUserId,
            authorName = guestUserName,
            name = name.trim(),
            category = category.name,
            specialty = specialty.trim(),
            sector = sector,
            workPhone = workPhone.trim(),
            workingHours = workingHours?.trim()?.takeIf { it.isNotBlank() },
            notes = notes?.trim()?.takeIf { it.isNotBlank() },
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )
        _servicesState.value = UiState.Success(listOf(newService) + current)
        _snackbarMessage.tryEmit("تمت إضافة الخدمة (وضع المعاينة المحلي)")
    }

    fun deleteServiceProvider(serviceId: String) {
        val current = (_servicesState.value as? UiState.Success)?.data.orEmpty()
        _servicesState.value = UiState.Success(current.filterNot { it.id == serviceId })
        _snackbarMessage.tryEmit("تم حذف الخدمة")
    }

    fun submitCommunityPost(
        title: String,
        description: String,
        category: PostCategory,
        sector: String,
        priceText: String?,
        contactNote: String?
    ) {
        val current = (_postsState.value as? UiState.Success)?.data.orEmpty()
        val newPost = CommunityPost(
            id = "guest_p_${UUID.randomUUID()}",
            userId = guestUserId,
            authorName = guestUserName,
            title = title.trim(),
            description = description.trim(),
            category = category.name,
            sector = sector,
            priceText = priceText?.trim()?.takeIf { it.isNotBlank() },
            contactNote = contactNote?.trim()?.takeIf { it.isNotBlank() },
            status = PostStatus.OPEN.name,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )
        _postsState.value = UiState.Success(listOf(newPost) + current)
        _snackbarMessage.tryEmit("تم نشر الإعلان (وضع المعاينة المحلي)")
    }

    fun markPostResolved(postId: String) {
        val current = (_postsState.value as? UiState.Success)?.data.orEmpty()
        _postsState.value = UiState.Success(
            current.map { post ->
                if (post.id == postId) post.copy(status = PostStatus.RESOLVED.name, updatedAt = Timestamp.now())
                else post
            }
        )
        _snackbarMessage.tryEmit("تم إغلاق المنشور بنجاح")
    }

    fun deleteCommunityPost(postId: String) {
        val current = (_postsState.value as? UiState.Success)?.data.orEmpty()
        _postsState.value = UiState.Success(current.filterNot { it.id == postId })
        _snackbarMessage.tryEmit("تم حذف المنشور")
    }

    fun updateStoreSearch(query: String) {
        _storeSearchQuery.value = query
    }

    fun selectStoreCategory(category: StoreCategory?) {
        _selectedStoreCategory.value = category
    }

    fun toggleShowOffersOnly() {
        _showOffersOnly.value = !_showOffersOnly.value
    }

    fun registerStore(
        name: String,
        category: StoreCategory,
        sector: String,
        phone: String,
        whatsappPhone: String?,
        workingHours: String?,
        description: String?
    ) {
        val current = (_storesState.value as? UiState.Success)?.data.orEmpty()
        val newStore = NeighborhoodStore(
            id = "guest_store_${UUID.randomUUID()}",
            userId = guestUserId,
            ownerName = guestUserName,
            name = name.trim(),
            category = category.name,
            sector = sector,
            phone = phone.trim(),
            whatsappPhone = whatsappPhone?.trim()?.takeIf { it.isNotBlank() },
            workingHours = workingHours?.trim()?.takeIf { it.isNotBlank() },
            description = description?.trim()?.takeIf { it.isNotBlank() },
            storeOpen = true,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )
        _storesState.value = UiState.Success(listOf(newStore) + current)
        _snackbarMessage.tryEmit("تم تسجيل ${newStore.name} (وضع المعاينة المحلي)")
    }

    fun toggleStoreOpen(storeId: String, nextOpen: Boolean) {
        val current = (_storesState.value as? UiState.Success)?.data.orEmpty()
        _storesState.value = UiState.Success(
            current.map { store ->
                if (store.id == storeId) store.copy(storeOpen = nextOpen, updatedAt = Timestamp.now())
                else store
            }
        )
        _snackbarMessage.tryEmit(if (nextOpen) "تم فتح المحل" else "تم تعيين المحل كمغلق مؤقتاً")
    }

    fun deleteStore(storeId: String) {
        val currentStores = (_storesState.value as? UiState.Success)?.data.orEmpty()
        val currentProducts = (_storeProductsState.value as? UiState.Success)?.data.orEmpty()
        _storesState.value = UiState.Success(currentStores.filterNot { it.id == storeId })
        _storeProductsState.value = UiState.Success(currentProducts.filterNot { it.storeId == storeId })
        _snackbarMessage.tryEmit("تم حذف المحل")
    }

    fun addStoreProduct(
        store: NeighborhoodStore,
        title: String,
        priceText: String,
        originalPriceText: String?,
        onOffer: Boolean,
        specsText: String?,
        description: String?
    ) {
        val current = (_storeProductsState.value as? UiState.Success)?.data.orEmpty()
        val newProduct = StoreProduct(
            id = "guest_prod_${UUID.randomUUID()}",
            storeId = store.id,
            userId = guestUserId,
            storeName = store.name,
            storeCategory = store.parsedCategory.name,
            storeSector = store.sector,
            storePhone = store.phone,
            title = title.trim(),
            description = description?.trim()?.takeIf { it.isNotBlank() },
            priceText = priceText.trim(),
            originalPriceText = originalPriceText?.trim()?.takeIf { it.isNotBlank() && onOffer },
            onOffer = onOffer,
            inStock = true,
            specsText = specsText?.trim()?.takeIf { it.isNotBlank() },
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )
        _storeProductsState.value = UiState.Success(listOf(newProduct) + current)
        _snackbarMessage.tryEmit("تمت إضافة البضاعة إلى ${store.name}")
    }

    fun toggleProductOfferAndStock(productId: String, onOffer: Boolean, inStock: Boolean) {
        val current = (_storeProductsState.value as? UiState.Success)?.data.orEmpty()
        _storeProductsState.value = UiState.Success(
            current.map { prod ->
                if (prod.id == productId) {
                    prod.copy(onOffer = onOffer, inStock = inStock, updatedAt = Timestamp.now())
                } else prod
            }
        )
    }

    fun deleteStoreProduct(productId: String) {
        val current = (_storeProductsState.value as? UiState.Success)?.data.orEmpty()
        _storeProductsState.value = UiState.Success(current.filterNot { it.id == productId })
        _snackbarMessage.tryEmit("تم حذف المنتج")
    }
}
