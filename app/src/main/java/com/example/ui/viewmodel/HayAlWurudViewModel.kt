package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.HayAlWurudRepository
import com.example.data.model.CommunityPost
import com.example.data.model.CraftFilterCategory
import com.example.data.model.DeliveryOrderType
import com.example.data.model.DeliveryRequest
import com.example.data.model.DeliveryStatus
import com.example.data.model.DeliveryVehicleType
import com.example.data.model.DeliveryZone
import com.example.data.model.FavoriteService
import com.example.data.model.NeighborhoodStore
import com.example.data.model.PostCategory
import com.example.data.model.ResidentProfile
import com.example.data.model.ServiceCategory
import com.example.data.model.ServiceProvider
import com.example.data.model.ServiceRecommendation
import com.example.data.model.StoreCategory
import com.example.data.model.StoreProduct
import com.example.data.model.StoreReview
import com.example.data.model.UtilityStatus
import com.example.data.model.UtilityType
import com.example.data.model.UtilityUpdate
import com.example.data.network.ConnectivityObserver
import com.example.ui.state.UiState
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HayAlWurudViewModel(
    private val repository: HayAlWurudRepository,
    connectivityObserver: ConnectivityObserver,
    val currentUserId: String,
    val currentUserName: String,
    private val currentUserEmail: String?
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = connectivityObserver.observeIsOnline()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = connectivityObserver.isCurrentlyOnline()
        )

    val hasPendingWrites: StateFlow<Boolean> = repository.hasPendingWritesFlow

    private fun <T> Flow<T>.toUiStateFlow(fallbackMessage: String): StateFlow<UiState<T>> =
        this.map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, fallbackMessage, rootError)
                emit(UiState.Error(rootError.message ?: fallbackMessage))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val profileState: StateFlow<UiState<ResidentProfile?>> =
        repository.observeResidentProfile(currentUserId)
            .toUiStateFlow("تعذّر تحميل بيانات الحساب")

    val favoritesState: StateFlow<UiState<List<FavoriteService>>> =
        repository.observeFavorites(currentUserId)
            .toUiStateFlow("تعذّر تحميل المفضلة")

    val utilitiesState: StateFlow<UiState<List<UtilityUpdate>>> =
        repository.observeUtilityUpdates()
            .toUiStateFlow("تعذّر تحميل حالة المرافق")

    val servicesState: StateFlow<UiState<List<ServiceProvider>>> =
        repository.observeServiceProviders()
            .toUiStateFlow("تعذّر تحميل دليل الخدمات")

    val recommendationsState: StateFlow<UiState<List<ServiceRecommendation>>> =
        repository.observeServiceRecommendations()
            .toUiStateFlow("تعذّر تحميل توصيات الجيران")

    val postsState: StateFlow<UiState<List<CommunityPost>>> =
        repository.observeCommunityPosts()
            .toUiStateFlow("تعذّر تحميل منشورات الحي")

    val storesState: StateFlow<UiState<List<NeighborhoodStore>>> =
        repository.observeStores()
            .toUiStateFlow("تعذّر تحميل متاجر الحي")

    val storeProductsState: StateFlow<UiState<List<StoreProduct>>> =
        repository.observeStoreProducts()
            .toUiStateFlow("تعذّر تحميل بضائع وعروض المتاجر")

    val storeReviewsState: StateFlow<UiState<List<StoreReview>>> =
        repository.observeStoreReviews()
            .toUiStateFlow("تعذّر تحميل تقييمات المتاجر")

    val deliveryRequestsState: StateFlow<UiState<List<DeliveryRequest>>> =
        repository.observeDeliveryRequests()
            .toUiStateFlow("تعذّر تحميل طلبات التوصيل والنقل")

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

    private val _storeSearchQuery = MutableStateFlow("")
    val storeSearchQuery: StateFlow<String> = _storeSearchQuery.asStateFlow()

    private val _selectedStoreCategory = MutableStateFlow<StoreCategory?>(null)
    val selectedStoreCategory: StateFlow<StoreCategory?> = _selectedStoreCategory.asStateFlow()

    private val _showOffersOnly = MutableStateFlow(false)
    val showOffersOnly: StateFlow<Boolean> = _showOffersOnly.asStateFlow()

    private val _sortStoresByRating = MutableStateFlow(false)
    val sortStoresByRating: StateFlow<Boolean> = _sortStoresByRating.asStateFlow()

    private val _isMutating = MutableStateFlow(false)
    val isMutating: StateFlow<Boolean> = _isMutating.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.ensureResidentProfile(currentUserId, currentUserName, currentUserEmail)
        }
    }

    fun updateServiceSearch(query: String) {
        _serviceSearchQuery.value = query
    }

    fun selectServiceCategory(category: CraftFilterCategory) {
        _selectedServiceCategory.value = category
    }

    fun selectSectorFilter(sector: String?) {
        _selectedSectorFilter.value = sector
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
        viewModelScope.launch {
            _isMutating.value = true
            val result = repository.updatePreferredSector(currentUserId, sector)
            _isMutating.value = false
            result.fold(
                onSuccess = { _snackbarMessage.tryEmit("تم تحديث حارتك إلى: $sector") },
                onFailure = { _snackbarMessage.tryEmit("تعذّر تحديث الحارة، حاول مجدداً") }
            )
        }
    }

    fun toggleFavorite(service: ServiceProvider, currentlyFavorited: Boolean) {
        viewModelScope.launch {
            val result = repository.toggleFavorite(currentUserId, service, currentlyFavorited)
            result.fold(
                onSuccess = {
                    val msg = if (currentlyFavorited) "تمت الإزالة من المحفوظات" else "تم حفظ ${service.name} في مفضلتك"
                    _snackbarMessage.tryEmit(msg)
                },
                onFailure = { _snackbarMessage.tryEmit("تعذّر تحديث المفضلة") }
            )
        }
    }

    fun submitUtilityUpdate(
        utilityType: UtilityType,
        sector: String,
        status: UtilityStatus,
        note: String?
    ) {
        viewModelScope.launch {
            _isMutating.value = true
            val result = repository.addUtilityUpdate(currentUserName, utilityType, sector, status, note)
            _isMutating.value = false
            result.fold(
                onSuccess = { _snackbarMessage.tryEmit("شكرًا لمشاركتك! تم تحديث حالة ${utilityType.arabicName}") },
                onFailure = { err -> _snackbarMessage.tryEmit(err.message ?: "تعذّر إرسال التحديث") }
            )
        }
    }

    fun deleteUtilityUpdate(reportId: String) {
        viewModelScope.launch {
            val result = repository.deleteUtilityUpdate(reportId)
            result.fold(
                onSuccess = { _snackbarMessage.tryEmit("تم حذف البلاغ") },
                onFailure = { _snackbarMessage.tryEmit("تعذّر حذف البلاغ") }
            )
        }
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
        viewModelScope.launch {
            _isMutating.value = true
            val result = repository.addServiceProvider(
                authorName = currentUserName,
                name = name,
                category = category,
                specialty = specialty,
                sector = sector,
                workPhone = workPhone,
                workingHours = workingHours,
                notes = notes
            )
            _isMutating.value = false
            result.fold(
                onSuccess = { _snackbarMessage.tryEmit("تمت إضافة الخدمة إلى دليل حي الورود") },
                onFailure = { err -> _snackbarMessage.tryEmit(err.message ?: "تعذّر إضافة الخدمة") }
            )
        }
    }

    fun deleteServiceProvider(serviceId: String) {
        viewModelScope.launch {
            val result = repository.deleteServiceProvider(serviceId)
            result.fold(
                onSuccess = { _snackbarMessage.tryEmit("تم حذف الخدمة من الدليل") },
                onFailure = { _snackbarMessage.tryEmit("تعذّر حذف الخدمة") }
            )
        }
    }

    fun toggleSortServicesByTrust() {
        _sortServicesByTrust.value = !_sortServicesByTrust.value
    }

    fun toggleServiceRecommendation(service: ServiceProvider, currentlyRecommended: Boolean) {
        viewModelScope.launch {
            val result = repository.toggleServiceRecommendation(
                serviceId = service.id,
                authorName = currentUserName,
                currentlyRecommended = currentlyRecommended
            )
            result.fold(
                onSuccess = {
                    val msg = if (currentlyRecommended) {
                        "تمت إزالة توصيتك لـ ${service.name}"
                    } else {
                        "تمت إضافة توصيتك لـ ${service.name} لمساعدة الجيران"
                    }
                    _snackbarMessage.tryEmit(msg)
                },
                onFailure = { _snackbarMessage.tryEmit("تعذّر تسجيل التوصية، حاول مجدداً") }
            )
        }
    }

    fun submitCommunityPost(
        title: String,
        description: String,
        category: PostCategory,
        sector: String,
        priceText: String?,
        contactNote: String?
    ) {
        viewModelScope.launch {
            _isMutating.value = true
            val result = repository.addCommunityPost(
                authorName = currentUserName,
                title = title,
                description = description,
                category = category,
                sector = sector,
                priceText = priceText,
                contactNote = contactNote
            )
            _isMutating.value = false
            result.fold(
                onSuccess = { _snackbarMessage.tryEmit("تم نشر إعلانك لأهالي الحي") },
                onFailure = { err -> _snackbarMessage.tryEmit(err.message ?: "تعذّر النشر") }
            )
        }
    }

    fun markPostResolved(postId: String) {
        viewModelScope.launch {
            val result = repository.markPostResolved(postId)
            result.fold(
                onSuccess = { _snackbarMessage.tryEmit("تم إغلاق المنشور بنجاح") },
                onFailure = { _snackbarMessage.tryEmit("تعذّر تحديث حالة المنشور") }
            )
        }
    }

    fun deleteCommunityPost(postId: String) {
        viewModelScope.launch {
            val result = repository.deleteCommunityPost(postId)
            result.fold(
                onSuccess = { _snackbarMessage.tryEmit("تم حذف المنشور") },
                onFailure = { _snackbarMessage.tryEmit("تعذّر حذف المنشور") }
            )
        }
    }

    fun loadStarterNeighborhoodData() {
        viewModelScope.launch {
            _isMutating.value = true
            val result = repository.seedStarterNeighborhoodData(currentUserName)
            _isMutating.value = false
            result.fold(
                onSuccess = { _snackbarMessage.tryEmit("تم تحميل دليل ومرافق حي الورود الأساسية") },
                onFailure = { _snackbarMessage.tryEmit("تعذّر تحميل البيانات التأسيسية") }
            )
        }
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
        viewModelScope.launch {
            _isMutating.value = true
            val result = repository.registerStore(
                ownerName = currentUserName,
                name = name,
                category = category,
                sector = sector,
                phone = phone,
                whatsappPhone = whatsappPhone,
                workingHours = workingHours,
                description = description
            )
            _isMutating.value = false
            result.fold(
                onSuccess = { _snackbarMessage.tryEmit("تم تسجيل محلك في دليل متاجر حي الورود") },
                onFailure = { err -> _snackbarMessage.tryEmit(err.message ?: "تعذّر تسجيل المحل") }
            )
        }
    }

    fun toggleStoreOpen(storeId: String, nextOpen: Boolean) {
        viewModelScope.launch {
            val result = repository.toggleStoreOpen(storeId, nextOpen)
            result.fold(
                onSuccess = {
                    _snackbarMessage.tryEmit(if (nextOpen) "تم تعيين المحل كمفتوح" else "تم تعيين المحل كمغلق مؤقتاً")
                },
                onFailure = { _snackbarMessage.tryEmit("تعذّر تحديث حالة المحل") }
            )
        }
    }

    fun deleteStore(storeId: String) {
        viewModelScope.launch {
            val result = repository.deleteStore(storeId)
            result.fold(
                onSuccess = { _snackbarMessage.tryEmit("تم حذف المحل من الدليل") },
                onFailure = { _snackbarMessage.tryEmit("تعذّر حذف المحل") }
            )
        }
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
        viewModelScope.launch {
            _isMutating.value = true
            val result = repository.addStoreProduct(
                store = store,
                title = title,
                priceText = priceText,
                originalPriceText = originalPriceText,
                onOffer = onOffer,
                specsText = specsText,
                description = description
            )
            _isMutating.value = false
            result.fold(
                onSuccess = { _snackbarMessage.tryEmit("تمت إضافة البضاعة إلى متجر ${store.name}") },
                onFailure = { err -> _snackbarMessage.tryEmit(err.message ?: "تعذّر إضافة البضاعة") }
            )
        }
    }

    fun toggleProductOfferAndStock(productId: String, onOffer: Boolean, inStock: Boolean) {
        viewModelScope.launch {
            val result = repository.toggleStoreProductOfferAndStock(productId, onOffer, inStock)
            result.fold(
                onSuccess = { _snackbarMessage.tryEmit("تم تحديث حالة المنتج") },
                onFailure = { _snackbarMessage.tryEmit("تعذّر تحديث حالة المنتج") }
            )
        }
    }

    fun deleteStoreProduct(productId: String) {
        viewModelScope.launch {
            val result = repository.deleteStoreProduct(productId)
            result.fold(
                onSuccess = { _snackbarMessage.tryEmit("تم حذف المنتج") },
                onFailure = { _snackbarMessage.tryEmit("تعذّر حذف المنتج") }
            )
        }
    }

    fun toggleSortStoresByRating() {
        _sortStoresByRating.value = !_sortStoresByRating.value
    }

    fun submitStoreReview(
        store: NeighborhoodStore,
        serviceQualityRating: Int,
        offerCredibilityRating: Int,
        comment: String?
    ) {
        viewModelScope.launch {
            _isMutating.value = true
            val result = repository.submitStoreReview(
                store = store,
                reviewerName = currentUserName,
                serviceQualityRating = serviceQualityRating,
                offerCredibilityRating = offerCredibilityRating,
                comment = comment
            )
            _isMutating.value = false
            result.fold(
                onSuccess = { _snackbarMessage.tryEmit("شكراً لتقييمك! تم حفظ رأيك حول ${store.name}") },
                onFailure = { err -> _snackbarMessage.tryEmit(err.message ?: "تعذّر حفظ التقييم") }
            )
        }
    }

    fun deleteStoreReview(storeId: String) {
        viewModelScope.launch {
            val result = repository.deleteStoreReview(storeId)
            result.fold(
                onSuccess = { _snackbarMessage.tryEmit("تم حذف تقييمك") },
                onFailure = { _snackbarMessage.tryEmit("تعذّر حذف التقييم") }
            )
        }
    }

    fun submitDeliveryRequest(
        orderType: DeliveryOrderType,
        vehicleType: DeliveryVehicleType,
        pickupLocation: String,
        orderDetails: String,
        deliveryZone: DeliveryZone,
        detailedAddress: String,
        gpsMapsUrl: String?,
        customerName: String,
        customerPhone: String
    ) {
        viewModelScope.launch {
            _isMutating.value = true
            val result = repository.createDeliveryRequest(
                orderType = orderType,
                vehicleType = vehicleType,
                pickupLocation = pickupLocation,
                orderDetails = orderDetails,
                deliveryZone = deliveryZone,
                detailedAddress = detailedAddress,
                gpsMapsUrl = gpsMapsUrl,
                customerName = customerName,
                customerPhone = customerPhone
            )
            _isMutating.value = false
            result.fold(
                onSuccess = { _snackbarMessage.tryEmit("تم إرسال طلب التوصيل والنقل السريع بنجاح!") },
                onFailure = { err -> _snackbarMessage.tryEmit(err.message ?: "تعذّر إرسال طلب التوصيل") }
            )
        }
    }

    fun updateDeliveryStatus(requestId: String, nextStatus: DeliveryStatus) {
        viewModelScope.launch {
            val result = repository.updateDeliveryStatus(requestId, nextStatus)
            result.fold(
                onSuccess = {
                    val msg = if (nextStatus == DeliveryStatus.COMPLETED) "تم تأكيد إتمام التوصيل" else "تم إلغاء طلب التوصيل"
                    _snackbarMessage.tryEmit(msg)
                },
                onFailure = { _snackbarMessage.tryEmit("تعذّر تحديث حالة الطلب") }
            )
        }
    }

    fun deleteDeliveryRequest(requestId: String) {
        viewModelScope.launch {
            val result = repository.deleteDeliveryRequest(requestId)
            result.fold(
                onSuccess = { _snackbarMessage.tryEmit("تم حذف طلب التوصيل") },
                onFailure = { _snackbarMessage.tryEmit("تعذّر حذف طلب التوصيل") }
            )
        }
    }

    private companion object {
        const val TAG = "HayAlWurudVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
