package art.intel.soft.ui.edit.body

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import art.intel.soft.utils.touchlisteners.BodyTouchListener
import art.intel.soft.view.body.BreastView
import art.intel.soft.view.body.HipsView
import art.intel.soft.view.body.WaistView
import ja.burhanrashid52.photoeditor.util.dpToPx

interface BodyViewBuilder {

    fun create(
            context: Context,
            x: Float,
            y: Float,
            listener: BodyTouchListener,
            photoIsBright: Boolean
    ): List<View>

    abstract class Base : BodyViewBuilder

    class BreastBuilder : Base() {

        private val side = 65.dpToPx()

        @SuppressLint("ClickableViewAccessibility")
        override fun create(
                context: Context,
                x: Float,
                y: Float,
                listener: BodyTouchListener,
                photoIsBright: Boolean
        ): List<View> {
            val result = mutableListOf<View>()

            for (i in 0 until 2) {

                val breastView = BreastView(context, null, 0, 0).apply {
                    setPhotoIsBright(photoIsBright)
                    isMirror(i % 2 == 0)
                    layoutParams = ViewGroup.LayoutParams(side, side)
                    translationX = x + side * if (i % 2 == 0) -1 else 0
                    translationY = y
                    requestLayout()
                }

                result.add(breastView)
            }

            return result
        }
    }

    open class WaistBuilder : Base() {
        private val w = 140.dpToPx()
        private val h = 115.dpToPx()

        @SuppressLint("ClickableViewAccessibility")
        override fun create(
                context: Context,
                x: Float,
                y: Float,
                listener: BodyTouchListener,
                photoIsBright: Boolean
        ): List<View> {

            val waistView = WaistView(context, null, 0, 0).apply {
                setPhotoIsBright(photoIsBright)
                layoutParams = ViewGroup.LayoutParams(w, h)
                requestLayout()

                translationX = x - w / 2
                translationY = y - h / 2
            }

            return listOf(waistView)
        }
    }

    class HipsBuilder : Base() {
        private val w = 140.dpToPx()
        private val h = 140.dpToPx()

        @SuppressLint("ClickableViewAccessibility")
        override fun create(
                context: Context,
                x: Float,
                y: Float,
                listener: BodyTouchListener,
                photoIsBright: Boolean
        ): List<View> {

            val waistView = HipsView(context, null, 0, 0).apply {
                setPhotoIsBright(photoIsBright)
                layoutParams = ViewGroup.LayoutParams(w, h)
                requestLayout()

                translationX = x - w / 2
                translationY = y - h / 2
            }

            return listOf(waistView)
        }
    }
}
