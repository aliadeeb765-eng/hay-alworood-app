package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasTestTag
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
}
