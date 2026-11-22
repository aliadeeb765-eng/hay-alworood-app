package com.example.data.model

import com.google.firebase.Timestamp

enum class ServiceAvailability(val arabicLabel: String) {
    AVAILABLE_NOW("متاح الآن"),
    BY_APPOINTMENT("بحسب الموعد"),
    BUSY("مشغول حالياً")
}

/**
 * Clean UI and domain data class representing a neighborhood service provider or craftsman
 * in Hay Al-Wurud.
 */
data class NeighborhoodService(
    val id: String,
    val name: String,
    val profession: String,
    val category: ServiceCategory,
    val sector: String,
    val phoneNumber: String,
    val workingHours: String? = null,
    val description: String? = null,
    val availability: ServiceAvailability = ServiceAvailability.AVAILABLE_NOW,
    val recommendationsCount: Int = 0
)

fun NeighborhoodService.toServiceProvider(
    uid: String,
    authorName: String,
    now: Timestamp? = Timestamp.now()
): ServiceProvider = ServiceProvider(
    id = id,
    userId = uid,
    authorName = authorName,
    name = name,
    category = category.name,
    specialty = profession,
    sector = sector,
    workPhone = phoneNumber,
    workingHours = workingHours,
    notes = description,
    createdAt = now,
    updatedAt = now
)

fun ServiceProvider.toNeighborhoodService(
    recommendationsCount: Int = 0,
    availability: ServiceAvailability = ServiceAvailability.AVAILABLE_NOW
): NeighborhoodService = NeighborhoodService(
    id = id,
    name = name,
    profession = specialty,
    category = parsedCategory,
    sector = sector,
    phoneNumber = workPhone,
    workingHours = workingHours,
    description = notes,
    availability = availability,
    recommendationsCount = recommendationsCount
)

object NeighborhoodServicesCatalog {
    val initialServices: List<NeighborhoodService> = listOf(
        NeighborhoodService(
            id = "guest_s_1",
            name = "المعلم أبو أحمد للكهرباء",
            profession = "كهربائي منازل وإنفرتر وبطاريات",
            category = ServiceCategory.MAINTENANCE,
            sector = "الساحة الرئيسية",
            phoneNumber = "0933112244",
            workingHours = "8 صباحاً - 9 مساءً",
            description = "صيانة طوارئ للإنفرتر وألواح الطاقة الشمسية لأهالي الحي.",
            availability = ServiceAvailability.AVAILABLE_NOW,
            recommendationsCount = 3
        ),
        NeighborhoodService(
            id = "guest_s_2",
            name = "أبو طارق السنكري",
            profession = "سباكة وتمديدات صحية وخزانات",
            category = ServiceCategory.MAINTENANCE,
            sector = "الحارة التحتانية",
            phoneNumber = "0944556677",
            workingHours = "9 صباحاً - 8 مساءً",
            description = "تنظيف خزانات وتصليح مضخات المياه بسرعة وبأسعار شعبية.",
            availability = ServiceAvailability.AVAILABLE_NOW,
            recommendationsCount = 0
        ),
        NeighborhoodService(
            id = "guest_s_3",
            name = "صيدلية الورود المناوبة",
            profession = "صيدلية وقياس ضغط وسكر",
            category = ServiceCategory.HEALTH,
            sector = "محيط جامع الهدى",
            phoneNumber = "0113712233",
            workingHours = "مفتوحة حتى 12 ليلاً",
            description = "يتوفر استشارات دوائية ومستلزمات أطفال.",
            availability = ServiceAvailability.AVAILABLE_NOW,
            recommendationsCount = 1
        ),
        NeighborhoodService(
            id = "guest_s_4",
            name = "فرن ومعجنات البركة",
            profession = "خبز وصمون وفطائر صباحية",
            category = ServiceCategory.FOOD,
            sector = "شارع المدارس",
            phoneNumber = "0955889900",
            workingHours = "5:30 فجراً - 7 مساءً",
            description = "صفيحة دمشقية ومناقيش زعتر وجبنة يومياً.",
            availability = ServiceAvailability.AVAILABLE_NOW,
            recommendationsCount = 1
        ),
        NeighborhoodService(
            id = "guest_s_5",
            name = "أبو علي لنقل المياه والطلبات",
            profession = "توصيل غاز، مياه شرب، ونقل أغراض",
            category = ServiceCategory.TRANSPORT,
            sector = "منطقة الجبل",
            phoneNumber = "0966334455",
            workingHours = "7 صباحاً - 10 مساءً",
            description = "خدمة طلوع للجبل والأزقة الضيقة بدراجة شحن.",
            availability = ServiceAvailability.BY_APPOINTMENT,
            recommendationsCount = 0
        ),
        NeighborhoodService(
            id = "guest_s_6",
            name = "أبو خليل لتصليح الغسالات والبرادات",
            profession = "صيانة أدوات منزلية وتكييف",
            category = ServiceCategory.MAINTENANCE,
            sector = "حي الندى",
            phoneNumber = "0988776655",
            workingHours = "10 صباحاً - 7 مساءً",
            description = "كشف أعطال بالمنزل وتبديل قطع أصلية مع ضمان.",
            availability = ServiceAvailability.AVAILABLE_NOW,
            recommendationsCount = 2
        )
    )
}
