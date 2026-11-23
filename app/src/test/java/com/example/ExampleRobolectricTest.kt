package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp-xxhdpi")
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("حي الورود", appName)
    }

    @Test
    fun `main activity displays all five bottom tabs with starter content`() {
        composeTestRule.waitForIdle()

        // 1. الخدمات (Services) tab displays starter craftsmen immediately
        composeTestRule.onNodeWithTag("nav_services").assertIsDisplayed()
        composeTestRule.onNodeWithTag("add_service_fab")
            .assertIsDisplayed()
            .assertTextEquals("أضف محلك وعروضك")
        composeTestRule.onNodeWithTag("add_service_fab").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("choose_register_store_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("choose_add_craftsman_button").assertIsDisplayed()
        composeTestRule.onNodeWithText("إلغاء").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("service_card_guest_s_1").assertIsDisplayed()
        composeTestRule.onNodeWithText("المعلم أبو أحمد للكهرباء").assertIsDisplayed()
        composeTestRule.onNodeWithText("رقم التواصل: 0933112244").assertIsDisplayed()

        // 2. المتاجر (Stores) tab displays starter shops & offers
        composeTestRule.onNodeWithTag("nav_stores").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("register_store_fab").assertIsDisplayed()
        composeTestRule.onNodeWithTag("store_category_filter_group").assertIsDisplayed()
        composeTestRule.onNodeWithTag("stores_lazy_column")
            .performScrollToNode(hasTestTag("store_card_guest_store_1"))
        composeTestRule.onNodeWithTag("store_card_guest_store_1").assertIsDisplayed()

        // 2b. التوصيل (Delivery) tab displays express delivery hero & order form
        composeTestRule.onNodeWithTag("nav_delivery").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("delivery_hero_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText("خدمة التوصيل والنقل السريع").assertIsDisplayed()

        // 3. المواعيد (Schedules) tab displays daily schedules & live utility reports
        composeTestRule.onNodeWithTag("nav_schedules").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("daily_schedules_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("المواعيد اليومية المعتادة في الحي").assertIsDisplayed()

        // 4. الإعلانات (Announcements) tab displays starter announcements & market items
        composeTestRule.onNodeWithTag("nav_announcements").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("add_post_fab").assertIsDisplayed()
        composeTestRule.onNodeWithText("حملة نظافة وتشجير حارة الندى يوم الجمعة").assertIsDisplayed()

        // 5. فزعة الجيران (Neighbor Fazaa) tab displays starter help requests/initiatives
        composeTestRule.onNodeWithTag("nav_fazaa").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("add_fazaa_fab").assertIsDisplayed()
        composeTestRule.onNodeWithText("مطلوب سلم ألمنيوم لساعة لتفقد خزان السطح").assertIsDisplayed()

        // 6. Top-bar Profile button opens ProfileScreen cleanly
        composeTestRule.onNodeWithTag("top_bar_profile_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("profile_google_sign_in_button")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `services screen top filter chips filter craftsmen by plumbing electrical and maintenance`() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("services_category_filter_group").assertIsDisplayed()

        // Plumbing chip filters to plumber only
        composeTestRule.onNodeWithTag("filter_chip_plumbing").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("أبو طارق السنكري").assertIsDisplayed()
        composeTestRule.onNodeWithText("المعلم أبو أحمد للكهرباء").assertDoesNotExist()

        // Electrical chip filters to electrician only
        composeTestRule.onNodeWithTag("filter_chip_electrical").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("المعلم أبو أحمد للكهرباء").assertIsDisplayed()
        composeTestRule.onNodeWithText("أبو طارق السنكري").assertDoesNotExist()

        // Maintenance chip filters to appliance/general maintenance craftsman
        composeTestRule.onNodeWithTag("filter_chip_maintenance").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("أبو خليل لتصليح الغسالات والبرادات").assertIsDisplayed()
        composeTestRule.onNodeWithText("أبو طارق السنكري").assertDoesNotExist()

        // All chip restores the full list
        composeTestRule.onNodeWithTag("filter_chip_all").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("المعلم أبو أحمد للكهرباء").assertIsDisplayed()
    }

    @Test
    fun `stores tab supports filtering registration and adding products`() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("nav_stores").performClick()
        composeTestRule.waitForIdle()

        // Filter by Shoes chip
        composeTestRule.onNodeWithTag("store_filter_shoes").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("stores_lazy_column")
            .performScrollToNode(hasTestTag("store_card_guest_store_2"))
        composeTestRule.onNodeWithTag("store_card_guest_store_2").assertIsDisplayed()

        // Filter by Tech Accessories chip
        composeTestRule.onNodeWithTag("store_category_filter_group")
            .performScrollToNode(hasTestTag("store_filter_tech_accessories"))
        composeTestRule.onNodeWithTag("store_filter_tech_accessories").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("stores_lazy_column")
            .performScrollToNode(hasTestTag("store_card_guest_store_5"))
        composeTestRule.onNodeWithTag("store_card_guest_store_5").assertIsDisplayed()

        // Reset to All stores
        composeTestRule.onNodeWithTag("store_category_filter_group")
            .performScrollToNode(hasTestTag("store_filter_all"))
        composeTestRule.onNodeWithTag("store_filter_all").performClick()
        composeTestRule.waitForIdle()

        // Open Store Detail for store 1 and verify products appear
        composeTestRule.onNodeWithTag("stores_lazy_column")
            .performScrollToNode(hasTestTag("store_card_guest_store_1"))
        composeTestRule.onNodeWithTag("store_card_guest_store_1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("store_detail_header").assertIsDisplayed()
        composeTestRule.onNodeWithText("جاكيت جينز مبطن شتوي").assertIsDisplayed()

        // Back to stores list
        composeTestRule.onNodeWithTag("store_detail_back_button").performClick()
        composeTestRule.waitForIdle()

        // Register a new store in local preview mode
        composeTestRule.onNodeWithTag("register_store_fab").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("store_name_input").performTextInput("متجر الندى للمكياج")
        composeTestRule.onNodeWithTag("store_phone_input").performTextInput("0999887766")
        composeTestRule.onNodeWithTag("submit_store_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("متجر الندى للمكياج").assertIsDisplayed()
        composeTestRule.mainClock.advanceTimeBy(5000L)
        composeTestRule.waitForIdle()

        // Open the newly created store and add a product
        composeTestRule.onNodeWithText("متجر الندى للمكياج").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("add_store_product_fab").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("product_title_input").performTextInput("عطر ياسمين دمشقي")
        composeTestRule.onNodeWithTag("product_price_input").performTextInput("80 ألف ل.س")
        composeTestRule.onNodeWithTag("submit_product_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("عطر ياسمين دمشقي").assertIsDisplayed()
    }

    @Test
    fun `stores support star ratings for service quality and offer credibility`() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("nav_stores").performClick()
        composeTestRule.waitForIdle()

        // Top-rated sort chip is visible and interactive
        composeTestRule.onNodeWithTag("stores_sort_rating_chip").assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()

        // Store card displays rating badge
        composeTestRule.onNodeWithTag("stores_lazy_column")
            .performScrollToNode(hasTestTag("store_card_guest_store_1"))
        composeTestRule.onNodeWithTag("store_card_guest_store_1").performScrollTo()
        composeTestRule.onNodeWithTag("store_card_rating_guest_store_1", useUnmergedTree = true)
            .assertIsDisplayed()

        // Open store 1 detail view
        composeTestRule.onNodeWithTag("store_card_guest_store_1")
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("store_rating_summary_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("جودة الخدمة والتعامل").assertIsDisplayed()
        composeTestRule.onNodeWithText("مصداقية العروض والأسعار").assertIsDisplayed()

        // Click Rate Store button
        composeTestRule.onNodeWithTag("store_detail_lazy_column")
            .performScrollToNode(hasTestTag("rate_store_button"))
        composeTestRule.onNodeWithTag("rate_store_button")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        // Select 4 stars for service quality, 5 stars for offer credibility, and write a review comment
        composeTestRule.onNodeWithTag("rate_store_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("quality_star_4").performClick()
        composeTestRule.onNodeWithTag("credibility_star_5").performClick()
        composeTestRule.onNodeWithTag("review_comment_input")
            .performTextInput("عروض حقيقية وتعامل ممتاز مع أهل الحي")
        composeTestRule.onNodeWithTag("submit_review_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.mainClock.advanceTimeBy(5000L)
        composeTestRule.waitForIdle()

        // Review appears in the store's reviews list and button updates to Edit
        composeTestRule.onNodeWithTag("store_detail_lazy_column")
            .performScrollToNode(hasTestTag("store_review_card_guest_store_1_guest_resident"))
        composeTestRule.onNodeWithTag("store_review_card_guest_store_1_guest_resident")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("عروض حقيقية وتعامل ممتاز مع أهل الحي").assertIsDisplayed()

        composeTestRule.onNodeWithTag("store_detail_lazy_column")
            .performScrollToNode(hasTestTag("rate_store_button"))
        composeTestRule.onNodeWithTag("rate_store_button").performScrollTo()
        composeTestRule.onNodeWithText("تعديل تقييمك للمتجر").assertIsDisplayed()

        // Delete own review removes it cleanly
        composeTestRule.onNodeWithTag("store_detail_lazy_column")
            .performScrollToNode(hasTestTag("delete_review_button_guest_store_1_guest_resident"))
        composeTestRule.onNodeWithTag("delete_review_button_guest_store_1_guest_resident")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("store_review_card_guest_store_1_guest_resident").assertDoesNotExist()
    }

    @Test
    fun `express delivery prefills pickup from store and submits complete delivery order`() {
        composeTestRule.waitForIdle()

        // Open Stores tab and click "اطلب توصيل من هذا المحل" on store 1
        composeTestRule.onNodeWithTag("nav_stores").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("stores_lazy_column")
            .performScrollToNode(hasTestTag("store_card_guest_store_1"))
        composeTestRule.onNodeWithTag("store_card_guest_store_1").performScrollTo()
        composeTestRule.onNodeWithTag("order_delivery_store_guest_store_1", useUnmergedTree = true)
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        // Arrives at Delivery screen with store name auto-filled in مكان الاستلام
        composeTestRule.onNodeWithTag("delivery_hero_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("delivery_prefill_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pickup_location_input")
            .assertTextContains("بوتيك الياسمين للألبسة")

        // Select order type & vehicle
        composeTestRule.onNodeWithTag("order_type_cargo_furniture")
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithTag("vehicle_suzuki")
            .performScrollTo()
            .performClick()

        // Enter order details
        composeTestRule.onNodeWithTag("order_details_input")
            .performScrollTo()
            .performTextInput("صندوق ملابس شتوية + طردين")

        // Open delivery zone dropdown and select مشروع دمر
        composeTestRule.onNodeWithTag("delivery_zone_dropdown")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("delivery_zone_option_dummar_project").performClick()
        composeTestRule.waitForIdle()

        // Fill address, customer name, phone and submit
        composeTestRule.onNodeWithTag("detailed_address_input")
            .performScrollTo()
            .performTextInput("الجزيرة الثالثة بناء 12 طابق 2")
        composeTestRule.onNodeWithTag("customer_name_input")
            .performScrollTo()
            .performTextInput("راما العلي")
        composeTestRule.onNodeWithTag("customer_phone_input")
            .performScrollTo()
            .performTextInput("0944556677")
        composeTestRule.onNodeWithTag("submit_delivery_button")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.mainClock.advanceTimeBy(5000L)
        composeTestRule.waitForIdle()

        // Created order card appears in the delivery list
        composeTestRule.onNodeWithTag("delivery_lazy_column")
            .performScrollToNode(hasText("صندوق ملابس شتوية + طردين"))
        composeTestRule.onNodeWithText("صندوق ملابس شتوية + طردين")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("delivery_lazy_column")
            .performScrollToNode(hasText("راما العلي • 0944556677", substring = true))
        composeTestRule.onNodeWithText("راما العلي • 0944556677", substring = true)
            .assertIsDisplayed()
    }
}
