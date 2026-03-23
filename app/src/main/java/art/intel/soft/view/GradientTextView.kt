package art.intel.soft.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import art.intel.soft.R

class GradientTextView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var startColor = Color.BLACK
    private var endColor = Color.BLACK

    init {
        initAttr(attrs, context)
    }

    private fun initAttr(attrs: AttributeSet?, context: Context) {
        if (attrs == null) return

        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.GradientTextView)

        startColor = typeArray.getColor(R.styleable.GradientTextView_startColor, Color.BLACK)
        endColor = typeArray.getColor(R.styleable.GradientTextView_endColor, Color.BLACK)

        typeArray.recycle()
    }

    @SuppressLint("DrawAllocation")
    public override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (changed) {
            paint.shader = LinearGradient(0F, 0F, width.toFloat(), 0F, startColor, endColor, Shader.TileMode.CLAMP)
        }
    }
}
