package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

object HayAlWurudSectors {
    val ALL = listOf(
        "الساحة الرئيسية",
        "الحارة التحتانية",
        "منطقة الجبل",
        "حي الندى",
        "شارع المدارس",
        "محيط جامع الهدى"
    )
}

enum class UtilityType(val arabicName: String, val subtitle: String) {
    WATER("المياه", "ضخ وخزانات الحي"),
    ELECTRICITY("الكهرباء", "التقنين والأمبيرات"),
    BREAD("الفرن والخبز", "الأفران والمعتمدين"),
    GAS("الغاز المنزلي", "الموزعين والرسائل"),
    TRANSPORT("السرفيس والنقل", "خط حي الورود - البرامكة");

    companion object {
        fun fromRaw(raw: String): UtilityType =
            entries.firstOrNull { it.name == raw } ?: WATER
    }
}

enum class UtilityStatus(val arabicLabel: String) {
    AVAILABLE("متوفر الآن"),
    CUT("مقطوع / متوقف"),
    CROWDED("ازدحام شديد"),
    SCHEDULED("حسب الدور");

    companion object {
        fun fromRaw(raw: String): UtilityStatus =
            entries.firstOrNull { it.name == raw } ?: SCHEDULED
    }
}

enum class ServiceCategory(val arabicName: String) {
    MAINTENANCE("صيانة ومهن"),
    HEALTH("صحة وصيدليات"),
    FOOD("أفران وتموين"),
    TRANSPORT("نقل وتوصيل"),
    EDUCATION("تعليم وخدمات"),
    OTHER("خدمات أخرى");

    companion object {
        fun fromRaw(raw: String): ServiceCategory =
            entries.firstOrNull { it.name == raw } ?: OTHER
    }
}

enum class CraftFilterCategory(val arabicLabel: String) {
    ALL("الكل"),
    PLUMBING("سباكة"),
    ELECTRICAL("كهرباء"),
    MAINTENANCE("صيانة"),
    HEALTH("صحة"),
    FOOD("أفران وتموين"),
    TRANSPORT("نقل وتوصيل"),
    EDUCATION("تعليم"),
    OTHER("أخرى");

    fun matches(service: ServiceProvider): Boolean =
        matchesCraftDetails(
            category = service.parsedCategory,
            name = service.name,
            specialty = service.specialty
        )

    fun matches(service: NeighborhoodService): Boolean =
        matchesCraftDetails(
            category = service.category,
            name = service.name,
            specialty = service.profession
        )

    fun matchesCraftDetails(
        category: ServiceCategory,
        name: String,
        specialty: String
    ): Boolean {
        val resolved = resolveCraftCategory(category, name, specialty)
        return when (this) {
            ALL -> true
            PLUMBING -> resolved == PLUMBING
            ELECTRICAL -> resolved == ELECTRICAL
            MAINTENANCE -> resolved == MAINTENANCE ||
                specialty.contains("صيانة") ||
                specialty.contains("تصليح")
            HEALTH -> category == ServiceCategory.HEALTH
            FOOD -> category == ServiceCategory.FOOD
            TRANSPORT -> category == ServiceCategory.TRANSPORT
            EDUCATION -> category == ServiceCategory.EDUCATION
            OTHER -> category == ServiceCategory.OTHER
        }
    }

    companion object {
        fun resolveCraftCategory(
            category: ServiceCategory,
            name: String,
            specialty: String
        ): CraftFilterCategory {
            val text = "$name $specialty"
            val isPlumbing = text.contains("سباك") ||
                text.contains("سنكري") ||
                text.contains("تمديدات صحية") ||
                text.contains("صحية") ||
                text.contains("خزانات") ||
                text.contains("مضخات")
            if (isPlumbing) return PLUMBING

            val isElectrical = text.contains("كهرب") ||
                text.contains("إنفرتر") ||
                text.contains("انفرتر") ||
                text.contains("بطاريات") ||
                text.contains("طاقة شمسية")
            if (isElectrical) return ELECTRICAL

            return when (category) {
                ServiceCategory.MAINTENANCE -> MAINTENANCE
                ServiceCategory.HEALTH -> HEALTH
                ServiceCategory.FOOD -> FOOD
                ServiceCategory.TRANSPORT -> TRANSPORT
                ServiceCategory.EDUCATION -> EDUCATION
                ServiceCategory.OTHER -> OTHER
            }
        }
    }
}

enum class PostCategory(val arabicName: String) {
    HELP("فزعة ومساعدة"),
    LOST_FOUND("مفقودات وموجودات"),
    MARKET("سوق الحارة"),
    NOTICE("إعلانات وتنبيهات");

