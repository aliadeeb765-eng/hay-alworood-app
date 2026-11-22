package com.example

import com.example.data.local.HayAlWurudStarterCatalog
import com.example.data.model.CraftFilterCategory
import com.example.data.model.NeighborhoodServicesCatalog
import com.example.data.model.StoreCategory
import com.example.data.model.toNeighborhoodService
import com.example.data.model.toServiceProvider
import org.junit.Assert.assertEquals
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
}
