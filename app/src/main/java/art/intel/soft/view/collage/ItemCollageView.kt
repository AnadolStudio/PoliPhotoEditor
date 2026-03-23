package art.intel.soft.view.collage

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent.ACTION_UP
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import art.intel.soft.databinding.ViewItemCollageBinding
import art.intel.soft.ui.edit.collage.mask_drawable.BaseMaskDrawableBitmapShader
import art.intel.soft.ui.edit.collage.mask_drawable.EmptyMaskDrawableBitmapShader
import art.intel.soft.ui.edit.collage.mask_drawable.PreviewMaskDrawableBitmapShader
import art.intel.soft.ui.edit.collage.mask_drawable.UnselectMaskDrawableBitmapShader
import art.intel.soft.utils.bitmaputils.captureView
import art.intel.soft.utils.bitmaputils.createScaledBitmap
import art.intel.soft.utils.bitmaputils.cropFromSource
import art.intel.soft.utils.bitmaputils.flipHorizontal
import art.intel.soft.utils.bitmaputils.flipVertical
import art.intel.soft.utils.bitmaputils.getBitmapFromImageView
import art.intel.soft.utils.bitmaputils.getSpace
import art.intel.soft.utils.bitmaputils.rotateLeft
import art.intel.soft.utils.bitmaputils.rotateRight
import art.intel.soft.utils.bitmaputils.scaleRatioCircumscribed
import art.intel.soft.utils.fitViewToEdge
import art.intel.soft.utils.throttleClick
import art.intel.soft.utils.touchlisteners.ImageTouchListener
import kotlin.math.roundToInt

