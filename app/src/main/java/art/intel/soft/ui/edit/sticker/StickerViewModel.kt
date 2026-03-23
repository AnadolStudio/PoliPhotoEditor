package art.intel.soft.ui.edit.sticker

import android.app.Application
import art.intel.soft.base.firebase.AnalyticEventsUtil
import art.intel.soft.base.firebase.events.implementation.ApplyItemEvent
import art.intel.soft.base.view_model.BaseAndroidViewModel
import art.intel.soft.extention.baseSubscribe
import art.intel.soft.extention.singleFrom
import art.intel.soft.model.AssetsDirections.STICKER_DIR_1
import art.intel.soft.model.AssetsDirections.STICKER_DIR_10
import art.intel.soft.model.AssetsDirections.STICKER_DIR_11
import art.intel.soft.model.AssetsDirections.STICKER_DIR_2
import art.intel.soft.model.AssetsDirections.STICKER_DIR_3
import art.intel.soft.model.AssetsDirections.STICKER_DIR_4
import art.intel.soft.model.AssetsDirections.STICKER_DIR_5
import art.intel.soft.model.AssetsDirections.STICKER_DIR_6
import art.intel.soft.model.AssetsDirections.STICKER_DIR_7
import art.intel.soft.model.AssetsDirections.STICKER_DIR_8
import art.intel.soft.model.AssetsDirections.STICKER_DIR_9
import art.intel.soft.model.getPathList
import art.intel.soft.utils.onNext

class StickerViewModel(application: Application) : BaseAndroidViewModel<StickerState>(application) {

    private val openedStickers = hashMapOf<String, Int>()

    init {
        _state.onNext(StickerState.Loading)

        singleFrom {
            mutableListOf<StickerData>().apply {
                add(StickerData(StickerGroup.GROUP_1.value, StickerGroup.GROUP_1.drawableRes, getPathList(app, STICKER_DIR_1)))
                add(StickerData(StickerGroup.GROUP_2.value, StickerGroup.GROUP_2.drawableRes, getPathList(app, STICKER_DIR_2)))
                add(StickerData(StickerGroup.GROUP_3.value, StickerGroup.GROUP_3.drawableRes, getPathList(app, STICKER_DIR_3)))
                add(StickerData(StickerGroup.GROUP_4.value, StickerGroup.GROUP_4.drawableRes, getPathList(app, STICKER_DIR_4)))
                add(StickerData(StickerGroup.GROUP_5.value, StickerGroup.GROUP_5.drawableRes, getPathList(app, STICKER_DIR_5)))
                add(StickerData(StickerGroup.GROUP_6.value, StickerGroup.GROUP_6.drawableRes, getPathList(app, STICKER_DIR_6)))
                add(StickerData(StickerGroup.GROUP_7.value, StickerGroup.GROUP_7.drawableRes, getPathList(app, STICKER_DIR_7)))
                add(StickerData(StickerGroup.GROUP_8.value, StickerGroup.GROUP_8.drawableRes, getPathList(app, STICKER_DIR_8)))
                add(StickerData(StickerGroup.GROUP_9.value, StickerGroup.GROUP_9.drawableRes, getPathList(app, STICKER_DIR_9)))
                add(StickerData(StickerGroup.GROUP_10.value, StickerGroup.GROUP_10.drawableRes, getPathList(app, STICKER_DIR_10)))
                add(StickerData(StickerGroup.GROUP_11.value, StickerGroup.GROUP_11.drawableRes, getPathList(app, STICKER_DIR_11)))
            }
        }.baseSubscribe(
                onSuccess = { stickerDataList -> _state.onNext(StickerState.Content(stickerDataList)) }
        ).disposeOnViewModelDestroy()
    }

    fun addSticker(name: String) {
        var count = openedStickers[name] ?: 0
        openedStickers[name] = ++count
    }

    fun removeSticker(name: String) {
        var count = openedStickers[name] ?: 0
        openedStickers[name] = maxOf(0, --count)
    }

    fun logApplyStickerEvent() {
        openedStickers.entries.forEach { (name, count) ->
            for (i in 0 until count) {
                AnalyticEventsUtil.logEvent(ApplyItemEvent.Stickers(name))
            }
        }
    }
}
