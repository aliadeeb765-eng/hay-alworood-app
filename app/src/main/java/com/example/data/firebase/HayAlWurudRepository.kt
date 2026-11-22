package com.example.data.firebase

import android.content.Context
import com.example.R
import com.example.data.model.CommunityPost
import com.example.data.model.FavoriteService
import com.example.data.model.HayAlWurudSectors
import com.example.data.model.NeighborhoodStore
import com.example.data.model.PostCategory
import com.example.data.model.PostStatus
import com.example.data.model.ResidentProfile
import com.example.data.model.ServiceCategory
import com.example.data.model.ServiceProvider
import com.example.data.model.ServiceRecommendation
import com.example.data.model.StoreCategory
import com.example.data.model.StoreProduct
import com.example.data.model.UtilityStatus
import com.example.data.model.UtilityType
import com.example.data.model.UtilityUpdate
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class HayAlWurudRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    init {
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(
                    PersistentCacheSettings.newBuilder()
                        .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                        .build()
                )
                .build()
            db.firestoreSettings = settings
        } catch (_: IllegalStateException) {
            // Firestore already started; default persistent cache remains active.
        }
    }

    private val _pendingSyncSources = MutableStateFlow<Set<String>>(emptySet())
    private val _hasPendingWrites = MutableStateFlow(false)
    val hasPendingWritesFlow: StateFlow<Boolean> = _hasPendingWrites.asStateFlow()

    private fun updatePendingState(source: String, hasPending: Boolean) {
        val current = _pendingSyncSources.value
        val updated = if (hasPending) current + source else current - source
        _pendingSyncSources.value = updated
        _hasPendingWrites.value = updated.isNotEmpty()
    }
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("يجب تسجيل الدخول بحساب Google أولاً.")
    }

    private fun <T> Flow<T>.logFirestoreErrors(
        operationType: OperationType,
        path: String,
        expectedUid: String
    ): Flow<T> = catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == expectedUid) {
            handleFirestoreError(exceptionToLog, operationType, path)
        }
        throw error
    }

    // --- Resident Profile ---
    fun observeResidentProfile(userId: String): Flow<ResidentProfile?> = flow {
        val uid = requireUserId()
        check(uid == userId) { "جلسة المستخدم تغيرت." }
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(ResidentProfile::class.java, ServerTimestampBehavior.ESTIMATE)
                    } else null
                }
                .logFirestoreErrors(OperationType.GET, path, uid)
        )
    }

    suspend fun ensureResidentProfile(
        userId: String,
        displayName: String,
        email: String?
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "عدم تطابق هوية المستخدم." }
            val docRef = db.collection("users").document(uid)
            val existing = docRef.get().await()
            if (!existing.exists()) {
                val safeName = displayName.trim().ifBlank { "أحد أهالي الحي" }.take(80)
                val safeEmail = email?.trim()?.takeIf { it.isNotBlank() }?.take(160)
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "email" to safeEmail,
                    "preferredSector" to HayAlWurudSectors.ALL.first(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun updatePreferredSector(userId: String, sector: String): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId)
            val safeSector = sector.trim().take(60)
            require(safeSector.isNotEmpty()) { "يرجى اختيار الحارة" }
            db.collection("users").document(uid).update(
                mapOf(
                    "preferredSector" to safeSector,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- Resident Favorites ---
    fun observeFavorites(userId: String): Flow<List<FavoriteService>> = flow {
        val uid = requireUserId()
        check(uid == userId)
        val path = "users/$uid/favorites"
        emitAll(
            db.collection("users").document(uid).collection("favorites")
                .snapshots(MetadataChanges.INCLUDE)
                .map { snapshot ->
                    updatePendingState("favorites", snapshot.metadata.hasPendingWrites())
                    snapshot.toObjects(FavoriteService::class.java, ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .logFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    suspend fun toggleFavorite(
        userId: String,
        service: ServiceProvider,
        currentlyFavorited: Boolean
    ): Result<Unit> {
        val serviceId = service.id
        val path = "users/$userId/favorites/$serviceId"
        return try {
            val uid = requireUserId()
            check(uid == userId)
            require(serviceId.isNotBlank()) { "معرّف الخدمة غير صالح" }
            val docRef = db.collection("users").document(uid)
                .collection("favorites").document(serviceId)
            if (currentlyFavorited) {
                docRef.delete().await()
            } else {
                val payload = mapOf(
                    "userId" to uid,
                    "serviceId" to serviceId,
                    "serviceName" to service.name.trim().take(100),
                    "specialty" to service.specialty.trim().take(100),
                    "workPhone" to service.workPhone.trim().takeIf { it.isNotBlank() }?.take(30),
                    "sector" to service.sector.trim().take(60),
                    "createdAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    // --- Live Utility Updates ---
    fun observeUtilityUpdates(): Flow<List<UtilityUpdate>> = flow {
        val uid = requireUserId()
        val path = "utility_updates"
        emitAll(
            db.collection(path)
                .whereEqualTo("visibility", "NEIGHBORHOOD")
                .snapshots(MetadataChanges.INCLUDE)
                .map { snapshot ->
                    updatePendingState("utility_updates", snapshot.metadata.hasPendingWrites())
                    snapshot.toObjects(UtilityUpdate::class.java, ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .logFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    suspend fun addUtilityUpdate(
        authorName: String,
        utilityType: UtilityType,
        sector: String,
        status: UtilityStatus,
        note: String?
    ): Result<Unit> {
        var docPath = "utility_updates"
        return try {
            val uid = requireUserId()
            val safeAuthor = authorName.trim().ifBlank { "أحد الجيران" }.take(80)
            val safeSector = sector.trim().take(60)
            require(safeSector.isNotEmpty()) { "يرجى تحديد الحارة" }
            val safeNote = note?.trim()?.takeIf { it.isNotBlank() }?.take(300)

            val docRef = db.collection("utility_updates").document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "authorName" to safeAuthor,
                "utilityType" to utilityType.name,
                "sector" to safeSector,
                "status" to status.name,
                "note" to safeNote,
                "visibility" to "NEIGHBORHOOD",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteUtilityUpdate(reportId: String): Result<Unit> {
        val docPath = "utility_updates/$reportId"
        return try {
            requireUserId()
            require(reportId.isNotBlank())
            db.collection("utility_updates").document(reportId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    // --- Service Providers Directory ---
    fun observeServiceProviders(): Flow<List<ServiceProvider>> = flow {
        val uid = requireUserId()
        val path = "service_providers"
        emitAll(
            db.collection(path)
                .whereEqualTo("visibility", "NEIGHBORHOOD")
                .snapshots(MetadataChanges.INCLUDE)
                .map { snapshot ->
                    updatePendingState("service_providers", snapshot.metadata.hasPendingWrites())
                    snapshot.toObjects(ServiceProvider::class.java, ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .logFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    suspend fun addServiceProvider(
        authorName: String,
        name: String,
        category: ServiceCategory,
        specialty: String,
        sector: String,
        workPhone: String,
        workingHours: String?,
        notes: String?
    ): Result<Unit> {
        var docPath = "service_providers"
        return try {
            val uid = requireUserId()
            val safeName = name.trim().take(100)
            val safeSpecialty = specialty.trim().take(100)
            val safeSector = sector.trim().take(60)
            val safePhone = workPhone.trim().take(30)
            val safeAuthor = authorName.trim().ifBlank { "أحد الجيران" }.take(80)

            require(safeName.length >= 2) { "الاسم يجب أن لا يقل عن حرفين" }
            require(safeSpecialty.length >= 2) { "التخصص يجب أن لا يقل عن حرفين" }
            require(safeSector.isNotEmpty()) { "يرجى اختيار الحارة" }
            require(safePhone.length >= 4) { "يرجى إدخال رقم هاتف صحيح" }

            val safeHours = workingHours?.trim()?.takeIf { it.isNotBlank() }?.take(80)
            val safeNotes = notes?.trim()?.takeIf { it.isNotBlank() }?.take(300)

            val docRef = db.collection("service_providers").document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "authorName" to safeAuthor,
                "name" to safeName,
                "category" to category.name,
                "specialty" to safeSpecialty,
                "sector" to safeSector,
                "workPhone" to safePhone,
                "workingHours" to safeHours,
                "notes" to safeNotes,
                "visibility" to "NEIGHBORHOOD",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteServiceProvider(serviceId: String): Result<Unit> {
        val docPath = "service_providers/$serviceId"
        return try {
            requireUserId()
            require(serviceId.isNotBlank())
            db.collection("service_providers").document(serviceId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    // --- Service Recommendations ("أنصح به") ---
    fun observeServiceRecommendations(): Flow<List<ServiceRecommendation>> = flow {
        val uid = requireUserId()
        val path = "service_recommendations"
        emitAll(
            db.collection(path)
                .whereEqualTo("visibility", "NEIGHBORHOOD")
                .snapshots(MetadataChanges.INCLUDE)
                .map { snapshot ->
                    updatePendingState("service_recommendations", snapshot.metadata.hasPendingWrites())
                    snapshot.toObjects(ServiceRecommendation::class.java, ServerTimestampBehavior.ESTIMATE)
                }
                .logFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    suspend fun toggleServiceRecommendation(
        serviceId: String,
        authorName: String,
        currentlyRecommended: Boolean
    ): Result<Unit> {
        val uid = auth.currentUser?.uid
        val recId = "${serviceId}_${uid ?: "unauthenticated"}"
        val docPath = "service_recommendations/$recId"
        return try {
            val verifiedUid = requireUserId()
            require(serviceId.isNotBlank()) { "معرّف الخدمة غير صالح" }
            val actualRecId = "${serviceId}_${verifiedUid}"
            val docRef = db.collection("service_recommendations").document(actualRecId)
            if (currentlyRecommended) {
                docRef.delete().await()
            } else {
                val safeAuthor = authorName.trim().ifBlank { "أحد الجيران" }.take(80)
                val payload = mapOf(
                    "userId" to verifiedUid,
                    "serviceId" to serviceId,
                    "authorName" to safeAuthor,
                    "visibility" to "NEIGHBORHOOD",
                    "createdAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    // --- Community Board Posts ---
    fun observeCommunityPosts(): Flow<List<CommunityPost>> = flow {
        val uid = requireUserId()
        val path = "community_posts"
        emitAll(
            db.collection(path)
                .whereEqualTo("visibility", "NEIGHBORHOOD")
                .snapshots(MetadataChanges.INCLUDE)
                .map { snapshot ->
                    updatePendingState("community_posts", snapshot.metadata.hasPendingWrites())
                    snapshot.toObjects(CommunityPost::class.java, ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .logFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    suspend fun addCommunityPost(
        authorName: String,
        title: String,
        description: String,
        category: PostCategory,
        sector: String,
        priceText: String?,
        contactNote: String?
    ): Result<Unit> {
        var docPath = "community_posts"
        return try {
            val uid = requireUserId()
            val safeAuthor = authorName.trim().ifBlank { "أحد الجيران" }.take(80)
            val safeTitle = title.trim().take(120)
            val safeDesc = description.trim().take(600)
            val safeSector = sector.trim().take(60)

            require(safeTitle.length >= 2) { "العنوان قصير جداً" }
            require(safeDesc.length >= 2) { "التفاصيل مطلوبة" }
            require(safeSector.isNotEmpty()) { "يرجى اختيار الحارة" }

            val safePrice = priceText?.trim()?.takeIf { it.isNotBlank() }?.take(60)
            val safeContact = contactNote?.trim()?.takeIf { it.isNotBlank() }?.take(120)

            val docRef = db.collection("community_posts").document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "authorName" to safeAuthor,
                "title" to safeTitle,
                "description" to safeDesc,
                "category" to category.name,
                "sector" to safeSector,
                "priceText" to safePrice,
                "contactNote" to safeContact,
                "status" to PostStatus.OPEN.name,
                "visibility" to "NEIGHBORHOOD",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun markPostResolved(postId: String): Result<Unit> {
        val docPath = "community_posts/$postId"
        return try {
            requireUserId()
            require(postId.isNotBlank())
            db.collection("community_posts").document(postId).update(
                mapOf(
                    "status" to PostStatus.RESOLVED.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteCommunityPost(postId: String): Result<Unit> {
        val docPath = "community_posts/$postId"
        return try {
            requireUserId()
            require(postId.isNotBlank())
            db.collection("community_posts").document(postId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    // --- Registered Neighborhood Retail Stores ---
    fun observeStores(): Flow<List<NeighborhoodStore>> = flow {
        val uid = requireUserId()
        val path = "stores"
        emitAll(
            db.collection(path)
                .whereEqualTo("visibility", "NEIGHBORHOOD")
                .snapshots(MetadataChanges.INCLUDE)
                .map { snapshot ->
                    updatePendingState("stores", snapshot.metadata.hasPendingWrites())
                    snapshot.toObjects(NeighborhoodStore::class.java, ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .logFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    suspend fun registerStore(
        ownerName: String,
        name: String,
        category: StoreCategory,
        sector: String,
        phone: String,
        whatsappPhone: String?,
        workingHours: String?,
        description: String?
    ): Result<Unit> {
        var docPath = "stores"
        return try {
            val uid = requireUserId()
            val safeOwner = ownerName.trim().ifBlank { "صاحب محل" }.take(80)
            val safeName = name.trim().take(100)
            val safeSector = sector.trim().take(60)
            val safePhone = phone.trim().take(30)

            require(safeName.length >= 2) { "اسم المحل يجب أن لا يقل عن حرفين" }
            require(safeSector.isNotEmpty()) { "يرجى اختيار الحارة" }
            require(safePhone.length >= 4) { "يرجى إدخال رقم هاتف صحيح" }

            val safeWhatsapp = whatsappPhone?.trim()?.takeIf { it.isNotBlank() }?.take(30)
            val safeHours = workingHours?.trim()?.takeIf { it.isNotBlank() }?.take(80)
            val safeDesc = description?.trim()?.takeIf { it.isNotBlank() }?.take(300)

            val docRef = db.collection("stores").document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "ownerName" to safeOwner,
                "name" to safeName,
                "category" to category.name,
                "sector" to safeSector,
                "phone" to safePhone,
                "whatsappPhone" to safeWhatsapp,
                "workingHours" to safeHours,
                "description" to safeDesc,
                "storeOpen" to true,
                "visibility" to "NEIGHBORHOOD",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun toggleStoreOpen(storeId: String, storeOpen: Boolean): Result<Unit> {
        val docPath = "stores/$storeId"
        return try {
            requireUserId()
            require(storeId.isNotBlank())
            db.collection("stores").document(storeId).update(
                mapOf(
                    "storeOpen" to storeOpen,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteStore(storeId: String): Result<Unit> {
        val docPath = "stores/$storeId"
        return try {
            requireUserId()
            require(storeId.isNotBlank())
            db.collection("stores").document(storeId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    // --- Store Products & Daily Offers ---
    fun observeStoreProducts(): Flow<List<StoreProduct>> = flow {
        val uid = requireUserId()
        val path = "store_products"
        emitAll(
            db.collection(path)
                .whereEqualTo("visibility", "NEIGHBORHOOD")
                .snapshots(MetadataChanges.INCLUDE)
                .map { snapshot ->
                    updatePendingState("store_products", snapshot.metadata.hasPendingWrites())
                    snapshot.toObjects(StoreProduct::class.java, ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .logFirestoreErrors(OperationType.LIST, path, uid)
        )
    }

    suspend fun addStoreProduct(
        store: NeighborhoodStore,
        title: String,
        priceText: String,
        originalPriceText: String?,
        onOffer: Boolean,
        specsText: String?,
        description: String?
    ): Result<Unit> {
        var docPath = "store_products"
        return try {
            val uid = requireUserId()
            require(store.id.isNotBlank()) { "معرّف المحل غير صالح" }
            val safeTitle = title.trim().take(100)
            val safePrice = priceText.trim().take(60)
            val safeStoreName = store.name.trim().take(100)
            val safeStoreSector = store.sector.trim().take(60)
            val safeStorePhone = store.phone.trim().take(30)

            require(safeTitle.length >= 2) { "اسم البضاعة يجب أن لا يقل عن حرفين" }
            require(safePrice.isNotEmpty()) { "يرجى تحديد السعر" }

            val safeOrigPrice = originalPriceText?.trim()?.takeIf { it.isNotBlank() && onOffer }?.take(60)
            val safeSpecs = specsText?.trim()?.takeIf { it.isNotBlank() }?.take(100)
            val safeDesc = description?.trim()?.takeIf { it.isNotBlank() }?.take(300)

            val docRef = db.collection("store_products").document()
            docPath = docRef.path
            val payload = mapOf(
                "storeId" to store.id,
                "userId" to uid,
                "storeName" to safeStoreName,
                "storeCategory" to store.parsedCategory.name,
                "storeSector" to safeStoreSector,
                "storePhone" to safeStorePhone,
                "title" to safeTitle,
                "description" to safeDesc,
                "priceText" to safePrice,
                "originalPriceText" to safeOrigPrice,
                "onOffer" to onOffer,
                "inStock" to true,
                "specsText" to safeSpecs,
                "visibility" to "NEIGHBORHOOD",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun toggleStoreProductOfferAndStock(
        productId: String,
        onOffer: Boolean,
        inStock: Boolean
    ): Result<Unit> {
        val docPath = "store_products/$productId"
        return try {
            requireUserId()
            require(productId.isNotBlank())
            db.collection("store_products").document(productId).update(
                mapOf(
                    "onOffer" to onOffer,
                    "inStock" to inStock,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteStoreProduct(productId: String): Result<Unit> {
        val docPath = "store_products/$productId"
        return try {
            requireUserId()
            require(productId.isNotBlank())
            db.collection("store_products").document(productId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    // --- Initial Hay Al-Wurud Neighborhood Starter Data ---
    suspend fun seedStarterNeighborhoodData(authorName: String): Result<Unit> {
        return try {
            val uid = requireUserId()
            val safeAuthor = authorName.trim().ifBlank { "أهل حي الورود" }.take(80)
            val batch = db.batch()

            val starterUtilities = listOf(
                mapOf(
                    "userId" to uid,
                    "authorName" to safeAuthor,
                    "utilityType" to UtilityType.WATER.name,
                    "sector" to "الساحة الرئيسية",
                    "status" to UtilityStatus.AVAILABLE.name,
                    "note" to "الضخ شغال الآن على الساحة والحارة التحتانية، يرجى الاقتصاد بالاستهلاك.",
                    "visibility" to "NEIGHBORHOOD",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                mapOf(
                    "userId" to uid,
                    "authorName" to safeAuthor,
                    "utilityType" to UtilityType.ELECTRICITY.name,
                    "sector" to "حي الندى",
                    "status" to UtilityStatus.SCHEDULED.name,
                    "note" to "وصل الكهرباء حسب برنامج التقنين المعتاد، والأمبيرات متوفرة.",
                    "visibility" to "NEIGHBORHOOD",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                mapOf(
                    "userId" to uid,
                    "authorName" to safeAuthor,
                    "utilityType" to UtilityType.BREAD.name,
                    "sector" to "شارع المدارس",
                    "status" to UtilityStatus.AVAILABLE.name,
                    "note" to "فرن الحي الآلي والمعتمدين مستمرين بالتوزيع والازدحام خفيف.",
                    "visibility" to "NEIGHBORHOOD",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                mapOf(
                    "userId" to uid,
                    "authorName" to safeAuthor,
                    "utilityType" to UtilityType.TRANSPORT.name,
                    "sector" to "الساحة الرئيسية",
                    "status" to UtilityStatus.AVAILABLE.name,
                    "note" to "سرافيس خط حي الورود - البرامكة متواجدة في الموقف الرئيسي.",
                    "visibility" to "NEIGHBORHOOD",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            starterUtilities.forEach { data ->
                batch.set(db.collection("utility_updates").document(), data)
            }

            val starterServices = listOf(
                mapOf(
                    "userId" to uid,
                    "authorName" to safeAuthor,
                    "name" to "المعلم أبو أحمد للكهرباء",
                    "category" to ServiceCategory.MAINTENANCE.name,
                    "specialty" to "كهربائي منازل وإنفرتر وبطاريات",
                    "sector" to "الساحة الرئيسية",
                    "workPhone" to "0933112244",
                    "workingHours" to "8 صباحاً - 9 مساءً",
                    "notes" to "صيانة طوارئ للإنفرتر وألواح الطاقة الشمسية لأهالي الحي.",
                    "visibility" to "NEIGHBORHOOD",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                mapOf(
                    "userId" to uid,
                    "authorName" to safeAuthor,
                    "name" to "أبو طارق السنكري",
                    "category" to ServiceCategory.MAINTENANCE.name,
                    "specialty" to "سباكة وتمديدات صحية وخزانات",
                    "sector" to "الحارة التحتانية",
                    "workPhone" to "0944556677",
                    "workingHours" to "9 صباحاً - 8 مساءً",
                    "notes" to "تنظيف خزانات وتصليح مضخات المياه بسرعة وبأسعار شعبية.",
                    "visibility" to "NEIGHBORHOOD",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                mapOf(
                    "userId" to uid,
                    "authorName" to safeAuthor,
                    "name" to "صيدلية الورود المناوبة",
                    "category" to ServiceCategory.HEALTH.name,
                    "specialty" to "صيدلية وقياس ضغط وسكر",
                    "sector" to "محيط جامع الهدى",
                    "workPhone" to "0113712233",
                    "workingHours" to "مفتوحة حتى 12 ليلاً",
                    "notes" to "يتوفر استشارات دوائية ومستلزمات أطفال.",
                    "visibility" to "NEIGHBORHOOD",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                mapOf(
                    "userId" to uid,
                    "authorName" to safeAuthor,
                    "name" to "فرن ومعجنات البركة",
                    "category" to ServiceCategory.FOOD.name,
                    "specialty" to "خبز وصمون وفطائر صباحية",
                    "sector" to "شارع المدارس",
                    "workPhone" to "0955889900",
                    "workingHours" to "5:30 فجراً - 7 مساءً",
                    "notes" to "صفيحة دمشقية ومناقيش زعتر وجبنة يومياً.",
                    "visibility" to "NEIGHBORHOOD",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                mapOf(
                    "userId" to uid,
                    "authorName" to safeAuthor,
                    "name" to "أبو علي لنقل المياه والطلبات",
                    "category" to ServiceCategory.TRANSPORT.name,
                    "specialty" to "توصيل غاز، مياه شرب، ونقل أغراض",
                    "sector" to "منطقة الجبل",
                    "workPhone" to "0966334455",
                    "workingHours" to "7 صباحاً - 10 مساءً",
                    "notes" to "خدمة طلوع للجبل والأزقة الضيقة بدراجة شحن.",
                    "visibility" to "NEIGHBORHOOD",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            starterServices.forEach { data ->
                batch.set(db.collection("service_providers").document(), data)
            }

            val starterPosts = listOf(
                mapOf(
                    "userId" to uid,
                    "authorName" to safeAuthor,
                    "title" to "حملة نظافة وتشجير حارة الندى يوم الجمعة",
                    "description" to "بدعوة من شباب حي الورود، نجتمع الساعة 9 صباحاً أمام جامع الهدى لتنظيف الأرصفة وزراعة شتول ياسمين وورد جوري.",
                    "category" to PostCategory.NOTICE.name,
                    "sector" to "محيط جامع الهدى",
                    "contactNote" to "التجمع أمام مدخل جامع الهدى",
                    "status" to PostStatus.OPEN.name,
                    "visibility" to "NEIGHBORHOOD",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                mapOf(
                    "userId" to uid,
                    "authorName" to safeAuthor,
                    "title" to "وجدنا مفاتيح بيت مع ميدالية خضراء قرب الساحة",
                    "description" to "تم العثور على سلسلة مفاتيح بجانب موقف السرافيس في الساحة الرئيسية، وهي محفوظة عند سمانة أبو خليل.",
                    "category" to PostCategory.LOST_FOUND.name,
                    "sector" to "الساحة الرئيسية",
                    "contactNote" to "موجودة أمانة لدى سمانة أبو خليل",
                    "status" to PostStatus.OPEN.name,
                    "visibility" to "NEIGHBORHOOD",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                mapOf(
                    "userId" to uid,
                    "authorName" to safeAuthor,
                    "title" to "مكدوس ومونة بيتية شغل يد نظيف",
                    "description" to "متوفر مكدوس باذنجان بلدي بالزيت الأصلي والجوز، وكبة مقلية ومفرزة حسب الطلب لأهالي الحي.",
                    "category" to PostCategory.MARKET.name,
                    "sector" to "منطقة الجبل",
                    "priceText" to "أسعار مراعية لأهل الحي",
                    "contactNote" to "التوصيل مجاني داخل حي الورود",
                    "status" to PostStatus.OPEN.name,
                    "visibility" to "NEIGHBORHOOD",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                mapOf(
                    "userId" to uid,
                    "authorName" to safeAuthor,
                    "title" to "مطلوب سلم ألمنيوم لساعة لتفقد خزان السطح",
                    "description" to "نحتاج استعارة سلم طويل لمدة ساعة فقط لتثبيت عوامة خزان المياه وإعادته فوراً، جزاكم الله خيراً.",
                    "category" to PostCategory.HELP.name,
                    "sector" to "منطقة الجبل",
                    "contactNote" to "البناية المقابلة لفرن الجبل",
                    "status" to PostStatus.OPEN.name,
                    "visibility" to "NEIGHBORHOOD",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                mapOf(
                    "userId" to uid,
                    "authorName" to safeAuthor,
                    "title" to "فزعة توصيل أسطوانات غاز ومشتريات لكبار السن",
                    "description" to "شباب المتطوعين جاهزون يومياً بعد العصر لمساعدة كبار السن بنقل الغاز والمشتريات مجاناً.",
                    "category" to PostCategory.HELP.name,
                    "sector" to "الحارة التحتانية",
                    "contactNote" to "نقطة التنسيق عند سمانة البركة",
                    "status" to PostStatus.OPEN.name,
                    "visibility" to "NEIGHBORHOOD",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            starterPosts.forEach { data ->
                batch.set(db.collection("community_posts").document(), data)
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "starter_seed_batch")
            Result.failure(e)
        }
    }
}
