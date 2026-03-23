package art.intel.soft.ui.edit

import art.intel.soft.base.firebase.events.implementation.AdEvent
import art.intel.soft.ui.edit.main.recycler.FunctionItem

enum class ModeEdit {
    MODE_MAIN,
    MODE_FRAME,
    MODE_COLLAGE,
    MODE_BACKGROUND,
    MODE_BODY,
    MODE_FILTER,
    MODE_EFFECT,
    MODE_FORM,
    MODE_TEXT,
    MODE_STICKER,
    MODE_IMPROVE,
    MODE_BRUSH,
    MODE_CROP;

    fun toAdBannerItem(): AdEvent.Banner.ItemName = when (this) {
        MODE_FRAME -> AdEvent.Banner.ItemName.FRAMES
        MODE_COLLAGE -> AdEvent.Banner.ItemName.COLLAGE
        MODE_BACKGROUND -> AdEvent.Banner.ItemName.BACKGROUND
        MODE_BODY -> AdEvent.Banner.ItemName.BODY
        MODE_FILTER -> AdEvent.Banner.ItemName.FILTERS
        MODE_EFFECT -> AdEvent.Banner.ItemName.EFFECTS
        MODE_FORM -> AdEvent.Banner.ItemName.FORMS
        MODE_TEXT -> AdEvent.Banner.ItemName.TEXT
        MODE_STICKER -> AdEvent.Banner.ItemName.STICKER
        MODE_IMPROVE -> AdEvent.Banner.ItemName.IMPROVE
        MODE_BRUSH -> AdEvent.Banner.ItemName.BRUSH
        MODE_CROP -> AdEvent.Banner.ItemName.CROP
        MODE_MAIN -> AdEvent.Banner.ItemName.EDIT_MENU
    }
}
