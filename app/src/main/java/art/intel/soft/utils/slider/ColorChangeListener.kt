package art.intel.soft.utils.slider

import com.divyanshu.colorseekbar.ColorSeekBar

class ColorChangeListener(private val listener: (color: Int) -> Unit) : ColorSeekBar.OnColorChangeListener {

    override fun onColorChangeListener(color: Int) = listener.invoke(color)
}
