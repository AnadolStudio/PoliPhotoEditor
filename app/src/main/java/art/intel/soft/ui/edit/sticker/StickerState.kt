package art.intel.soft.ui.edit.sticker

sealed class StickerState {

    object Loading : StickerState()

    class Content(val data: List<StickerData>) : StickerState()
}
