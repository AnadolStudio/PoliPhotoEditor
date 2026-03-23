package art.intel.soft.ui.edit.crop

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import art.intel.soft.R
import art.intel.soft.base.firebase.AnalyticEventsUtil
import art.intel.soft.base.firebase.events.Screens
import art.intel.soft.base.firebase.events.implementation.OpenItemEvent
import art.intel.soft.databinding.FragmentEditCropBinding
import art.intel.soft.ui.edit.BaseEditFragment
import art.intel.soft.ui.edit.FragmentCreatedCallback
import art.intel.soft.utils.bitmaputils.flipHorizontal
import art.intel.soft.utils.bitmaputils.flipVertical
import art.intel.soft.utils.bitmaputils.rotateLeft
import art.intel.soft.utils.bitmaputils.rotateRight
import art.intel.soft.utils.throttleClick
import art.intel.soft.view.BaseToolbar
import com.canhub.cropper.CropImageView

class CropEditFragment : BaseEditFragment() {

    override val toolbarTitleId: Int = R.string.edit_crop_title

    companion object {

        fun newInstance(callback: FragmentCreatedCallback): CropEditFragment = CropEditFragment().apply {
            this.callback = callback
        }
    }

    override fun provideScreenName(): Screens = Screens.CROP

    private val binding by lazy(LazyThreadSafetyMode.NONE) { FragmentEditCropBinding.inflate(layoutInflater) }
    private lateinit var cropPanel: CropImageView
    private lateinit var cropBitmap: Bitmap
    private var currentState: State = State.BASE_FUNCTION
    private val viewModel: CropViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        initCropView()
        initBaseButton()

        return binding.root
    }

    private fun initCropView() {
        cropPanel = editor().cropView() // TODO можно добавлять в контейнер
        cropPanel.visibility = View.VISIBLE
        cropPanel.setFixedAspectRatio(false)
        cropPanel.isShowCropOverlay = false
        cropBitmap = editor().currentBitmap()
        setBitmapAndSelectWholeRect(editor().currentBitmap())
        binding.cropFunctionRecycler.adapter = CropDslAdapter { _, ratio -> actionCrop(ratio) }.setup()
    }

    private fun initBaseButton() {
        with(binding) {

            sizeButton.throttleClick { setState(State.CROP_FUNCTION) }

            reflectVerticalButton.throttleClick {
                changeCropBitmap(cropBitmap.flipVertical())
                setChanges()
            }

            reflectHorizontalButton.throttleClick {
                changeCropBitmap(cropBitmap.flipHorizontal())
                setChanges()
            }

            rotateLeftButton.throttleClick {
                changeCropBitmap(cropBitmap.rotateLeft())
                setChanges()
            }

            rotateRightButton.throttleClick {
                changeCropBitmap(cropBitmap.rotateRight())
                setChanges()
            }
        }
    }

    private fun changeCropBitmap(newBitmap: Bitmap) {
        cropBitmap = newBitmap
        cropPanel.setImageBitmap(newBitmap)
        cropPanel.cropRect = cropPanel.wholeImageRect
    }

    private fun setState(state: State) {
        currentState = state
        cropPanel.isShowCropOverlay = state == State.CROP_FUNCTION

        binding.baseFunction.isVisible = state == State.BASE_FUNCTION
        binding.cropFunctionRecycler.isVisible = state == State.CROP_FUNCTION

        cropPanel.cropRect = cropPanel.wholeImageRect // clear zoom
    }

    override fun onSetupToolbar(toolbar: BaseToolbar) {
        super.onSetupToolbar(toolbar)
        toolbar.setRightButtonAction(this::applyChanges)
    }

    private fun applyChanges() = when (currentState) {
        State.BASE_FUNCTION -> showApplyDialog()
        State.CROP_FUNCTION -> applyCropImage()
    }

    override fun dialogActionApply() {
        cropPanel.croppedImage?.let { newBitmap -> cropBitmap = newBitmap }
        super.dialogActionApply()
    }

    override fun onBackPressed(): Boolean = when {
        currentState == State.CROP_FUNCTION -> false.also { setState(State.BASE_FUNCTION) }
        hasChanges -> false.also { showCancelDialog() }
        else -> true
    }

    override fun process(main: Bitmap, support: Bitmap?): Bitmap = cropBitmap.also {
        cropPanel.resetCropRect()
    }

    private fun applyCropImage() {
        cropPanel.croppedImage?.let { newBitmap -> cropBitmap = newBitmap }
        setBitmapAndSelectWholeRect(cropBitmap)
        setChanges()
        setState(State.BASE_FUNCTION)
        viewModel.applyItem()
    }

    private fun setBitmapAndSelectWholeRect(bitmap: Bitmap) {
        cropPanel.setImageBitmap(bitmap)
        cropPanel.cropRect = cropPanel.wholeImageRect
    }

    private fun actionCrop(ratioText: RatioText) {
        viewModel.onOpenItem(ratioText.value)

        when (ratioText) {
            RatioText.FREE -> cropPanel.setFixedAspectRatio(false)
            else -> cropPanel.setAspectRatio(ratioText.aspectRatio.aspectX, ratioText.aspectRatio.aspectY)
        }
    }

    private enum class State {
        BASE_FUNCTION,
        CROP_FUNCTION,
    }

}
