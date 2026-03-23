package art.intel.soft.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import art.intel.soft.R
import art.intel.soft.databinding.ViewSelColorBinding
import art.intel.soft.utils.throttleAction
import com.anadolstudio.mapper.implementation.selcolor.SelectiveColorFunction
import com.anadolstudio.mapper.implementation.selcolor.SelectiveColorFunction.SelectiveColorItem
import com.google.android.material.slider.Slider

class SelectiveColorView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val DELAY = 50L
        private const val BLACK = "Black"
    }

    private var red: SelectiveColorItem = SelectiveColorItem()
    private var green: SelectiveColorItem = SelectiveColorItem()
    private var blue: SelectiveColorItem = SelectiveColorItem()
    private var cyan: SelectiveColorItem = SelectiveColorItem()
    private var magenta: SelectiveColorItem = SelectiveColorItem()
    private var yellow: SelectiveColorItem = SelectiveColorItem()
    private var white: SelectiveColorItem = SelectiveColorItem()
    private var gray: SelectiveColorItem = SelectiveColorItem()
    private var black: SelectiveColorItem = SelectiveColorItem()

    private var currentColor: SelectiveColorItem = red
    private var currentColorMode: SelectiveColors = SelectiveColors.RED

    private var dataChangeListener: DataChangeListener? = null

    private val binding: ViewSelColorBinding =
            ViewSelColorBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.cyanSlider.setLabelFormatter { context.getString(R.string.title_cyan) }
        binding.magentaSlider.setLabelFormatter { context.getString(R.string.title_magenta) }
        binding.yellowSlider.setLabelFormatter { context.getString(R.string.title_yellow) }
        binding.blackSlider.setLabelFormatter { BLACK }

        binding.cyanSlider.addOnSliderTouchListener(SliderTouchListener(SettingsColors.CYAN))
        binding.cyanSlider.addThrottleDataChangeListener(
                predicate = { value -> value != currentColor.cyan },
                action = { value -> currentColor.cyan = value }
        )
        binding.magentaSlider.addOnSliderTouchListener(SliderTouchListener(SettingsColors.MAGENTA))
        binding.magentaSlider.addThrottleDataChangeListener(
                predicate = { value -> value != currentColor.magenta },
                action = { value -> currentColor.magenta = value }
        )
        binding.yellowSlider.addOnSliderTouchListener(SliderTouchListener(SettingsColors.YELLOW))
        binding.yellowSlider.addThrottleDataChangeListener(
                predicate = { value -> value != currentColor.yellow },
                action = { value -> currentColor.yellow = value }
        )
        binding.blackSlider.addOnSliderTouchListener(SliderTouchListener(SettingsColors.BLACK))
        binding.blackSlider.addThrottleDataChangeListener(
                predicate = { value -> value != currentColor.black },
                action = { value -> currentColor.black = value }
        )

    }

    private fun Slider.addThrottleDataChangeListener(
            predicate: (Int) -> Boolean,
            action: (Int) -> Unit
    ) {
        this.addOnChangeListener { _, value, fromUser ->
            val valueInt = value.toInt()

            if (predicate.invoke(valueInt) && fromUser) {
                throttleAction(DELAY) {
                    action.invoke(valueInt)
                    notifyListener()
                }
            }
        }
    }

    private inner class SliderTouchListener(val settingsColors: SettingsColors) : Slider.OnSliderTouchListener {

        override fun onStartTrackingTouch(slider: Slider) {
            setAlphaFocus(settingsColors)
            setBackgroundColor(Color.TRANSPARENT)
        }

        override fun onStopTrackingTouch(slider: Slider) {
            binding.cyanSliderContainer.alpha = 1F
            binding.magentaSliderContainer.alpha = 1F
            binding.yellowSliderContainer.alpha = 1F
            binding.blackSliderContainer.alpha = 1F
            setBackgroundColor(ContextCompat.getColor(context, R.color.colorAlphaWhite))
        }
    }

    private fun setAlphaFocus(settingsColors: SettingsColors) {
        binding.cyanSliderContainer.alpha = if (settingsColors == SettingsColors.CYAN) 1F else 0.1F
        binding.magentaSliderContainer.alpha = if (settingsColors == SettingsColors.MAGENTA) 1F else 0.1F
        binding.yellowSliderContainer.alpha = if (settingsColors == SettingsColors.YELLOW) 1F else 0.1F
        binding.blackSliderContainer.alpha = if (settingsColors == SettingsColors.BLACK) 1F else 0.1F
    }

    fun reset() {
        red = SelectiveColorItem()
        green = SelectiveColorItem()
        blue = SelectiveColorItem()
        cyan = SelectiveColorItem()
        magenta = SelectiveColorItem()
        yellow = SelectiveColorItem()
        white = SelectiveColorItem()
        gray = SelectiveColorItem()
        black = SelectiveColorItem()

        selectColor(currentColorMode)
    }

    fun resetTo(func: SelectiveColorFunction) {
        red = func.red
        green = func.green
        blue = func.blue
        cyan = func.cyan
        magenta = func.magenta
        yellow = func.yellow
        white = func.white
        gray = func.gray
        black = func.black

        selectColor(currentColorMode)
    }

    private fun notifyListener() {
        dataChangeListener?.onChange(
                SelectiveColorFunction(
                        red = red,
                        green = green,
                        blue = blue,
                        cyan = cyan,
                        magenta = magenta,
                        yellow = yellow,
                        white = white,
                        gray = gray,
                        black = black
                )
        )
    }

    fun setDataChangeListener(listener: DataChangeListener?) {
        dataChangeListener = listener
    }

    fun setDataChangeListener(listener: (SelectiveColorFunction) -> Unit) {
        dataChangeListener = object : DataChangeListener {
            override fun onChange(func: SelectiveColorFunction) {
                listener.invoke(func)
            }
        }
    }

    fun selectGreen() = selectColor(SelectiveColors.GREEN)
    fun selectBlue() = selectColor(SelectiveColors.BLUE)
    fun selectCyan() = selectColor(SelectiveColors.CYAN)
    fun selectMagenta() = selectColor(SelectiveColors.MAGENTA)
    fun selectYellow() = selectColor(SelectiveColors.YELLOW)
    fun selectWhite() = selectColor(SelectiveColors.WHITE)
    fun selectGray() = selectColor(SelectiveColors.GRAY)
    fun selectBlack() = selectColor(SelectiveColors.BLACK)
    fun selectRed() = selectColor(SelectiveColors.RED)

    private fun selectColor(selectiveColor: SelectiveColors) {
        currentColorMode = selectiveColor

        currentColor = when (selectiveColor) {
            SelectiveColors.GREEN -> green
            SelectiveColors.BLUE -> blue
            SelectiveColors.CYAN -> cyan
            SelectiveColors.MAGENTA -> magenta
            SelectiveColors.YELLOW -> yellow
            SelectiveColors.WHITE -> white
            SelectiveColors.GRAY -> gray
            SelectiveColors.BLACK -> black
            SelectiveColors.RED -> red
        }

        binding.cyanSlider.value = currentColor.cyan.toFloat()
        binding.magentaSlider.value = currentColor.magenta.toFloat()
        binding.yellowSlider.value = currentColor.yellow.toFloat()
        binding.blackSlider.value = currentColor.black.toFloat()
    }

    enum class SelectiveColors {
        GREEN,
        BLUE,
        CYAN,
        MAGENTA,
        YELLOW,
        WHITE,
        GRAY,
        BLACK,
        RED
    }

    private enum class SettingsColors {
        CYAN,
        MAGENTA,
        YELLOW,
        BLACK
    }

    interface DataChangeListener {

        fun onChange(func: SelectiveColorFunction)
    }

}
