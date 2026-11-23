package com.example.data.local

import com.example.data.model.CommunityPost
import com.example.data.model.NeighborhoodServicesCatalog
import com.example.data.model.NeighborhoodStore
import com.example.data.model.PostCategory
import com.example.data.model.PostStatus
import com.example.data.model.ServiceProvider
import com.example.data.model.ServiceRecommendation
import com.example.data.model.StoreCategory
import com.example.data.model.StoreProduct
import com.example.data.model.StoreReview
import com.example.data.model.UtilityStatus
import com.example.data.model.UtilityType
import com.example.data.model.UtilityUpdate
import com.example.data.model.toServiceProvider
import com.google.firebase.Timestamp

object HayAlWurudStarterCatalog {

    fun starterUtilities(uid: String, authorName: String, now: Timestamp? = Timestamp.now()): List<UtilityUpdate> = listOf(
        UtilityUpdate(
            id = "guest_u_1",
            userId = uid,
            authorName = authorName,
            utilityType = UtilityType.WATER.name,
            sector = "الساحة الرئيسية",
            status = UtilityStatus.AVAILABLE.name,
            note = "الضخ شغال الآن على الساحة والحارة التحتانية، يرجى الاقتصاد بالاستهلاك.",
            createdAt = now,
            updatedAt = now
        ),
        UtilityUpdate(
            id = "guest_u_2",
            userId = uid,
            authorName = authorName,
            utilityType = UtilityType.ELECTRICITY.name,
            sector = "حي الندى",
            status = UtilityStatus.SCHEDULED.name,
            note = "وصل الكهرباء حسب برنامج التقنين المعتاد، والأمبيرات متوفرة.",
            createdAt = now,
            updatedAt = now
        ),
        UtilityUpdate(
            id = "guest_u_3",
            userId = uid,
            authorName = authorName,
            utilityType = UtilityType.BREAD.name,
            sector = "شارع المدارس",
            status = UtilityStatus.AVAILABLE.name,
            note = "فرن الحي الآلي والمعتمدين مستمرين بالتوزيع والازدحام خفيف.",
            createdAt = now,
            updatedAt = now
        ),
        UtilityUpdate(
            id = "guest_u_4",
            userId = uid,
            authorName = authorName,
            utilityType = UtilityType.GAS.name,
            sector = "منطقة الجبل",
            status = UtilityStatus.SCHEDULED.name,
            note = "توزيع الدفعة الجديدة عند المعتمدين في الحارة الوسطى.",
            createdAt = now,
            updatedAt = now
        ),
        UtilityUpdate(
            id = "guest_u_5",
            userId = uid,
            authorName = authorName,
            utilityType = UtilityType.TRANSPORT.name,
            sector = "الساحة الرئيسية",
            status = UtilityStatus.AVAILABLE.name,
            note = "سرافيس خط حي الورود - البرامكة متواجدة في الموقف الرئيسي.",
            createdAt = now,
            updatedAt = now
        )
    )

    fun starterServices(uid: String, authorName: String, now: Timestamp? = Timestamp.now()): List<ServiceProvider> =
        NeighborhoodServicesCatalog.initialServices.map { service ->
            service.toServiceProvider(uid = uid, authorName = authorName, now = now)
        }

