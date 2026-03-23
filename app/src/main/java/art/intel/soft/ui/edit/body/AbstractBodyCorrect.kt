package art.intel.soft.ui.edit.body

import android.graphics.Point
import android.view.View
import ja.burhanrashid52.photoeditor.util.dpToPx
import org.wysaid.nativePort.CGEDeformFilterWrapper

abstract class AbstractBodyCorrect(val w: Float, val h: Float) {
    open var intensity: Float = 0.15f

    abstract var lastPoints: List<Point>

    abstract fun deform(
            view: View, deformWrapper: CGEDeformFilterWrapper, value: Int, currentValue: Int
    )

    abstract fun getPoints(view: View): List<Point>
}

class BreastCorrect(w: Float, h: Float) : AbstractBodyCorrect(w, h) {
    override var intensity: Float = 0.05F

    override lateinit var lastPoints: List<Point>

    override fun deform(view: View, deformWrapper: CGEDeformFilterWrapper, value: Int, currentValue: Int) {
        getPoints(view).forEach {
            val x = it.x.toFloat()
            val y = it.y.toFloat()
            val r = view.width / 2F

            if (value > currentValue) deformWrapper.bloatDeform(x, y, w, h, r, intensity)
            else deformWrapper.wrinkleDeform(x, y, w, h, r, intensity)
        }
    }

    override fun getPoints(view: View): List<Point> {
        val scale = view.scaleX
        val realW = view.width * scale
        val realH = view.height * scale

        val x = ((view.x - (realW - view.width) / 2)) + realW / 2
        val y = ((view.y - (realH - view.height) / 2)) + realH / 2

        return listOf(Point(x.toInt(), y.toInt()))
    }
}

open class WaistCorrect(w: Float, h: Float) : AbstractBodyCorrect(w, h) {
    companion object {
        private const val FORWARD_DEF = 7
    }

    override lateinit var lastPoints: List<Point>

    override fun deform(
            view: View, deformWrapper: CGEDeformFilterWrapper, value: Int, currentValue: Int
    ) {
        getPoints(view).forEachIndexed { index, it ->
            val x = it.x.toFloat()
            val y = it.y.toFloat()

            val correct = if (index % 2 == 0) -1 else 1
            val delta = FORWARD_DEF * correct

            val endX = if (value > currentValue) x - delta else x + delta
            val r = (view.height - 14.dpToPx() * 2F) / 2

            deformWrapper.forwardDeform(x, y, endX, y, w, h, r, intensity)
        }
    }

    override fun getPoints(view: View): List<Point> {
        val result = mutableListOf<Point>()

        val realW = view.width
        val realH = view.height
        val delta = 45

        var x = ((view.x - (realW - view.width) / 2)) + delta
        val y = ((view.y - (realH - view.height) / 2)) + realH / 2
        result.add(Point(x.toInt(), y.toInt()))

        x = ((view.x - (realW - view.width) / 2)) + realW - delta
        result.add(Point(x.toInt(), y.toInt()))

        return result
    }
}

class HipsCorrect(w: Float, h: Float) : WaistCorrect(w, h)
