package com.example

import com.example.data.local.HayAlWurudStarterCatalog
import com.example.data.location.LocationHelper
import com.example.data.model.CraftFilterCategory
import com.example.data.model.DELIVERY_ADMIN_WHATSAPP_PHONE
import com.example.data.model.DeliveryOrderType
import com.example.data.model.DeliveryVehicleType
import com.example.data.model.DeliveryZone
import com.example.data.model.NeighborhoodServicesCatalog
import com.example.data.model.PhoneEntry
import com.example.data.model.StoreCategory
import com.example.data.model.StoreReview
import com.example.data.model.buildDeliveryOrderWhatsAppMessage
import com.example.data.model.buildDriverRegistrationWhatsAppMessage
import com.example.data.model.calculateEstimatedDeliveryFare
import com.example.data.model.computeStoreRatingSummaries
import com.example.data.model.suggestedDeliveryOrderType
import com.example.data.model.supportsStoreDelivery
import com.example.data.model.toNeighborhoodService
import com.example.data.model.toServiceProvider
import com.example.data.update.AppUpdateChecker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun `neighborhood services catalog maps cleanly to and from ServiceProvider`() {
        val initial = NeighborhoodServicesCatalog.initialServices
        assertTrue(initial.size >= 5)

        val first = initial.first()
        val provider = first.toServiceProvider(uid = "user_1", authorName = "أحمد")
        val roundTrip = provider.toNeighborhoodService(
            recommendationsCount = first.recommendationsCount,
            availability = first.availability
        )

        assertEquals(first.id, roundTrip.id)
        assertEquals(first.name, roundTrip.name)
        assertEquals(first.profession, roundTrip.profession)
        assertEquals(first.category, roundTrip.category)
        assertEquals(first.sector, roundTrip.sector)
        assertEquals(first.phoneNumber, roundTrip.phoneNumber)
    }

    @Test
    fun `craft filter categories distinguish plumbing electrical and maintenance`() {
        val services = NeighborhoodServicesCatalog.initialServices

        val plumbing = services.filter { CraftFilterCategory.PLUMBING.matches(it) }
        assertEquals(listOf("guest_s_2"), plumbing.map { it.id })

        val electrical = services.filter { CraftFilterCategory.ELECTRICAL.matches(it) }
        assertEquals(listOf("guest_s_1"), electrical.map { it.id })

        val maintenance = services.filter { CraftFilterCategory.MAINTENANCE.matches(it) }
        assertEquals(listOf("guest_s_6"), maintenance.map { it.id })

        val all = services.filter { CraftFilterCategory.ALL.matches(it) }
        assertEquals(services.size, all.size)
    }

    @Test
    fun `starter stores and products cover clothing shoes cosmetics stationery and tech accessories`() {
        val stores = HayAlWurudStarterCatalog.starterStores("uid_1", "صاحب محل")
        val categories = stores.map { it.parsedCategory }.toSet()
        assertTrue(categories.contains(StoreCategory.CLOTHING))
        assertTrue(categories.contains(StoreCategory.SHOES))
        assertTrue(categories.contains(StoreCategory.COSMETICS))
        assertTrue(categories.contains(StoreCategory.STATIONERY))
        assertTrue(categories.contains(StoreCategory.TECH_ACCESSORIES))

        val products = HayAlWurudStarterCatalog.starterStoreProducts("uid_1")
        assertTrue(products.any { it.onOffer && it.inStock })
        val storeIds = stores.map { it.id }.toSet()
        assertTrue(products.all { it.storeId in storeIds })
    }

    @Test
    fun `app update checker parses valid version json and checks version code`() {
        val rawJson = """
            {
              "latestVersion": 2,
              "apkUrl": "https://github.com/aliadeeb765-eng/hay-alworood-app/raw/main/my-app.apk"
            }
        """.trimIndent()

        val info = AppUpdateChecker.parseVersionJson(rawJson)
        assertNotNull(info)
        assertEquals(2, info!!.latestVersion)
        assertTrue(AppUpdateChecker.isUpdateAvailable(info, currentVersionCode = 1))
        assertFalse(AppUpdateChecker.isUpdateAvailable(info, currentVersionCode = 2))
        assertFalse(AppUpdateChecker.isUpdateAvailable(info, currentVersionCode = 3))

        assertNull(AppUpdateChecker.parseVersionJson("""{"latestVersion": 0, "apkUrl": "https://github.com"}"""))
        assertNull(AppUpdateChecker.parseVersionJson("""{"latestVersion": 2, "apkUrl": "http://insecure.example.com/a.apk"}"""))
        assertNull(AppUpdateChecker.parseVersionJson("not-json"))
    }

    @Test
    fun `computeStoreRatingSummaries aggregates quality credibility and overall stars`() {
        val reviews = listOf(
            StoreReview(
                id = "s1_u1",
                storeId = "s1",
                userId = "u1",
                reviewerName = "أم محمود",
                serviceQualityRating = 5,
                offerCredibilityRating = 5,
                overallRating = 5.0
            ),
            StoreReview(
                id = "s1_u2",
                storeId = "s1",
                userId = "u2",
                reviewerName = "أبو علي",
                serviceQualityRating = 3,
                offerCredibilityRating = 4,
                overallRating = 3.5
            )
        )

        val summaries = computeStoreRatingSummaries(reviews, currentUserId = "u2")
        val s1 = summaries["s1"]
        assertNotNull(s1)
        assertEquals(2, s1!!.reviewCount)
        assertEquals(4.0, s1.serviceQualityAverage, 0.01)
        assertEquals(4.5, s1.offerCredibilityAverage, 0.01)
        assertEquals(4.25, s1.overallAverage, 0.01)
        assertEquals("s1_u2", s1.myReview?.id)
        assertTrue(HayAlWurudStarterCatalog.starterStoreReviews().isNotEmpty())
    }

    @Test
    fun `location helper formats google maps url with US decimal coordinates`() {
        val url = LocationHelper.formatGoogleMapsUrl(33.5321, 36.2245)
        assertEquals("https://maps.google.com/?q=33.532100,36.224500", url)
    }

    @Test
    fun `service providers and stores suggest matching delivery order types`() {
        val services = HayAlWurudStarterCatalog.starterServices("u1", "جاري")
        val pharmacy = services.first { it.name.contains("صيدلية") }
        val plumber = services.first { it.specialty.contains("سباكة") }

        assertTrue(pharmacy.supportsStoreDelivery())
        assertEquals(DeliveryOrderType.PHARMACY, pharmacy.suggestedDeliveryOrderType())
        assertFalse(plumber.supportsStoreDelivery())
        assertTrue(HayAlWurudStarterCatalog.starterDeliveryRequests().isNotEmpty())
    }

    @Test
    fun `instant delivery fare calculator matches vehicle and zone pricing table`() {
        // Motorcycle
        assertEquals("10,000 ل.س", calculateEstimatedDeliveryFare(DeliveryVehicleType.MOTORCYCLE, DeliveryZone.INSIDE_HAY_AL_WURUD).headlinePrice)
        assertEquals("15,000 ل.س", calculateEstimatedDeliveryFare(DeliveryVehicleType.MOTORCYCLE, DeliveryZone.QUDSAYA_SUBURB).headlinePrice)
        assertEquals("15,000 ل.س", calculateEstimatedDeliveryFare(DeliveryVehicleType.MOTORCYCLE, DeliveryZone.QUDSAYA_TOWN).headlinePrice)
        assertEquals("15,000 ل.س", calculateEstimatedDeliveryFare(DeliveryVehicleType.MOTORCYCLE, DeliveryZone.DUMMAR_PROJECT).headlinePrice)
        assertEquals("25,000 ل.س", calculateEstimatedDeliveryFare(DeliveryVehicleType.MOTORCYCLE, DeliveryZone.OTHER_DAMASCUS).headlinePrice)

        // Sedan
        assertEquals("20,000 ل.س", calculateEstimatedDeliveryFare(DeliveryVehicleType.SEDAN, DeliveryZone.INSIDE_HAY_AL_WURUD).headlinePrice)
        assertEquals("30,000 ل.س", calculateEstimatedDeliveryFare(DeliveryVehicleType.SEDAN, DeliveryZone.QUDSAYA_SUBURB).headlinePrice)
        assertEquals("30,000 ل.س", calculateEstimatedDeliveryFare(DeliveryVehicleType.SEDAN, DeliveryZone.QUDSAYA_TOWN).headlinePrice)
        assertEquals("30,000 ل.س", calculateEstimatedDeliveryFare(DeliveryVehicleType.SEDAN, DeliveryZone.DUMMAR_PROJECT).headlinePrice)
        assertEquals("45,000 ل.س", calculateEstimatedDeliveryFare(DeliveryVehicleType.SEDAN, DeliveryZone.OTHER_DAMASCUS).headlinePrice)

        // Cargo & transport vehicles with starting prices and disclaimer note
        val expectedNote = "السعر تقديري وقد يختلف حسب المسافة وعدد العمال"
        val suzuki = calculateEstimatedDeliveryFare(DeliveryVehicleType.SUZUKI, DeliveryZone.INSIDE_HAY_AL_WURUD)
        assertEquals("تبدأ من 50,000 ل.س", suzuki.headlinePrice)
        assertEquals(expectedNote, suzuki.disclaimerNote)

        val van = calculateEstimatedDeliveryFare(DeliveryVehicleType.VAN, DeliveryZone.DUMMAR_PROJECT)
        assertEquals("تبدأ من 50,000 ل.س", van.headlinePrice)
        assertEquals(expectedNote, van.disclaimerNote)

        val pickup = calculateEstimatedDeliveryFare(DeliveryVehicleType.PICKUP, DeliveryZone.INSIDE_HAY_AL_WURUD)
        assertEquals("تبدأ من 80,000 ل.س", pickup.headlinePrice)
        assertEquals(expectedNote, pickup.disclaimerNote)

        val truck = calculateEstimatedDeliveryFare(DeliveryVehicleType.TRUCK, DeliveryZone.OTHER_DAMASCUS)
        assertEquals("تبدأ من 120,000 ل.س", truck.headlinePrice)
        assertEquals(expectedNote, truck.disclaimerNote)

        // Custom vehicle or custom zone via "أخرى"
        val customVehicleFare = calculateEstimatedDeliveryFare(DeliveryVehicleType.OTHER, DeliveryZone.INSIDE_HAY_AL_WURUD)
        assertEquals("يتم تحديد السعر بدقة بالاتفاق عبر واتساب", customVehicleFare.headlinePrice)

        val customZoneFare = calculateEstimatedDeliveryFare(DeliveryVehicleType.MOTORCYCLE, DeliveryZone.OTHER)
        assertEquals("يتم تحديد السعر بدقة بالاتفاق عبر واتساب", customZoneFare.headlinePrice)
    }

    @Test
    fun `whatsapp message builders include custom labels and multiple mobile and landline phones`() {
        assertEquals("0933125102", DELIVERY_ADMIN_WHATSAPP_PHONE)

        val phones = listOf(
            PhoneEntry("0944112233", "موبايل"),
            PhoneEntry("0112233445", "هاتف أرضي")
        )
        val roundTrip = PhoneEntry.parseList(PhoneEntry.serializeList(phones))
        assertEquals(phones, roundTrip)

        val fare = calculateEstimatedDeliveryFare(DeliveryVehicleType.PICKUP, DeliveryZone.QUDSAYA_SUBURB)
        val orderMessage = buildDeliveryOrderWhatsAppMessage(
            orderTypeLabel = "نقل بضائع وأثاث",
            vehicleTypeLabel = "بيك أب",
            pickupLocation = "مفروشات النور",
            orderDetails = "خزانة غرف نوم",
            deliveryZoneLabel = "ضاحية قدسيا",
            detailedAddress = "الجزيرة ب2 طابق 1",
            gpsMapsUrl = "https://maps.google.com/?q=33.532100,36.224500",
            customerName = "مازن الدمشقي",
            customerPhones = phones,
            fareEstimate = fare
        )

        assertTrue(orderMessage.contains("نقل بضائع وأثاث"))
        assertTrue(orderMessage.contains("بيك أب"))
        assertTrue(orderMessage.contains("مفروشات النور"))
        assertTrue(orderMessage.contains("خزانة غرف نوم"))
        assertTrue(orderMessage.contains("ضاحية قدسيا"))
        assertTrue(orderMessage.contains("الجزيرة ب2 طابق 1"))
        assertTrue(orderMessage.contains("https://maps.google.com/?q=33.532100,36.224500"))
        assertTrue(orderMessage.contains("مازن الدمشقي"))
        assertTrue(orderMessage.contains("0944112233 (موبايل)"))
        assertTrue(orderMessage.contains("0112233445 (هاتف أرضي)"))
        assertTrue(orderMessage.contains("تبدأ من 80,000 ل.س"))

        val driverMessage = buildDriverRegistrationWhatsAppMessage(
            fullName = "أيمن كمال",
            phones = listOf(PhoneEntry("0966778899", "موبايل")),
            vehicleTypeLabel = "فان"
        )

        assertTrue(driverMessage.contains("أيمن كمال"))
        assertTrue(driverMessage.contains("0966778899 (موبايل)"))
        assertTrue(driverMessage.contains("فان"))
    }
}
