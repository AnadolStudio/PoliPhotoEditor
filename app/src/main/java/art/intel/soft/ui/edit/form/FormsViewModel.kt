package art.intel.soft.ui.edit.form

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import art.intel.soft.base.firebase.AnalyticEventFabric
import art.intel.soft.base.firebase.RememberItemDelegate
import art.intel.soft.base.view_model.BaseAndroidViewModel
import art.intel.soft.extention.baseSubscribe
import art.intel.soft.extention.correctInDiapason
import art.intel.soft.extention.singleBy
import art.intel.soft.extention.singleFrom
import art.intel.soft.model.AssetsDirections
import art.intel.soft.model.getPathList
import art.intel.soft.utils.ImageLoader
import art.intel.soft.utils.onNext
import art.intel.soft.utils.toImmutable
import art.intel.soft.view.FormView
import io.reactivex.Single

class FormsViewModel(application: Application) : BaseAndroidViewModel<FormState>(application),
        RememberItemDelegate<String> by RememberItemDelegate.Delegate(AnalyticEventFabric.Forms()) {

    private companion object {
        const val SLIDER_MAX_COUNT = 100
        const val BLUR_MAX_COUNT = 24
        const val BLUR_RATIO = SLIDER_MAX_COUNT / BLUR_MAX_COUNT
    }

    init {
        initData()
    }

    protected val _settingsState = MutableLiveData(SettingsState())
    val settingsState = _settingsState.toImmutable()

    private fun initData() {
        _state.onNext(FormState.Loading)

        Single.zip(
                singleFrom { getPathList(app, AssetsDirections.FORM_DIR) },
                singleFrom { getPathList(app, AssetsDirections.FORM_MASK_DIR) },
                singleFrom { getPathList(app, AssetsDirections.FORM_PREVIEW) },
        ) { formPath, formMaskPath, formPreviewPath ->
            val minSize = minOf(formPath.size, formMaskPath.size, formPreviewPath.size)
            val formDataList = mutableListOf<FormData>()

            for (i in 0 until minSize) {
                formDataList.add(
                        FormData(
                                previewPath = formPreviewPath[i],
                                formPath = formPath[i],
                                formMaskPath = formMaskPath[i]
                        )
                )
            }

            return@zip formDataList.toList()
        }.map { formDataList -> FormState.Content(formDataList) }
                .baseSubscribe(_state::onNext)
                .disposeOnViewModelDestroy()

        getPathList(app, AssetsDirections.FORM_DIR)
    }

    fun loadForm(pathForm: String, pathFormMask: String) {
        _singleEvent.onNext(FormEvent.Loading)

        Single.zip(
                singleBy<Bitmap> {
                    ImageLoader.loadImageWithoutCache(app, pathForm) { form: Bitmap ->
                        onSuccess(form.copy(Bitmap.Config.ALPHA_8, false))
                    }
                },
                singleBy<Bitmap> {
                    ImageLoader.loadImageWithoutCache(app, pathFormMask) { mask: Bitmap ->
                        onSuccess(mask.copy(Bitmap.Config.ALPHA_8, false))
                    }
                },
        ) { form, mask -> Pair(form, mask) }
                .baseSubscribe(
                        onSuccess = { (form, mask) ->
                            _singleEvent.onNext(FormEvent.ShowForm(form, mask))
                            onChangeSettingsState { SettingsState(backSliderType = BackgroundSettingsType.SATURATION) }
                        }
                )
                .disposeOnViewModelDestroy()
    }

    fun changeBackSliderType(type: BackgroundSettingsType) {
        val previousSettings = _settingsState.value ?: return

        val currentValue = when (type) {
            BackgroundSettingsType.BLUR -> ((previousSettings.blurRadius - 1) * BLUR_RATIO).correctInDiapason(min = 0F, max = 100F)
            BackgroundSettingsType.SATURATION -> previousSettings.saturation * 100F
        }

        onChangeSettingsState { copy(currentValue = currentValue, backSliderType = type) }
    }

    fun changeColorFrame(newColor: Int) = onChangeSettingsState { copy(colorFrame = newColor) }

    private fun changeBlur(value: Float) {
        val correctValue = (1 + value / BLUR_RATIO).correctInDiapason(min = FormView.MIN_BLUR, max = FormView.MAX_BLUR)
        onChangeSettingsState { copy(currentValue = value, blurRadius = correctValue) }
    }

    private fun changeSaturation(value: Float) {
        val correctValue = value / 100
        onChangeSettingsState { copy(currentValue = value, saturation = correctValue) }
    }

    fun changeBackgroundSliderValue(value: Float) {
        val previousSettings = _settingsState.value ?: return

        when (previousSettings.backSliderType) {
            BackgroundSettingsType.BLUR -> changeBlur(value)
            BackgroundSettingsType.SATURATION -> changeSaturation(value)
        }
    }

    private fun onChangeSettingsState(action: SettingsState.() -> SettingsState) {
        val previousSettings = _settingsState.value ?: return
        _settingsState.onNext(action.invoke(previousSettings))
    }

    data class SettingsState(
            val currentValue: Float = 0F,
            val backSliderType: BackgroundSettingsType = BackgroundSettingsType.SATURATION,
            val colorFrame: Int? = null,
            val blurRadius: Float = FormView.MIN_BLUR,
            val saturation: Float = FormView.MIN_SATURATION,
    )

    enum class BackgroundSettingsType { BLUR, SATURATION }
}