    companion object {
        fun fromRaw(raw: String): PostCategory =
            entries.firstOrNull { it.name == raw } ?: NOTICE
    }
}

enum class PostStatus(val arabicLabel: String) {
    OPEN("مفتوح"),
    RESOLVED("تم الحل");

    companion object {
        fun fromRaw(raw: String): PostStatus =
            entries.firstOrNull { it.name == raw } ?: OPEN
    }
}

@Keep
@IgnoreExtraProperties
data class ResidentProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String? = null,
    val preferredSector: String = HayAlWurudSectors.ALL.first(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FavoriteService(
    @DocumentId val id: String = "",
    val userId: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val specialty: String = "",
    val workPhone: String = "",
    val sector: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class UtilityUpdate(
    @DocumentId val id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val utilityType: String = UtilityType.WATER.name,
    val sector: String = HayAlWurudSectors.ALL.first(),
    val status: String = UtilityStatus.AVAILABLE.name,
    val note: String? = null,
    val visibility: String = "NEIGHBORHOOD",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val parsedType: UtilityType get() = UtilityType.fromRaw(utilityType)
    val parsedStatus: UtilityStatus get() = UtilityStatus.fromRaw(status)
}

@Keep
@IgnoreExtraProperties
data class ServiceProvider(
    @DocumentId val id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val name: String = "",
    val category: String = ServiceCategory.MAINTENANCE.name,
    val specialty: String = "",
    val sector: String = HayAlWurudSectors.ALL.first(),
    val workPhone: String = "",
    val workingHours: String? = null,
    val notes: String? = null,
    val visibility: String = "NEIGHBORHOOD",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val parsedCategory: ServiceCategory get() = ServiceCategory.fromRaw(category)
}

@Keep
@IgnoreExtraProperties
data class ServiceRecommendation(
    @DocumentId val id: String = "",
    val userId: String = "",
    val serviceId: String = "",
    val authorName: String = "",
    val visibility: String = "NEIGHBORHOOD",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CommunityPost(
    @DocumentId val id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = PostCategory.HELP.name,
    val sector: String = HayAlWurudSectors.ALL.first(),
    val priceText: String? = null,
    val contactNote: String? = null,
    val status: String = PostStatus.OPEN.name,
    val visibility: String = "NEIGHBORHOOD",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val parsedCategory: PostCategory get() = PostCategory.fromRaw(category)
    val parsedStatus: PostStatus get() = PostStatus.fromRaw(status)
}

data class EmergencyContact(
    val title: String,
    val number: String,
    val description: String
)

object DamascusEmergencyContacts {
    val ALL = listOf(
        EmergencyContact("الإسعاف", "110", "طوارئ الإسعاف السريع"),
        EmergencyContact("الإطفاء", "113", "فوج إطفاء دمشق"),
        EmergencyContact("الشرطة", "112", "شرطة النجدة"),
        EmergencyContact("طوارئ الكهرباء", "117", "أعطال الشبكة والمحولات"),
        EmergencyContact("طوارئ المياه", "119", "أعطال خطوط المياه")
    )
}

enum class StoreCategory(val arabicName: String) {
    CLOTHING("ألبسة"),
    SHOES("أحذية"),
    COSMETICS("مكياج وعطور"),
    STATIONERY("مكتبات وقرطاسية"),
    TECH_ACCESSORIES("كمبيوتر وإكسسوارات"),
    HOME_GIFTS("أدوات وهدايا"),
    OTHER("متاجر أخرى");

    companion object {
        fun fromRaw(raw: String): StoreCategory =
            entries.firstOrNull { it.name == raw } ?: OTHER
    }
}

@Keep
@IgnoreExtraProperties
data class NeighborhoodStore(
    @DocumentId val id: String = "",
    val userId: String = "",
    val ownerName: String = "",
    val name: String = "",
    val category: String = StoreCategory.CLOTHING.name,
    val sector: String = HayAlWurudSectors.ALL.first(),
    val phone: String = "",
    val whatsappPhone: String? = null,
    val workingHours: String? = null,
    val description: String? = null,
    val storeOpen: Boolean = true,
    val visibility: String = "NEIGHBORHOOD",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val parsedCategory: StoreCategory get() = StoreCategory.fromRaw(category)
}

@Keep
@IgnoreExtraProperties
data class StoreProduct(
    @DocumentId val id: String = "",
    val storeId: String = "",
    val userId: String = "",
    val storeName: String = "",
    val storeCategory: String = StoreCategory.CLOTHING.name,
    val storeSector: String = HayAlWurudSectors.ALL.first(),
    val storePhone: String = "",
    val title: String = "",
    val description: String? = null,
    val priceText: String = "",
    val originalPriceText: String? = null,
    val onOffer: Boolean = false,
    val inStock: Boolean = true,
    val specsText: String? = null,
    val visibility: String = "NEIGHBORHOOD",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val parsedStoreCategory: StoreCategory get() = StoreCategory.fromRaw(storeCategory)
}

@Keep
@IgnoreExtraProperties
data class StoreReview(
    @DocumentId val id: String = "",
    val storeId: String = "",
    val userId: String = "",
    val reviewerName: String = "",
    val serviceQualityRating: Int = 5,
    val offerCredibilityRating: Int = 5,
    val overallRating: Double = 5.0,
    val comment: String? = null,
    val visibility: String = "NEIGHBORHOOD",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class StoreRatingSummary(
    val storeId: String,
    val reviewCount: Int,
    val overallAverage: Double,
    val serviceQualityAverage: Double,
    val offerCredibilityAverage: Double,
    val myReview: StoreReview?
)

fun computeStoreRatingSummaries(
    reviews: List<StoreReview>,
    currentUserId: String
): Map<String, StoreRatingSummary> {
    return reviews.groupBy { it.storeId }.mapValues { (storeId, storeReviews) ->
        val qualityAvg = storeReviews.map { it.serviceQualityRating.coerceIn(1, 5) }.average()
        val credibilityAvg = storeReviews.map { it.offerCredibilityRating.coerceIn(1, 5) }.average()
        val overallAvg = storeReviews.map { review ->
            if (review.overallRating in 1.0..5.0) {
                review.overallRating
            } else {
                (review.serviceQualityRating.coerceIn(1, 5) + review.offerCredibilityRating.coerceIn(1, 5)) / 2.0
            }
        }.average()
        StoreRatingSummary(
            storeId = storeId,
            reviewCount = storeReviews.size,
            overallAverage = overallAvg,
            serviceQualityAverage = qualityAvg,
            offerCredibilityAverage = credibilityAvg,
            myReview = storeReviews.firstOrNull { it.userId == currentUserId }
        )
    }
}

fun formatRatingScore(score: Double): String = String.format(java.util.Locale.US, "%.1f", score)

enum class DeliveryOrderType(val arabicLabel: String) {
    GROCERY("بقالة وسوبرماركت"),
    RESTAURANT("مطعم"),
    PHARMACY("صيدلية"),
    CARGO_FURNITURE("نقل بضائع وأثاث"),
    SPECIAL_ITEM("غرض خاص"),
    OTHER("أخرى");

    companion object {
        fun fromRaw(raw: String): DeliveryOrderType =
            entries.firstOrNull { it.name == raw } ?: OTHER
    }
}

enum class DeliveryVehicleType(val arabicLabel: String, val capacityHint: String) {
    MOTORCYCLE("دراجة نارية", "سريع للطلبات الخفيفة"),
    SEDAN("سيارة سياحية", "مريح للطرود والأكياس"),
    SUZUKI("سوزوكي", "مناسب للأغراض المتوسطة"),
    VAN("فان", "مساحة مغلقة وآمنة"),
    PICKUP("بيك أب", "للأثاث والبضائع"),
    TRUCK("شاحنة", "للنقل والحمولات الكبيرة");

    companion object {
        fun fromRaw(raw: String): DeliveryVehicleType =
            entries.firstOrNull { it.name == raw } ?: MOTORCYCLE
    }
}

enum class DeliveryZone(val arabicLabel: String) {
    INSIDE_HAY_AL_WURUD("داخل حي الورود"),
    QUDSAYA_SUBURB("ضاحية قدسيا"),
    QUDSAYA_TOWN("قدسيا البلد"),
    DUMMAR_PROJECT("مشروع دمر"),
    OTHER_DAMASCUS("مناطق أخرى في دمشق");

    companion object {
        fun fromRaw(raw: String): DeliveryZone =
            entries.firstOrNull { it.name == raw } ?: INSIDE_HAY_AL_WURUD
    }
}

enum class DeliveryStatus(val arabicLabel: String) {
    PENDING("قيد الطلب والتجهيز"),
    COMPLETED("تم التوصيل"),
    CANCELLED("ملغي");

    companion object {
        fun fromRaw(raw: String): DeliveryStatus =
            entries.firstOrNull { it.name == raw } ?: PENDING
    }
}

@Keep
@IgnoreExtraProperties
data class DeliveryRequest(
    @DocumentId val id: String = "",
    val userId: String = "",
    val orderType: String = DeliveryOrderType.GROCERY.name,
    val vehicleType: String = DeliveryVehicleType.MOTORCYCLE.name,
    val pickupLocation: String = "",
    val orderDetails: String = "",
    val deliveryZone: String = DeliveryZone.INSIDE_HAY_AL_WURUD.name,
    val detailedAddress: String = "",
    val gpsMapsUrl: String? = null,
    val customerName: String = "",
    val customerPhone: String = "",
    val status: String = DeliveryStatus.PENDING.name,
    val visibility: String = "NEIGHBORHOOD",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val parsedOrderType: DeliveryOrderType get() = DeliveryOrderType.fromRaw(orderType)
    val parsedVehicleType: DeliveryVehicleType get() = DeliveryVehicleType.fromRaw(vehicleType)
    val parsedDeliveryZone: DeliveryZone get() = DeliveryZone.fromRaw(deliveryZone)
    val parsedStatus: DeliveryStatus get() = DeliveryStatus.fromRaw(status)
}

data class DeliveryPrefill(
    val pickupLocation: String,
    val orderType: DeliveryOrderType,
    val requestNonce: Long = System.nanoTime()
)

fun ServiceProvider.supportsStoreDelivery(): Boolean {
    if (parsedCategory == ServiceCategory.HEALTH || parsedCategory == ServiceCategory.FOOD) {
        return true
    }
    val combined = "$name $specialty"
    return listOf(
        "صيدلية", "مطعم", "فرن", "معجنات", "بقالة", "سوبرماركت", "ماركت",
        "سمانة", "محل", "متجر", "مكتبة", "حلويات", "مخبز"
    ).any { combined.contains(it) }
}

fun ServiceProvider.suggestedDeliveryOrderType(): DeliveryOrderType {
    val combined = "$name $specialty"
    return when {
        combined.contains("صيدلية") || parsedCategory == ServiceCategory.HEALTH ->
            DeliveryOrderType.PHARMACY
        combined.contains("سمانة") || combined.contains("بقالة") || combined.contains("سوبرماركت") || combined.contains("ماركت") ->
            DeliveryOrderType.GROCERY
        combined.contains("مطعم") || combined.contains("فرن") || combined.contains("معجنات") || combined.contains("حلويات") || parsedCategory == ServiceCategory.FOOD ->
            DeliveryOrderType.RESTAURANT
        else -> DeliveryOrderType.SPECIAL_ITEM
    }
}

fun NeighborhoodStore.suggestedDeliveryOrderType(): DeliveryOrderType {
    val combined = "$name ${description.orEmpty()}"
    return when {
        combined.contains("صيدلية") -> DeliveryOrderType.PHARMACY
        combined.contains("مطعم") || combined.contains("فرن") -> DeliveryOrderType.RESTAURANT
        combined.contains("بقالة") || combined.contains("سمانة") || combined.contains("سوبرماركت") ->
            DeliveryOrderType.GROCERY
        else -> DeliveryOrderType.SPECIAL_ITEM
    }
}

data class DeliveryFareEstimate(
    val headlinePrice: String,
    val isStartingEstimate: Boolean,
    val zoneDetails: String?,
    val disclaimerNote: String?
)

fun calculateEstimatedDeliveryFare(
    vehicleType: DeliveryVehicleType,
    deliveryZone: DeliveryZone
): DeliveryFareEstimate {
    return when (vehicleType) {
        DeliveryVehicleType.MOTORCYCLE -> {
            val price = when (deliveryZone) {
                DeliveryZone.INSIDE_HAY_AL_WURUD -> "10,000 ل.س"
                DeliveryZone.QUDSAYA_SUBURB,
                DeliveryZone.QUDSAYA_TOWN,
                DeliveryZone.DUMMAR_PROJECT -> "15,000 ل.س"
                DeliveryZone.OTHER_DAMASCUS -> "25,000 ل.س"
            }
            DeliveryFareEstimate(
                headlinePrice = price,
                isStartingEstimate = false,
                zoneDetails = "التعرفة التقديرية لمنطقة: ${deliveryZone.arabicLabel}",
                disclaimerNote = null
            )
        }

        DeliveryVehicleType.SEDAN -> {
            val price = when (deliveryZone) {
                DeliveryZone.INSIDE_HAY_AL_WURUD -> "20,000 ل.س"
                DeliveryZone.QUDSAYA_SUBURB,
                DeliveryZone.QUDSAYA_TOWN,
                DeliveryZone.DUMMAR_PROJECT -> "30,000 ل.س"
                DeliveryZone.OTHER_DAMASCUS -> "45,000 ل.س"
            }
            DeliveryFareEstimate(
                headlinePrice = price,
                isStartingEstimate = false,
                zoneDetails = "التعرفة التقديرية لمنطقة: ${deliveryZone.arabicLabel}",
                disclaimerNote = null
            )
        }

        DeliveryVehicleType.SUZUKI,
        DeliveryVehicleType.VAN -> {
            val zoneText = if (deliveryZone == DeliveryZone.INSIDE_HAY_AL_WURUD) {
                "داخل حي الورود (وقد تزيد حسب الوجهة)"
            } else {
                "تزيد حسب المنطقة (${deliveryZone.arabicLabel})"
            }
            DeliveryFareEstimate(
                headlinePrice = "تبدأ من 50,000 ل.س",
                isStartingEstimate = true,
                zoneDetails = zoneText,
                disclaimerNote = "السعر تقديري وقد يختلف حسب المسافة وعدد العمال"
            )
        }

        DeliveryVehicleType.PICKUP -> {
            val zoneText = if (deliveryZone == DeliveryZone.INSIDE_HAY_AL_WURUD) {
                "داخل حي الورود (وقد تزيد حسب الوجهة)"
            } else {
                "تزيد حسب المنطقة (${deliveryZone.arabicLabel})"
            }
            DeliveryFareEstimate(
                headlinePrice = "تبدأ من 80,000 ل.س",
                isStartingEstimate = true,
                zoneDetails = zoneText,
                disclaimerNote = "السعر تقديري وقد يختلف حسب المسافة وعدد العمال"
            )
        }

        DeliveryVehicleType.TRUCK -> {
            val zoneText = if (deliveryZone == DeliveryZone.INSIDE_HAY_AL_WURUD) {
                "داخل حي الورود (وقد تزيد حسب الوجهة)"
            } else {
                "تزيد حسب المنطقة (${deliveryZone.arabicLabel})"
            }
            DeliveryFareEstimate(
                headlinePrice = "تبدأ من 120,000 ل.س",
                isStartingEstimate = true,
                zoneDetails = zoneText,
                disclaimerNote = "السعر تقديري وقد يختلف حسب المسافة وعدد العمال"
            )
        }
    }
}

/**
 * رقم واتساب الإدارة المسؤول عن استقبال طلبات التوصيل وطلبات انضمام السائقين.
 * استبدل هذا الرقم برقم الإدارة الفعلي عند النشر إذا لزم الأمر.
 */
const val DELIVERY_ADMIN_WHATSAPP_PHONE = "0933125102"

fun buildDeliveryOrderWhatsAppMessage(
    orderType: DeliveryOrderType,
    vehicleType: DeliveryVehicleType,
    pickupLocation: String,
    orderDetails: String,
    deliveryZone: DeliveryZone,
    detailedAddress: String,
    gpsMapsUrl: String?,
    customerName: String,
    customerPhone: String,
    fareEstimate: DeliveryFareEstimate
): String = buildString {
    appendLine("📦 *طلب توصيل ونقل سريع - حي الورود*")
    appendLine("━━━━━━━━━━━━━━")
    appendLine("• *نوع الطلب:* ${orderType.arabicLabel}")
    appendLine("• *المركبة المطلوبة:* ${vehicleType.arabicLabel}")
    appendLine("• *مكان الاستلام:* $pickupLocation")
    appendLine("• *تفاصيل الطلب:* $orderDetails")
    appendLine("• *المنطقة:* ${deliveryZone.arabicLabel}")
    appendLine("• *العنوان:* ${detailedAddress.ifBlank { "محدد عبر رابط GPS" }}")
    appendLine("• *رابط موقع الـ GPS:* ${gpsMapsUrl?.takeIf { it.isNotBlank() } ?: "غير مرفق"}")
    appendLine("• *اسم العميل:* $customerName")
    appendLine("• *رقم العميل:* $customerPhone")
    appendLine("• *التكلفة التقديرية المحسوبة:* ${fareEstimate.headlinePrice}")
    fareEstimate.disclaimerNote?.let { note ->
        appendLine("  ($note)")
    }
    append("━━━━━━━━━━━━━━")
}

fun buildDriverRegistrationWhatsAppMessage(
    fullName: String,
    phone: String,
    vehicleType: DeliveryVehicleType
): String = buildString {
    appendLine("🚚 *طلب انضمام كسائق توصيل أو نقل - حي الورود*")
    appendLine("━━━━━━━━━━━━━━")
    appendLine("• *الاسم الكامل:* $fullName")
    appendLine("• *رقم الهاتف:* $phone")
    appendLine("• *نوع المركبة:* ${vehicleType.arabicLabel}")
    append("━━━━━━━━━━━━━━")
}

