package art.intel.soft.ui.edit

import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.view.ViewGroup
import art.intel.soft.utils.changeViewSize
import org.wysaid.view.ImageGLSurfaceView

abstract class SurfaceEditFragment : BaseEditFragment() {

    protected lateinit var surfaceView: ImageGLSurfaceView

    protected open fun surfaceViewBitmap(): Bitmap = editor().getPreviewOriginalBitmap()

    protected open fun initSurfaceView(
            bitmap: Bitmap,
            surfaceCreatedCallback: ((ImageGLSurfaceView) -> Unit)? = null
    ): ImageGLSurfaceView {

        val surfaceView = ImageGLSurfaceView(requireContext(), null).apply {
            updateLayoutSize(bitmap)
            displayMode = ImageGLSurfaceView.DisplayMode.DISPLAY_ASPECT_FIT
        }

        surfaceView.setSurfaceCreatedCallback {
            surfaceView.setImageBitmap(bitmap)
            surfaceCreatedCallback?.invoke(surfaceView)
        }

        return surfaceView
    }

    protected fun ImageGLSurfaceView.updateLayoutSize(bitmap: Bitmap = surfaceViewBitmap()) {
        val workSpace = editor().workSpace()
        val size = changeViewSize(bitmap.width, bitmap.height, workSpace.x, workSpace.y)
        holder.setFormat(PixelFormat.RGBA_8888)

        if (layoutParams == null) {
            layoutParams = ViewGroup.LayoutParams(size.x, size.y)
        } else {
            layoutParams.height = size.y
            layoutParams.width = size.x

        }
        requestLayout()
    }

    open fun initInOnResume() = Unit

    override fun onResume() {
        super.onResume()
        // TODO переделать через lifecycle
        if (!this::surfaceView.isInitialized) {
            surfaceView =
                    initSurfaceView(surfaceViewBitmap()).also(editor()::addView) // тут действительный размер тулбара
            initInOnResume()
        }
//        surfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
//        surfaceView.onPause()
    }

}