    fun starterPosts(uid: String, authorName: String, now: Timestamp? = Timestamp.now()): List<CommunityPost> = listOf(
        CommunityPost(
            id = "guest_p_1",
            userId = uid,
            authorName = authorName,
            title = "حملة نظافة وتشجير حارة الندى يوم الجمعة",
            description = "بدعوة من شباب حي الورود، نجتمع الساعة 9 صباحاً أمام جامع الهدى لتنظيف الأرصفة وزراعة شتول ياسمين وورد جوري.",
            category = PostCategory.NOTICE.name,
            sector = "محيط جامع الهدى",
            contactNote = "التجمع أمام مدخل جامع الهدى",
            status = PostStatus.OPEN.name,
            createdAt = now,
            updatedAt = now
        ),
        CommunityPost(
            id = "guest_p_2",
            userId = uid,
            authorName = authorName,
            title = "وجدنا مفاتيح بيت مع ميدالية خضراء قرب الساحة",
            description = "تم العثور على سلسلة مفاتيح بجانب موقف السرافيس في الساحة الرئيسية، وهي محفوظة عند سمانة أبو خليل.",
            category = PostCategory.LOST_FOUND.name,
            sector = "الساحة الرئيسية",
            contactNote = "موجودة أمانة لدى سمانة أبو خليل",
            status = PostStatus.OPEN.name,
            createdAt = now,
            updatedAt = now
        ),
        CommunityPost(
            id = "guest_p_3",
            userId = uid,
            authorName = authorName,
            title = "مكدوس ومونة بيتية شغل يد نظيف",
            description = "متوفر مكدوس باذنجان بلدي بالزيت الأصلي والجوز، وكبة مقلية ومفرزة حسب الطلب لأهالي الحي.",
            category = PostCategory.MARKET.name,
            sector = "منطقة الجبل",
            priceText = "أسعار مراعية لأهل الحي",
            contactNote = "التوصيل مجاني داخل حي الورود",
            status = PostStatus.OPEN.name,
            createdAt = now,
            updatedAt = now
        ),
        CommunityPost(
            id = "guest_p_4",
            userId = uid,
            authorName = authorName,
            title = "دروس تقوية مجانية لطلاب التاسع والبكالوريا",
            description = "مبادرة من أساتذة حي الورود لتقديم حصص مراجعة في الرياضيات واللغة العربية عصراً في قاعة المركز الثقافي.",
            category = PostCategory.NOTICE.name,
            sector = "شارع المدارس",
            contactNote = "التسجيل يومياً من 4 إلى 6 مساءً",
            status = PostStatus.OPEN.name,
            createdAt = now,
            updatedAt = now
        ),
        CommunityPost(
            id = "guest_p_5",
            userId = uid,
            authorName = "أبو سامر",
            title = "مطلوب سلم ألمنيوم لساعة لتفقد خزان السطح",
            description = "نحتاج استعارة سلم طويل لمدة ساعة فقط لتثبيت عوامة خزان المياه وإعادته فوراً، جزاكم الله خيراً.",
            category = PostCategory.HELP.name,
            sector = "منطقة الجبل",
            contactNote = "البناية المقابلة لفرن الجبل - الطابق الثالث",
            status = PostStatus.OPEN.name,
            createdAt = now,
            updatedAt = now
        ),
        CommunityPost(
            id = "guest_p_6",
            userId = uid,
            authorName = "شباب الحارة التحتانية",
            title = "فزعة إيصال أسطوانات غاز ومشتريات لكبار السن",
            description = "شباب المتطوعين جاهزون يومياً بعد العصر لمساعدة كبار السن وذوي الاحتياجات بنقل أسطوانات الغاز وأكياس الطحين مجاناً.",
            category = PostCategory.HELP.name,
            sector = "الحارة التحتانية",
            contactNote = "التنسيق عبر نقطة التجمع عند سمانة البركة",
            status = PostStatus.OPEN.name,
            createdAt = now,
            updatedAt = now
        ),
        CommunityPost(
            id = "guest_p_7",
            userId = uid,
            authorName = "أم علاء",
            title = "متوفر طقم كتب مدرسية للصف السادس بحالة ممتازة",
            description = "الكتب نظيفة ومجلدة بالكامل وتُهدى مجاناً لأي عائلة محتاجة لها في الحي.",
            category = PostCategory.HELP.name,
            sector = "شارع المدارس",
            contactNote = "متوفرة قرب مدرسة حي الورود الأولى",
            status = PostStatus.OPEN.name,
            createdAt = now,
            updatedAt = now
        )
    )

    fun starterRecommendations(): List<ServiceRecommendation> = listOf(
        ServiceRecommendation(id = "rec_1", userId = "neighbor_1", serviceId = "guest_s_1", authorName = "أبو محمود"),
        ServiceRecommendation(id = "rec_2", userId = "neighbor_2", serviceId = "guest_s_1", authorName = "أم سليم"),
        ServiceRecommendation(id = "rec_3", userId = "neighbor_3", serviceId = "guest_s_1", authorName = "خالد"),
        ServiceRecommendation(id = "rec_4", userId = "neighbor_1", serviceId = "guest_s_3", authorName = "أبو محمود"),
        ServiceRecommendation(id = "rec_5", userId = "neighbor_2", serviceId = "guest_s_4", authorName = "أم سليم")
    )

