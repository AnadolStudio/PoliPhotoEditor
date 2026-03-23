package art.intel.soft.ui.edit.main.recycler

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import art.intel.soft.R

enum class FunctionItem(
        @StringRes val textId: Int,
        @DrawableRes var drawableId: Int,
) {
    FRAME(R.string.edit_func_frames, R.drawable.ic_frames),
    COLLAGE(R.string.edit_func_collages, R.drawable.ic_collages),
    BACKGROUND(R.string.edit_func_background, R.drawable.ic_cut),
    BODY(R.string.edit_func_body, R.drawable.ic_body),
    FILTER(R.string.edit_func_filters, R.drawable.ic_filters),
    EFFECT(R.string.edit_func_effects, R.drawable.ic_effects),
    FORM(R.string.edit_func_form, R.drawable.ic_splash),
    TEXT(R.string.edit_func_text, R.drawable.ic_text),
    STICKER(R.string.edit_func_stickers, R.drawable.ic_stickers),
    IMPROVE(R.string.edit_func_improve, R.drawable.ic_adjusment),
    BRUSH(R.string.edit_func_brush, R.drawable.ic_brush),
    CROP(R.string.edit_func_crop, R.drawable.ic_crop);
}