@SuppressLint("ClickableViewAccessibility")
class ItemCollageView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private companion object {
        const val START = 0
        const val TOP = 1
        const val END = 2
        const val BOTTOM = 3
    }

    private val binding = ViewItemCollageBinding.inflate(LayoutInflater.from(context), this)
    private var currentState: State = State.Empty
    private var onAddClickAction: ((maskId: Int) -> Unit)? = null
    private var onSelectAction: ((maskId: Int) -> Unit)? = null
    private var placeHolderBitmap: Bitmap? = null
    private var maskId: Int = -1
    private var lastSavedMaskBitmap: Bitmap? = null

    private val photoImageTouchListener = ImageTouchListener(this, true)

    private lateinit var maskBitmap: Bitmap
    private lateinit var bounds: FloatArray

    init {
        binding.photoImage.setOnTouchListener { v, event ->
            if (event.action == ACTION_UP) captureContainer()

            return@setOnTouchListener photoImageTouchListener.onTouch(v, event)
        }
        background = ColorDrawable(Color.TRANSPARENT)
    }

    private fun captureContainer() {
        lastSavedMaskBitmap = captureView(binding.root)
    }

    fun setup(maskId: Int, maskBitmap: Bitmap, bounds: FloatArray) {
        this.maskId = maskId
        this.bounds = bounds

        val (width, height) = getSize()
        this.maskBitmap = Bitmap.createScaledBitmap(maskBitmap, width, height, true)

        layoutParams = ViewGroup.LayoutParams(width, height)
        x = bounds[START]
        y = bounds[TOP]

        requestLayout()
        setState(State.Empty)
    }

    fun setPhoto(photoBitmap: Bitmap, needSelect: Boolean = false) {
        val (width, height) = getSize()

        fitViewToEdge(width, height, photoBitmap, binding.photoImage)
        val scale = scaleRatioCircumscribed(width, height, photoBitmap.width, photoBitmap.height)
        val scaleBitmap = createScaledBitmap(photoBitmap, scale)

        this.placeHolderBitmap = cropFromSource(
                width, height, getSpace(width, scaleBitmap.width), getSpace(height, scaleBitmap.height), scaleBitmap
        )
        binding.photoImage.setImageBitmap(photoBitmap)

        binding.photoImage.post {
            if (needSelect) {
                onSelectAction?.invoke(maskId)
            } else {
                setState(State.Initialized)
            }
        }
    }

    private fun getSize(): Pair<Int, Int> {
        val width = (bounds[END] - bounds[START]).roundToInt()
        val height = (bounds[BOTTOM] - bounds[TOP]).roundToInt()

        return Pair(width, height)
    }

    fun setOnAddLister(listener: ((maskId: Int) -> Unit)?) {
        onAddClickAction = listener
    }

    fun setOnSelectLister(listener: ((maskId: Int) -> Unit)?) {
        onSelectAction = listener
    }

    fun edit(command: EditCommand) = when (command) {
        EditCommand.Delete -> clear()
        EditCommand.FlipHorizontal -> editBitmap { bitmap -> bitmap.flipHorizontal() }
        EditCommand.FlipVertical -> editBitmap { bitmap -> bitmap.flipVertical() }
        EditCommand.RotateLeft -> editBitmap { bitmap -> bitmap.rotateLeft() }
        EditCommand.RotateRight -> editBitmap { bitmap -> bitmap.rotateRight() }
    }

    private fun editBitmap(action: (Bitmap) -> Bitmap) {
        val oldBitmap = getBitmapFromImageView(binding.photoImage) ?: return
        val newBitmap = action.invoke(oldBitmap)
        binding.photoImage.setImageBitmap(newBitmap)
        captureContainer()
    }

    private fun setState(state: State) {
        setClickAction(state)

        when (state) {
            is State.Empty -> showEmptyMask()
            is State.Initialized -> showPreviewMask(placeHolderBitmap)
            is State.Unselect -> showUnselectMask(lastSavedMaskBitmap ?: placeHolderBitmap)
            is State.Select -> Unit
            is State.Preview -> showPreviewMask(lastSavedMaskBitmap ?: placeHolderBitmap)
        }

        binding.addButton.isVisible = state is State.Empty
        binding.photoImage.isVisible = state is State.Select
        binding.maskImage.isVisible = state !is State.Select

        currentState = state
    }

    private fun setClickAction(state: State) = if (state is State.Empty) {
        throttleClick { onAddClickAction?.invoke(maskId) }
    } else {
        throttleClick { onSelectAction?.invoke(maskId) }
    }

    private fun showUnselectMask(bitmap: Bitmap?) {
        bitmap ?: return
        showMask(UnselectMaskDrawableBitmapShader(context, bitmap, maskBitmap))
    }

    private fun showPreviewMask(bitmap: Bitmap?) {
        bitmap ?: return
        showMask(PreviewMaskDrawableBitmapShader(bitmap, maskBitmap))
    }

    private fun showEmptyMask() = showMask(EmptyMaskDrawableBitmapShader(maskBitmap))

    private fun showMask(drawable: BaseMaskDrawableBitmapShader) {
        binding.maskImage.setImageDrawable(null) // Иначе, каждый новый Drawable накладывается
        binding.maskImage.setImageDrawable(drawable)
    }

    fun clear() {
        setState(State.Empty)
        binding.photoImage.setImageBitmap(null)
        lastSavedMaskBitmap = null
        placeHolderBitmap = null
    }

    fun getMaskId(): Int = maskId

    fun select() = setState(State.Select)

    fun unselect() = if (currentState is State.Empty) Unit else setState(State.Unselect)

    fun preview() = if (currentState is State.Empty) Unit else setState(State.Preview)

    fun isNotEmpty() = currentState != State.Empty

    private sealed class State {

        object Empty : State()

        object Initialized : State()

        object Select : State()

        object Unselect : State()

        object Preview : State()
    }

    sealed class EditCommand {

        object FlipHorizontal : EditCommand()

        object FlipVertical : EditCommand()

        object RotateRight : EditCommand()

        object RotateLeft : EditCommand()

        object Delete : EditCommand()
    }

}
