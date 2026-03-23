package art.intel.soft

import art.intel.soft.base.firebase.AnalyticEventsUtil
import art.intel.soft.base.firebase.events.Screens
import art.intel.soft.base.firebase.events.implementation.AdEvent.Banner
import art.intel.soft.base.firebase.events.implementation.AdEvent.Interstitial
import art.intel.soft.base.firebase.events.implementation.AdEvent.Rewarded
import art.intel.soft.base.firebase.events.implementation.ClickEvent
import art.intel.soft.base.firebase.events.implementation.ClickItemEvent
import art.intel.soft.base.firebase.events.implementation.OpenItemEvent
import art.intel.soft.base.firebase.events.implementation.PopUpEvent
import org.junit.Assert
import org.junit.Test

class FirebaseTest {

    @Test
    fun `success generate open event name`() {
        Assert.assertEquals("open_filters", OpenItemEvent.Filters("test").toString())
        Assert.assertEquals("open_frames", OpenItemEvent.Frames("test").toString())
        Assert.assertEquals("open_forms", OpenItemEvent.Forms("test").toString())
        Assert.assertEquals("open_improve", OpenItemEvent.Improve("test").toString())
        Assert.assertEquals("open_effects", OpenItemEvent.Effects("test").toString())
        Assert.assertEquals("open_text", OpenItemEvent.Text("test").toString())
        Assert.assertEquals("open_stickers", OpenItemEvent.Stickers("test").toString())
        Assert.assertEquals("open_background", OpenItemEvent.Background("test").toString())
        Assert.assertEquals("open_body", OpenItemEvent.Body("test").toString())
        Assert.assertEquals("open_collage", OpenItemEvent.Collage("test").toString())

        Assert.assertEquals("open_stickers_category", OpenItemEvent.Stickers.Category("test").toString())
        Assert.assertEquals("open_text_font", OpenItemEvent.Text.Font("test").toString())
    }

    @Test
    fun `success generate click event name`() {
        Assert.assertEquals("click_save_screen", ClickItemEvent.SaveScreen(ClickItemEvent.SaveScreen.ItemName.BACK).toString())
        Assert.assertEquals("click_edit_test", ClickEvent.Edit("test").toString())
        Assert.assertEquals("click_edit_save", ClickEvent.Edit.Save().toString())
        Assert.assertEquals("click_background", ClickItemEvent.Background("test").toString())
        Assert.assertEquals("click_background_not_found", ClickItemEvent.Background.NotFound(ClickItemEvent.Background.NotFound.ItemName.MANUAL).toString())
    }

    @Test
    fun `success generate ad event name`() {
        Assert.assertEquals("ad_interstitial_click", Interstitial.Click(Interstitial.ItemName.OPEN_APP).toString())
        Assert.assertEquals("ad_interstitial_showed", Interstitial.Showed(Interstitial.ItemName.OPEN_APP).toString())
        Assert.assertEquals("ad_rewarded_showed", Rewarded.Showed(Rewarded.ItemName.BODY).toString())
        Assert.assertEquals("ad_rewarded_showed", Rewarded.Showed(Rewarded.ItemName.BODY).toString())
        Assert.assertEquals("ad_banner_click", Banner.Click(Banner.ItemName.BRUSH).toString())
        Assert.assertEquals("ad_banner_showed", Banner.Showed(Banner.ItemName.BRUSH).toString())
    }

    @Test
    fun `success generate pop-up event name`() {
        Assert.assertEquals("pop_up_back_apply", PopUpEvent.Back.Apply("Test").toString())
        Assert.assertEquals("pop_up_back_cancel", PopUpEvent.Back.Cancel("Test").toString())
        Assert.assertEquals("pop_up_save_apply", PopUpEvent.Save.Apply("Test").toString())
        Assert.assertEquals("pop_up_save_cancel", PopUpEvent.Save.Cancel("Test").toString())
    }

    @Test
    fun `success get item name from path`() {
        Assert.assertEquals("text", AnalyticEventsUtil.getNameFromPath("testFolder/text.txt"))
        Assert.assertEquals("owr_name", AnalyticEventsUtil.getNameFromPath("owr_name"))
    }
}
