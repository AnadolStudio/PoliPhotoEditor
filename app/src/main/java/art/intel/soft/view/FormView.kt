package art.intel.soft.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.Shader.TileMode.CLAMP
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import art.intel.soft.R
import art.intel.soft.databinding.ViewSplashBinding
import art.intel.soft.extention.correctInDiapason
import art.intel.soft.ui.edit.ViewState
import art.intel.soft.utils.bitmaputils.blur
import art.intel.soft.utils.bitmaputils.changeSaturation
import art.intel.soft.utils.bitmaputils.getCopyBitmap
import art.intel.soft.utils.changeViewSize
import art.intel.soft.utils.touchlisteners.SplashTouchListener
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class FormView(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {
    companion object {
        const val BLUR_BACK = 0
        const val MONOCHROME_BACK = 1
        const val COMMON_BACK = 2
        const val MAX_SATURATION = 1F
        const val MIN_SATURATION = 0F
        const val MAX_BLUR = 25F
        const val MIN_BLUR = 1F
        const val TAG: String = "SplashView"
    }

    private var listener: ZoomListener? = null
    private var lastChangeType = -1

    var workspace: Point = Point(500, 500)
    var frameColor: Int = Color.WHITE
        set(value) {
            field = value
            if (::maskDrawable.isInitialized) maskBitmap?.let { createMask(it) }
        }

    var blurRadius: Float = 12F
        set(value) {
            if (value == field) return
            field = value.correctInDiapason(min = MIN_BLUR, max = MAX_BLUR)

            if (lastChangeType != BLUR_BACK) {
                semiFinishedBitmap = getMonochromeEffect(sourcePreview!!)
            }
            lastChangeType = BLUR_BACK
            binding.backgroundIv.setImageBitmap(getBlurEffect(semiFinishedBitmap!!))
        }

    var saturationRatio: Float = 1F
        set(value) {
            if (value == field) return
            field = value.correctInDiapason(min = MIN_SATURATION, max = MAX_SATURATION)

            if (lastChangeType != MONOCHROME_BACK) {
                semiFinishedBitmap = getBlurEffect(sourcePreview!!)
            }
            lastChangeType = MONOCHROME_BACK
            binding.backgroundIv.setImageBitmap(getMonochromeEffect(semiFinishedBitmap!!))
        }

    var backgroundEffect = 0
        set(value) {
            field = value
            sourceOriginal?.let { setBackgroundBitmap(it) }
        }

    private var sourceOriginal: Bitmap? = null
    private var sourcePreview: Bitmap? = null
    private var semiFinishedBitmap: Bitmap? = null
    private var viewState: ViewState? = null
    private var maskBitmap: Bitmap? = null
    private var splashBitmap: Bitmap? = null
    private lateinit var maskDrawable: MaskBitmapShader
    private val maxSide: Int
        get() = with(binding.backgroundIv) {
            minOf(width, height, 1000)
        }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null)

    constructor(context: Context, workspace: Point, backgroundEffect: Int) : this(context, null) {
        this.workspace = workspace
        this.backgroundEffect = backgroundEffect
    }

    private val binding: ViewSplashBinding = ViewSplashBinding.bind(
            LayoutInflater.from(context).inflate(R.layout.view_splash, this, true)
    )

    init {
        initializeAttributes(attrs)
        setWillNotDraw(false)
    }

    fun isReady() = sourceOriginal != null && maskBitmap != null && splashBitmap != null

    private fun initializeAttributes(attrs: AttributeSet?) {

        if (attrs == null) return
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.SplashView)

        backgroundEffect = typeArray.getInt(R.styleable.SplashView_backgroundEffect, BLUR_BACK)
        typeArray.recycle()
    }

    fun setBackgroundBitmap(bitmap: Bitmap) {
        sourceOriginal = bitmap
        sourcePreview = getCopyBitmap(bitmap, 1250, 1250)
        binding.backgroundIv.setImageBitmap(getBackgroundWithEffect(sourcePreview!!))
    }

    private fun getBackgroundWithEffect(bitmap: Bitmap): Bitmap = when (backgroundEffect) {
        BLUR_BACK -> getBlurEffect(bitmap)
        MONOCHROME_BACK -> getMonochromeEffect(bitmap)
        else /*COMMON_BACK*/ -> getBlurEffect(bitmap).let(this::getMonochromeEffect)
    }

    fun setSplashBitmap(splash: Bitmap, mask: Bitmap) {
        this.splashBitmap = if (::maskDrawable.isInitialized) {
            Bitmap.createScaledBitmap(
                    splash, binding.splashIv.width, binding.splashIv.height, true
            )
        } else splash

        this.maskBitmap = if (::maskDrawable.isInitialized) {
            Bitmap.createScaledBitmap(
                    mask, binding.splashIv.width, binding.splashIv.height, true
            )
        } else mask

        viewState?.rebootToDefault()
        if (::maskDrawable.isInitialized) createMask(this.maskBitmap!!)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (!::maskDrawable.isInitialized && maskBitmap != null) init()
    }

    private fun init() {
        val touchListener = SplashTouchListener(true) { maskBitmap?.let { createMask(it) } }
        binding.splashIv.setOnTouchListener(touchListener)
        correctSourceScale()
        binding.backgroundIv.visibility = INVISIBLE

        binding.splashIv.apply {
            visibility = INVISIBLE
            layoutParams.width = maskBitmap!!.width
            layoutParams.height = maskBitmap!!.height
            requestLayout()

            post {
                viewState = ViewState(binding.splashIv)
                createMask(this@FormView.maskBitmap!!)
                binding.backgroundIv.visibility = VISIBLE
                visibility = VISIBLE
            }
        }
    }

    private fun correctSourceScale() {
        sourcePreview = sourcePreview?.let {
            Bitmap.createScaledBitmap(
                    it, binding.backgroundIv.width, binding.backgroundIv.height, true
            )
        }
        changeViewSize(sourcePreview!!, binding.backgroundIv, workspace)
        splashBitmap = splashBitmap?.let { Bitmap.createScaledBitmap(it, maxSide, maxSide, true) }
        maskBitmap = maskBitmap?.let { Bitmap.createScaledBitmap(it, maxSide, maxSide, true) }
    }

    private fun createMask(mask: Bitmap) {
        sourcePreview?.also {
            maskDrawable = MaskBitmapShader(mask, createBitmapForMask(it, mask.width, mask.height))
        }
        binding.splashIv.setImageDrawable(maskDrawable)
    }

    private fun createBitmapForMask(
            src: Bitmap, maskW: Int, maskH: Int, scale: Float = 1F
    ): Bitmap {
        val matrix = Matrix()
        binding.splashIv.run { matrix.setRotate(-rotation, width / 2F, height / 2F) }

        val result = Bitmap.createBitmap(maskW, maskW, Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.isHardwareAccelerated

        val scaleBitmap = Bitmap.createScaledBitmap(
                getCropBitmap(src, maskW, maskH, scale), maskW, maskW, true
        )
        canvas.drawBitmap(scaleBitmap, matrix, null)

        return result
    }

    private fun getCropBitmap(src: Bitmap, cropW: Int, cropH: Int, scale: Float = 1F): Bitmap =
            with(binding.splashIv) {
                var cropWidth = (cropW * scaleX * scale).roundToInt()
                var cropHeight = (cropH * scaleX * scale).roundToInt()

                val maskWidth = cropWidth
                val maskHeight = cropHeight

                val x = ((x - (cropW * scaleX - cropW) / 2F) * scale).roundToInt()
                val y = ((y - (cropH * scaleX - cropH) / 2F) * scale).roundToInt()

                cropWidth = correctMeasure(maskWidth, src.width, x, cropWidth)
                cropHeight = correctMeasure(maskHeight, src.height, y, cropHeight)

                val result = Bitmap.createBitmap(maskWidth, maskHeight, Config.ARGB_8888)
                val canvas = Canvas(result)
                canvas.isHardwareAccelerated

                if (cropWidth > 0 && cropHeight > 0) {
                    val left = min(src.width - cropWidth, max(x, 0))
                    val top = min(src.height - cropHeight, max(y, 0))

                    val crop = Bitmap.createBitmap(src, left, top, cropWidth, cropHeight, null, true)
                    canvas.drawBitmap(crop, abs(min(0, x)).toFloat(), abs(min(0, y)).toFloat(), null)
                    crop.recycle()
                }
                result
            }

    private fun correctMeasure(mask: Int, src: Int, startPoint: Int, crop: Int): Int {
        var result = crop

        if (mask <= src) {
            if (src - mask < startPoint) {
                result -= startPoint - (src - mask)
            } else if (startPoint < 0)
                result += startPoint

            result = min(crop, src - abs(startPoint))
        } else {
            result = src
            if (startPoint > 0) result -= startPoint
        }
        return result
    }

    private fun getBlurEffect(bitmap: Bitmap): Bitmap = blur(context, bitmap, blurRadius)

    private fun getMonochromeEffect(bitmap: Bitmap): Bitmap = changeSaturation(saturationRatio, bitmap)

    fun process(): Bitmap {
        if (sourceOriginal == null) throw IllegalArgumentException()
        zoomFirebaseEvent()

        return with(sourceOriginal!!) {
            val result = Bitmap.createBitmap(width, height, Config.ARGB_8888)
            val canvas = Canvas(result)

            canvas.drawBitmap(getBackgroundWithEffect(this), 0F, 0F, null)

            val scalePreview = (this.width.toFloat() / sourcePreview!!.width)
            val scale = (binding.splashIv.scaleX * scalePreview)

            val pictureToMask = getCropBitmap(
                    this@with, maskBitmap!!.width, maskBitmap!!.height, scalePreview
            )

            val scaleMask = with(maskBitmap!!) {
                createScaleAndRotateBitmap(this, scale, binding.splashIv.rotation)
            }
            val scaleSplash = with(splashBitmap!!) {
                createScaleAndRotateBitmap(this, scale, binding.splashIv.rotation)
            }

            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = BitmapShader(pictureToMask, CLAMP, CLAMP)
            }

            with(binding.splashIv) {
                val x = ((x - (width * scaleX - width) / 2F) * scalePreview).roundToInt()
                val y = ((y - (height * scaleX - height) / 2F) * scalePreview).roundToInt()
                val cropWidth =
                        correctMeasure(scaleMask.width, sourceOriginal!!.width, x, scaleMask.width)
                val cropHeight =
                        correctMeasure(scaleMask.height, sourceOriginal!!.height, y, scaleMask.height)

                val left = min(sourceOriginal!!.width - cropWidth, x).toFloat()
                val top = min(sourceOriginal!!.height - cropHeight, y).toFloat()

                canvas.drawBitmap(scaleMask, left, top, paint)
                canvas.drawBitmap(scaleSplash, left, top, colorPaint())
            }
            result
        }
    }

    private fun zoomFirebaseEvent() {
        listener?.zoom(binding.splashIv.scaleX)
    }

    private fun createScaleAndRotateBitmap(
            src: Bitmap, scale: Float, rotate: Float, config: Config = src.config
    ): Bitmap {
        val temp = Bitmap.createScaledBitmap(
                src, (src.width * scale).toInt(), (src.height * scale).toInt(), true
        )

        val matrix = Matrix().apply { setRotate(rotate, temp.width / 2F, temp.height / 2F) }

        val result = Bitmap.createBitmap(temp.width, temp.height, config)
        val canvas = Canvas(result)

        canvas.drawBitmap(temp, matrix, null)
        temp.recycle()

        return result
    }

    interface ZoomListener {
        fun zoom(scale: Float)
    }

    fun setZoomListener(zoomListener: ZoomListener) {
        this.listener = zoomListener
    }

    inner class MaskBitmapShader(private val maskBitmap: Bitmap, val src: Bitmap) : Drawable() {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        init {
            paint.shader = BitmapShader(src, CLAMP, CLAMP)
        }

        override fun draw(canvas: Canvas) {
            canvas.drawBitmap(maskBitmap, 0F, 0F, paint)
            splashBitmap?.let { canvas.drawBitmap(it, 0F, 0F, colorPaint()) }
        }

        override fun setAlpha(i: Int) {
            paint.alpha = i
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

        override fun getOpacity(): Int = PixelFormat.UNKNOWN
    }

    private fun colorPaint(): Paint {
        val colorPaint = Paint()
        colorPaint.color = frameColor
        return colorPaint
    }
}