    fun starterStores(uid: String, ownerName: String, now: Timestamp? = Timestamp.now()): List<NeighborhoodStore> = listOf(
        NeighborhoodStore(
            id = "guest_store_1",
            userId = uid,
            ownerName = ownerName,
            name = "بوتيك الياسمين للألبسة",
            category = StoreCategory.CLOTHING.name,
            sector = "الساحة الرئيسية",
            phone = "0933445566",
            whatsappPhone = "0933445566",
            workingHours = "10 صباحاً - 9 مساءً",
            description = "تشكيلة ألبسة نسائية ورجالية وبيجامات قطنية بأسعار مدروسة لأهل الحي.",
            storeOpen = true,
            createdAt = now,
            updatedAt = now
        ),
        NeighborhoodStore(
            id = "guest_store_2",
            userId = uid,
            ownerName = ownerName,
            name = "أحذية الشام العصرية",
            category = StoreCategory.SHOES.name,
            sector = "شارع المدارس",
            phone = "0944112233",
            whatsappPhone = "0944112233",
            workingHours = "9:30 صباحاً - 8:30 مساءً",
            description = "أحذية رياضية ومدرسية ورسمية، جلد طبيعي مع كفالة.",
            storeOpen = true,
            createdAt = now,
            updatedAt = now
        ),
        NeighborhoodStore(
            id = "guest_store_3",
            userId = uid,
            ownerName = ownerName,
            name = "مركز لمسة جمال للمكياج والعطور",
            category = StoreCategory.COSMETICS.name,
            sector = "حي الندى",
            phone = "0955223344",
            whatsappPhone = "0955223344",
            workingHours = "10 صباحاً - 9 مساءً",
            description = "مستحضرات تجميل أصلية، عناية بالبشرة، وعطور مركزة ثابتة.",
            storeOpen = true,
            createdAt = now,
            updatedAt = now
        ),
        NeighborhoodStore(
            id = "guest_store_4",
            userId = uid,
            ownerName = ownerName,
            name = "مكتبة النور والقرطاسية",
            category = StoreCategory.STATIONERY.name,
            sector = "شارع المدارس",
            phone = "0966778899",
            whatsappPhone = "0966778899",
            workingHours = "7:30 صباحاً - 8 مساءً",
            description = "دفاتر مدرسية، حقائب، طباعة وتصوير أبحاث وملخصات طلابية.",
            storeOpen = true,
            createdAt = now,
            updatedAt = now
        ),
        NeighborhoodStore(
            id = "guest_store_5",
            userId = uid,
            ownerName = ownerName,
            name = "مركز التقنية للكمبيوتر والإكسسوارات",
            category = StoreCategory.TECH_ACCESSORIES.name,
            sector = "الساحة الرئيسية",
            phone = "0988119922",
            whatsappPhone = "0988119922",
            workingHours = "10 صباحاً - 9 مساءً",
            description = "لابتوبات، قطع تبديل، شواحن أصلية، فلاشات، وسماعات مع صيانة فورية.",
            storeOpen = true,
            createdAt = now,
            updatedAt = now
        )
    )

