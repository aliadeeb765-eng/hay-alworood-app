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
