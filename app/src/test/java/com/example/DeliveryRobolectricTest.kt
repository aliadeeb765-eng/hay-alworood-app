package com.example

import android.Manifest
import android.app.Application
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DELIVERY_ADMIN_WHATSAPP_PHONE
import com.example.ui.HayAlWurudApp
import com.example.ui.components.openWhatsAppMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp-xxhdpi")
class DeliveryRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `service pharmacy order button prefills pharmacy and gps button attaches maps link`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        Shadows.shadowOf(app).grantPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        var capturedWhatsAppPhone: String? = null
        var capturedWhatsAppMessage: String? = null

        composeTestRule.setContent {
            HayAlWurudApp(
                updateChecker = { null },
                gpsLocationFetcher = {
                    Result.success("https://maps.google.com/?q=33.532100,36.224500")
                },
                onOpenWhatsApp = { _, phone, msg ->
                    capturedWhatsAppPhone = phone
                    capturedWhatsAppMessage = msg
                }
            )
        }
        composeTestRule.waitForIdle()

        // Scroll in Services tab to Pharmacy card ("صيدلية الورود المناوبة", id = guest_s_3)
        composeTestRule.onNodeWithTag("services_lazy_column")
            .performScrollToNode(hasTestTag("order_delivery_service_guest_s_3"))
        composeTestRule.onNodeWithTag("order_delivery_service_guest_s_3")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        // Verify pickup pre-filled with pharmacy name
        composeTestRule.onNodeWithTag("delivery_hero_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pickup_location_input")
            .assertTextContains("صيدلية الورود المناوبة")

        // Tap GPS auto-locate button and verify Google Maps link badge appears
        composeTestRule.onNodeWithTag("gps_locate_button")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("gps_maps_link_badge")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("https://maps.google.com/?q=33.532100,36.224500")
            .performScrollTo()
            .assertIsDisplayed()

        // Fill minimal remaining fields and submit via WhatsApp
        composeTestRule.onNodeWithTag("order_details_input")
            .performScrollTo()
            .performTextInput("دواء خافض حرارة وفيتامينات")
        composeTestRule.onNodeWithTag("customer_name_input")
            .performScrollTo()
            .performTextInput("سامر محمود")
        composeTestRule.onNodeWithTag("customer_phone_input")
            .performScrollTo()
            .performTextInput("0955112233")
        composeTestRule.onNodeWithTag("submit_delivery_button")
            .performScrollTo()
            .assertTextContains("إرسال الطلب عبر واتساب")
            .performClick()
        composeTestRule.waitForIdle()

        assertEquals(DELIVERY_ADMIN_WHATSAPP_PHONE, capturedWhatsAppPhone)
        val sentOrderMsg = requireNotNull(capturedWhatsAppMessage)
        assertTrue(sentOrderMsg.contains("صيدلية الورود المناوبة"))
        assertTrue(sentOrderMsg.contains("دواء خافض حرارة وفيتامينات"))
        assertTrue(sentOrderMsg.contains("https://maps.google.com/?q=33.532100,36.224500"))
        assertTrue(sentOrderMsg.contains("سامر محمود"))
        assertTrue(sentOrderMsg.contains("0955112233"))
        assertTrue(sentOrderMsg.contains("10,000 ل.س"))

        composeTestRule.mainClock.advanceTimeBy(5000L)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("delivery_lazy_column")
            .performScrollToNode(hasText("دواء خافض حرارة وفيتامينات"))
        composeTestRule.onNodeWithText("دواء خافض حرارة وفيتامينات")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("delivery_lazy_column")
            .performScrollToNode(hasText("سامر محمود • 0955112233", substring = true))
        composeTestRule.onNodeWithText("سامر محمود • 0955112233", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun `instant fare calculator updates dynamically and customer data auto-saves and clears`() {
        composeTestRule.setContent {
            HayAlWurudApp(
                updateChecker = { null },
                gpsLocationFetcher = { Result.failure(IllegalStateException("unused")) }
            )
        }
        composeTestRule.waitForIdle()

        // Navigate to Delivery tab
        composeTestRule.onNodeWithTag("nav_delivery").performClick()
        composeTestRule.waitForIdle()

        // Default: Motorcycle + Inside Hay Al-Wurud -> 10,000 ل.س
        composeTestRule.onNodeWithTag("estimated_fare_card").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("estimated_fare_value").assertTextContains("10,000 ل.س")

        // Change vehicle to Sedan -> 20,000 ل.س
        composeTestRule.onNodeWithTag("vehicle_sedan").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("estimated_fare_value").performScrollTo().assertTextContains("20,000 ل.س")

        // Change zone to Other Damascus -> 45,000 ل.س
        composeTestRule.onNodeWithTag("delivery_zone_dropdown").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("delivery_zone_option_other_damascus").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("estimated_fare_value").performScrollTo().assertTextContains("45,000 ل.س")

        // Change vehicle to Truck -> تبدأ من 120,000 ل.س + disclaimer note
        composeTestRule.onNodeWithTag("vehicle_truck").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("estimated_fare_value").performScrollTo().assertTextContains("تبدأ من 120,000 ل.س")
        composeTestRule.onNodeWithTag("estimated_fare_note")
            .performScrollTo()
            .assertTextContains("السعر تقديري وقد يختلف حسب المسافة وعدد العمال")

        // Fill form and submit to auto-save customer info (name, phone, zone, address)
        composeTestRule.onNodeWithTag("pickup_location_input")
            .performScrollTo()
            .performTextInput("سوق حي الورود")
        composeTestRule.onNodeWithTag("order_details_input")
            .performScrollTo()
            .performTextInput("أغراض منزلية متفرقة")
        composeTestRule.onNodeWithTag("detailed_address_input")
            .performScrollTo()
            .performTextInput("مقابل جامع الهدى طابق 3")
        composeTestRule.onNodeWithTag("customer_name_input")
            .performScrollTo()
            .performTextInput("ندى الخطيب")
        composeTestRule.onNodeWithTag("customer_phone_input")
            .performScrollTo()
            .performTextInput("0988776655")
        composeTestRule.onNodeWithTag("submit_delivery_button")
            .performScrollTo()
            .performClick()
        composeTestRule.mainClock.advanceTimeBy(5000L)
        composeTestRule.waitForIdle()

        // Allow DataStore IO write to complete, then switch away and back to Delivery tab (simulating reopen)
        Thread.sleep(250)
        composeTestRule.onNodeWithTag("nav_stores").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nav_delivery").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(150)
        composeTestRule.waitForIdle()

        // Saved customer data auto-fills all 4 fields
        composeTestRule.onNodeWithTag("customer_name_input")
            .performScrollTo()
            .assertTextContains("ندى الخطيب")
        composeTestRule.onNodeWithTag("customer_phone_input")
            .performScrollTo()
            .assertTextContains("0988776655")
        composeTestRule.onNodeWithTag("detailed_address_input")
            .performScrollTo()
            .assertTextContains("مقابل جامع الهدى طابق 3")
        composeTestRule.onNodeWithTag("delivery_zone_dropdown")
            .performScrollTo()
            .assertTextContains("مناطق أخرى في دمشق")

        // Bring fare card below into view so clear button sits comfortably inside viewport, then click it
        composeTestRule.onNodeWithTag("estimated_fare_card").performScrollTo()
        composeTestRule.onNodeWithTag("clear_saved_delivery_data_button")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("clear_saved_delivery_data_button").assertDoesNotExist()
        composeTestRule.onNodeWithTag("customer_name_input")
            .performScrollTo()
            .assertTextContains("")
        composeTestRule.onNodeWithTag("delivery_zone_dropdown")
            .performScrollTo()
            .assertTextContains("داخل حي الورود")
    }

    @Test
    fun `join driver modal validates and sends driver registration via whatsapp and normalizes arabic digits`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        openWhatsAppMessage(app, "٠٩٣٣١٢٥١٠٢", "اختبار")
        val startedIntent = Shadows.shadowOf(app).nextStartedActivity
        assertNotNull(startedIntent)
        assertEquals(Intent.ACTION_VIEW, startedIntent.action)
        assertTrue(startedIntent.dataString!!.startsWith("https://wa.me/963933125102?text="))

        var driverWhatsAppPhone: String? = null
        var driverWhatsAppMessage: String? = null

        composeTestRule.setContent {
            HayAlWurudApp(
                updateChecker = { null },
                gpsLocationFetcher = { Result.failure(IllegalStateException("unused")) },
                onOpenWhatsApp = { _, phone, msg ->
                    driverWhatsAppPhone = phone
                    driverWhatsAppMessage = msg
                }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("nav_delivery").performClick()
        composeTestRule.waitForIdle()

        // Scroll to the bottom Join as Driver recruitment section and open modal
        composeTestRule.onNodeWithTag("delivery_lazy_column")
            .performScrollToNode(hasTestTag("open_join_driver_dialog_button"))
        composeTestRule.onNodeWithTag("open_join_driver_dialog_button")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("join_driver_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("driver_full_name_input").performTextInput("فادي بركات")
        composeTestRule.onNodeWithTag("driver_phone_input").performTextInput("0933998877")

        composeTestRule.onNodeWithTag("driver_vehicle_dropdown").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("driver_vehicle_option_pickup").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("submit_driver_join_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("join_driver_dialog").assertDoesNotExist()
        assertEquals("0933125102", driverWhatsAppPhone)
        val sentDriverMsg = requireNotNull(driverWhatsAppMessage)
        assertTrue(sentDriverMsg.contains("فادي بركات"))
        assertTrue(sentDriverMsg.contains("0933998877"))
        assertTrue(sentDriverMsg.contains("بيك أب"))
    }
}