    fun starterStoreProducts(uid: String, now: Timestamp? = Timestamp.now()): List<StoreProduct> = listOf(
        StoreProduct(
            id = "guest_prod_1",
            storeId = "guest_store_1",
            userId = uid,
            storeName = "بوتيك الياسمين للألبسة",
            storeCategory = StoreCategory.CLOTHING.name,
            storeSector = "الساحة الرئيسية",
            storePhone = "0933445566",
            title = "جاكيت جينز مبطن شتوي",
            description = "قماش ثقيل مبطن بالفرو الداخلي مناسب للأجواء الباردة.",
            priceText = "185 ألف ل.س",
            originalPriceText = "230 ألف ل.س",
            onOffer = true,
            inStock = true,
            specsText = "المقاسات: M / L / XL",
            createdAt = now,
            updatedAt = now
        ),
        StoreProduct(
            id = "guest_prod_2",
            storeId = "guest_store_1",
            userId = uid,
            storeName = "بوتيك الياسمين للألبسة",
            storeCategory = StoreCategory.CLOTHING.name,
            storeSector = "الساحة الرئيسية",
            storePhone = "0933445566",
            title = "طقم بيجاما قطن صافي",
            description = "قطن سوري مريح بألوان متعددة.",
            priceText = "110 ألف ل.س",
            onOffer = false,
            inStock = true,
            specsText = "ألوان: كحلي، رمادي، عنابي",
            createdAt = now,
            updatedAt = now
        ),
        StoreProduct(
            id = "guest_prod_3",
            storeId = "guest_store_2",
            userId = uid,
            storeName = "أحذية الشام العصرية",
            storeCategory = StoreCategory.SHOES.name,
            storeSector = "شارع المدارس",
            storePhone = "0944112233",
            title = "حذاء رياضي مريح للمشي اليومي",
            description = "نعل طبي خفيف مقاوم للانزلاق ومناسب لطلوع الحي.",
            priceText = "165 ألف ل.س",
            originalPriceText = "210 ألف ل.س",
            onOffer = true,
            inStock = true,
            specsText = "النمر: 38 حتى 44",
            createdAt = now,
            updatedAt = now
        ),
        StoreProduct(
            id = "guest_prod_4",
            storeId = "guest_store_3",
            userId = uid,
            storeName = "مركز لمسة جمال للمكياج والعطور",
            storeCategory = StoreCategory.COSMETICS.name,
            storeSector = "حي الندى",
            storePhone = "0955223344",
            title = "مجموعة العناية بالبشرة والمكياج",
            description = "سيروم فيتامين سي مع كريم ترطيب وماسكارا ضد الماء.",
            priceText = "95 ألف ل.س",
            originalPriceText = "125 ألف ل.س",
            onOffer = true,
            inStock = true,
            specsText = "عرض نهاية الأسبوع • أصلية",
            createdAt = now,
            updatedAt = now
        ),
        StoreProduct(
            id = "guest_prod_5",
            storeId = "guest_store_4",
            userId = uid,
            storeName = "مكتبة النور والقرطاسية",
            storeCategory = StoreCategory.STATIONERY.name,
            storeSector = "شارع المدارس",
            storePhone = "0966778899",
            title = "حزمة قرطاسية ودفاتر مدرسية (12 قطعة)",
            description = "دفاتر سلك 100 ورقة مع أقلام وأدوات هندسية كاملة.",
            priceText = "65 ألف ل.س",
            originalPriceText = "85 ألف ل.س",
            onOffer = true,
            inStock = true,
            specsText = "للمرحلتين الإعدادية والثانوية",
            createdAt = now,
            updatedAt = now
        ),
        StoreProduct(
            id = "guest_prod_6",
            storeId = "guest_store_5",
            userId = uid,
            storeName = "مركز التقنية للكمبيوتر والإكسسوارات",
            storeCategory = StoreCategory.TECH_ACCESSORIES.name,
            storeSector = "الساحة الرئيسية",
            storePhone = "0988119922",
            title = "كيبورد وماوس لاسلكي + باد هدية",
            description = "بطارية طويلة الأمد وتوصيل فوري للكمبيوتر واللابتوب.",
            priceText = "140 ألف ل.س",
            originalPriceText = "175 ألف ل.س",
            onOffer = true,
            inStock = true,
            specsText = "كفالة 6 أشهر • USB 2.4GHz",
            createdAt = now,
            updatedAt = now
        ),
        StoreProduct(
            id = "guest_prod_7",
            storeId = "guest_store_5",
            userId = uid,
            storeName = "مركز التقنية للكمبيوتر والإكسسوارات",
            storeCategory = StoreCategory.TECH_ACCESSORIES.name,
            storeSector = "الساحة الرئيسية",
            storePhone = "0988119922",
            title = "شاحن لابتوب أصلي ووصلات Type-C سريعة",
            description = "يدعم الشحن السريع وحماية من تذبذب الكهرباء.",
            priceText = "120 ألف ل.س",
            onOffer = false,
            inStock = true,
            specsText = "65W PD • متوفر لجميع الموديلات",
            createdAt = now,
            updatedAt = now
        )
    )

