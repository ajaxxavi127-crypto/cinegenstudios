package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.StylePresetItem
import com.example.data.model.TemplateItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CineGen Studio", appName)
    }

    @Test
    fun `verify template data model properties`() {
        val template = TemplateItem(
            id = 1L,
            title = "Cyberpunk City Drive",
            category = "Cyberpunk",
            basePrompt = "Futuristic vehicles speeding through neon alleys",
            motionPrompt = "Forward tracking camera",
            cameraMovement = "Slow Zoom In",
            durationSec = 4,
            fps = 30,
            aspectRatio = "16:9",
            isCustom = false
        )
        assertEquals("Cyberpunk City Drive", template.title)
        assertEquals("16:9", template.aspectRatio)
        assertEquals(4, template.durationSec)
    }

    @Test
    fun `verify style preset data model properties`() {
        val style = StylePresetItem(
            id = 1L,
            name = "Cinematic 35mm",
            styleKeywords = "35mm anamorphic, volumetric lighting",
            negativeKeywords = "blurry, low quality",
            previewColorHex = "#FFB300",
            isCustom = false
        )
        assertEquals("Cinematic 35mm", style.name)
        assertEquals("#FFB300", style.previewColorHex)
    }

    @Test
    fun `verify AdMob configured credentials and normalization`() {
        assertEquals("ca-app-pub-8757057682803457~4510077253", com.example.admob.AdMobManager.CONFIGURED_APP_ID)
        assertEquals("ca-app-pub-8757057682803457/8417707683", com.example.admob.AdMobManager.DEFAULT_USER_AD_UNIT_ID)
        val normalized = com.example.admob.AdMobManager.normalizeAdUnitId("ca-app-pub-8757057682803457~8417707683")
        assertEquals("ca-app-pub-8757057682803457/8417707683", normalized)
    }
}
