package tr.erdaldemir.barem

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MemurWizardUiSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeToKadroStep_afterHizmetSinifiSelection() {
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Maaş").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("YHS — Yardımcı Hizmetler", substring = true).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Maaş unsurları", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("Unvan", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("Kademe", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("Devam").assertIsDisplayed()
    }
}