    fun starterStoreReviews(now: Timestamp? = Timestamp.now()): List<StoreReview> = listOf(
        StoreReview(
            id = "guest_store_1_neighbor_1",
            storeId = "guest_store_1",
            userId = "neighbor_1",
            reviewerName = "أم محمود",
            serviceQualityRating = 5,
            offerCredibilityRating = 5,
            overallRating = 5.0,
            comment = "معاملة راقية جداً والأسعار والعروض مطابقة تماماً للمعلن بدون أي زيادة.",
            createdAt = now,
            updatedAt = now
        ),
        StoreReview(
            id = "guest_store_1_neighbor_2",
            storeId = "guest_store_1",
            userId = "neighbor_2",
            reviewerName = "خالد",
            serviceQualityRating = 5,
            offerCredibilityRating = 4,
            overallRating = 4.5,
            comment = "جودة الألبسة ممتازة والتبديل سهل إذا تغير المقاس.",
            createdAt = now,
            updatedAt = now
        ),
        StoreReview(
            id = "guest_store_2_neighbor_1",
            storeId = "guest_store_2",
            userId = "neighbor_1",
            reviewerName = "أبو سليم",
            serviceQualityRating = 4,
            offerCredibilityRating = 5,
            overallRating = 4.5,
            comment = "الحذاء الطبي مريح جداً وسعر العرض كان أوفر من السوق.",
            createdAt = now,
            updatedAt = now
        ),
        StoreReview(
            id = "guest_store_3_neighbor_3",
            storeId = "guest_store_3",
            userId = "neighbor_3",
            reviewerName = "سارة",
            serviceQualityRating = 5,
            offerCredibilityRating = 5,
            overallRating = 5.0,
            comment = "منتجات أصلية 100% وعرض نهاية الأسبوع حقيقي ومجرب.",
            createdAt = now,
            updatedAt = now
        ),
        StoreReview(
            id = "guest_store_4_neighbor_2",
            storeId = "guest_store_4",
            userId = "neighbor_2",
            reviewerName = "أم طارق",
            serviceQualityRating = 5,
            offerCredibilityRating = 5,
            overallRating = 5.0,
            comment = "تعاون كبير مع الطلاب وأسعار القرطاسية مدروسة.",
            createdAt = now,
            updatedAt = now
        ),
        StoreReview(
            id = "guest_store_5_neighbor_1",
            storeId = "guest_store_5",
            userId = "neighbor_1",
            reviewerName = "وسيم",
            serviceQualityRating = 5,
            offerCredibilityRating = 4,
            overallRating = 4.5,
            comment = "صيانة سريعة وكفالة حقيقية على الإكسسوارات.",
            createdAt = now,
            updatedAt = now
        )
    )

    fun starterDeliveryRequests(now: Timestamp? = Timestamp.now()): List<com.example.data.model.DeliveryRequest> = listOf(
        com.example.data.model.DeliveryRequest(
            id = "guest_del_1",
            userId = "neighbor_1",
            orderType = com.example.data.model.DeliveryOrderType.PHARMACY.name,
            vehicleType = com.example.data.model.DeliveryVehicleType.MOTORCYCLE.name,
            pickupLocation = "صيدلية الورود المناوبة",
            orderDetails = "مسكن ألم + شراب سعال أطفال، الدفع عند الاستلام.",
            deliveryZone = com.example.data.model.DeliveryZone.INSIDE_HAY_AL_WURUD.name,
            detailedAddress = "شارع المدارس، بناء رقم 12، طابق 3",
            gpsMapsUrl = "https://maps.google.com/?q=33.531800,36.244900",
            customerName = "أم محمود",
            customerPhone = "0933556677",
            status = com.example.data.model.DeliveryStatus.PENDING.name,
            createdAt = now,
            updatedAt = now
        ),
        com.example.data.model.DeliveryRequest(
            id = "guest_del_2",
            userId = "neighbor_2",
            orderType = com.example.data.model.DeliveryOrderType.CARGO_FURNITURE.name,
            vehicleType = com.example.data.model.DeliveryVehicleType.SUZUKI.name,
            pickupLocation = "مركز التقنية للكمبيوتر والإكسسوارات",
            orderDetails = "نقل طاولة كمبيوتر وكرتونة شاشة إلى ضاحية قدسيا.",
            deliveryZone = com.example.data.model.DeliveryZone.QUDSAYA_SUBURB.name,
            detailedAddress = "الجزيرة السابعة، بناء السلام، مدخل B",
            customerName = "طارق",
            customerPhone = "0944889911",
            status = com.example.data.model.DeliveryStatus.COMPLETED.name,
            createdAt = now,
            updatedAt = now
        )
    )
}
