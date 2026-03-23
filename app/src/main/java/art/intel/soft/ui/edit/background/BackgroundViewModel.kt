package art.intel.soft.ui.edit.background

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import art.intel.soft.R
import art.intel.soft.base.firebase.AnalyticEventFabric
import art.intel.soft.base.firebase.RememberItemDelegate
import art.intel.soft.base.view_model.BaseAndroidViewModel
import art.intel.soft.extention.baseSubscribe
import art.intel.soft.extention.singleFrom
import art.intel.soft.model.AssetsDirections
import art.intel.soft.model.getPathList
import art.intel.soft.model.segmenter.SegmenterService
import art.intel.soft.ui.edit.background.BrushSettings.Companion.NORMAL
import art.intel.soft.utils.bitmaputils.CutUtils
import art.intel.soft.utils.onNext
import art.intel.soft.utils.toImmutable
import io.reactivex.Single

class BackgroundViewModel(application: Application) : BaseAndroidViewModel<BackgroundState>(application),
        RememberItemDelegate<String> by RememberItemDelegate.Delegate(AnalyticEventFabric.Body()) {

    companion object {
        const val CUSTOM = "custom"
        const val COLOR = "color"
    }

    private val service = SegmenterService()

    private val _brushSettingsState = MutableLiveData(
            BrushSettings(
                    size = NORMAL,
                    color = app.getColor(R.color.background_func_back_color),
                    mode = BrushMode.DRAW
            )
    )
    val brushSettingsState = _brushSettingsState.toImmutable()

    fun createMask(bitmap: Bitmap) {
        _state.onNext(BackgroundState.Loading)

        service.createMask(bitmap)
                .flatMap { mask -> service.createBitmapByMask(mask, app.getColor(R.color.background_func_back_color)) }
                .baseSubscribe(
                        { maskBitmap ->
                            _state.onNext(BackgroundState.Content.CutContent(maskBitmap))
                        },
                        { error ->
                            _state.onNext(BackgroundState.Error)
                        }
                )
                .disposeOnViewModelDestroy()
    }

    fun cutBackground(mainViewBitmap: Bitmap, drawViewBitmap: Bitmap, hasChanges: Boolean) {
        val mask = (_state.value as? BackgroundState.Content.CutContent)
                ?.maskBitmap
                ?: let {
                    if (hasChanges) return@let null

                    _singleEvent.onNext(BackgroundEvent.MaskNotExistEvent)

                    return
                }

        _state.onNext(BackgroundState.Loading)

        Single.zip(
                CutUtils.cutBackground(app, mainViewBitmap, drawViewBitmap),
                singleFrom { listOf(CUSTOM, COLOR) + getPathList(app, AssetsDirections.BACKGROUND_DIR) },
        ) { cutBitmap, backgroundList ->
            BackgroundState.Content.ChoiceContent(
                    maskBitmap = mask,
                    drawingBitmap = if (hasChanges) drawViewBitmap else null,
                    cutBitmap = cutBitmap,
                    backgroundPathList = backgroundList
            )
        }
                .baseSubscribe(
                        onSuccess = _state::onNext,
                        onError = { error -> _state.onNext(BackgroundState.Error) }
                )
                .disposeOnViewModelDestroy()
    }

    fun clearDrawPanel() {
        val maskBitmap = (_state.value as? BackgroundState.Content.CutContent)?.maskBitmap
        _singleEvent.onNext(BackgroundEvent.ClearEvent(maskBitmap))
    }

    fun updateBrushSettings(
            size: Float? = null,
            mode: BrushMode? = null,
    ) {
        val currentStetting = _brushSettingsState.value ?: return

        _brushSettingsState.onNext(
                currentStetting.copy(
                        size = size ?: currentStetting.size,
                        mode = mode ?: currentStetting.mode,
                )
        )
    }

    fun backToCut() {
        val content = (_state.value as? BackgroundState.Content.ChoiceContent) ?: return
        val maskBitmap = content.maskBitmap
        val drawingBitmap = content.drawingBitmap
        _state.onNext(BackgroundState.Content.CutContent(maskBitmap = maskBitmap, drawingBitmap = drawingBitmap))
    }

}
