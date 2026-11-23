package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.data.update.AppUpdateInfo
import com.example.ui.HayAlWurudApp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp-xxhdpi")
class UpdatePromptRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `shows update dialog when remote latestVersion exceeds versionCode and dismisses on later`() {
        composeTestRule.setContent {
            HayAlWurudApp(
                updateChecker = {
                    AppUpdateInfo(
                        latestVersion = BuildConfig.VERSION_CODE + 1,
                        apkUrl = "https://github.com/aliadeeb765-eng/hay-alworood-app/raw/main/my-app.apk"
                    )
                }
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("update_available_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithText("يتوفر تحديث جديد لتطبيق حي الورود").assertIsDisplayed()
        composeTestRule.onNodeWithTag("update_download_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("update_dismiss_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("update_available_dialog").assertDoesNotExist()
    }
}
