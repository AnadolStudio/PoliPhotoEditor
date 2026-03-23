package art.intel.soft.ui.edit

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentTransaction
import art.intel.soft.R
import art.intel.soft.base.OnBackPressed
import art.intel.soft.base.activity.AdActivityWithBanner
import art.intel.soft.base.firebase.AnalyticEventsUtil
import art.intel.soft.base.firebase.events.implementation.ClickEvent
import art.intel.soft.databinding.ActivityEditBinding
import art.intel.soft.extention.baseSubscribe
import art.intel.soft.extention.singleFrom
import art.intel.soft.ui.Processor
import art.intel.soft.ui.ProcessorListener
import art.intel.soft.ui.edit.ModeEdit.MODE_BACKGROUND
import art.intel.soft.ui.edit.ModeEdit.MODE_BODY
import art.intel.soft.ui.edit.ModeEdit.MODE_BRUSH
import art.intel.soft.ui.edit.ModeEdit.MODE_COLLAGE
import art.intel.soft.ui.edit.ModeEdit.MODE_CROP
import art.intel.soft.ui.edit.ModeEdit.MODE_EFFECT
import art.intel.soft.ui.edit.ModeEdit.MODE_FILTER
import art.intel.soft.ui.edit.ModeEdit.MODE_FORM
import art.intel.soft.ui.edit.ModeEdit.MODE_FRAME
import art.intel.soft.ui.edit.ModeEdit.MODE_IMPROVE
import art.intel.soft.ui.edit.ModeEdit.MODE_MAIN
import art.intel.soft.ui.edit.ModeEdit.MODE_STICKER
import art.intel.soft.ui.edit.ModeEdit.MODE_TEXT
import art.intel.soft.ui.edit.background.BackgroundCutEditFragment
import art.intel.soft.ui.edit.body.BodyEditFragment
import art.intel.soft.ui.edit.brush.BrushEditFragment
import art.intel.soft.ui.edit.collage.CollageEditFragment
import art.intel.soft.ui.edit.crop.CropEditFragment
import art.intel.soft.ui.edit.effect.EffectEditFragment
import art.intel.soft.ui.edit.filter.FilterEditFragment
import art.intel.soft.ui.edit.form.FormEditFragment
import art.intel.soft.ui.edit.frame.FrameEditFragment
import art.intel.soft.ui.edit.improve.ImproveEditFragment
import art.intel.soft.ui.edit.main.MainEditFragment
import art.intel.soft.ui.edit.main.recycler.FunctionItem
import art.intel.soft.ui.edit.main.recycler.FunctionItem.BACKGROUND
import art.intel.soft.ui.edit.main.recycler.FunctionItem.BODY
import art.intel.soft.ui.edit.main.recycler.FunctionItem.BRUSH
import art.intel.soft.ui.edit.main.recycler.FunctionItem.COLLAGE
import art.intel.soft.ui.edit.main.recycler.FunctionItem.CROP
import art.intel.soft.ui.edit.main.recycler.FunctionItem.EFFECT
import art.intel.soft.ui.edit.main.recycler.FunctionItem.FILTER
import art.intel.soft.ui.edit.main.recycler.FunctionItem.FORM
import art.intel.soft.ui.edit.main.recycler.FunctionItem.FRAME
import art.intel.soft.ui.edit.main.recycler.FunctionItem.IMPROVE
import art.intel.soft.ui.edit.main.recycler.FunctionItem.STICKER
import art.intel.soft.ui.edit.main.recycler.FunctionItem.TEXT
import art.intel.soft.ui.edit.sticker.StickerEditFragment
import art.intel.soft.ui.edit.text.TextEditFragment
import art.intel.soft.ui.main.OpenEditType
import art.intel.soft.ui.save.SaveActivity
import art.intel.soft.utils.DisplayUtil
import art.intel.soft.utils.ImageLoader
import art.intel.soft.utils.PermissionHelper.REQUEST_STORAGE_PERMISSION
import art.intel.soft.utils.PermissionHelper.WRITE_GALLERY_PERMISSION
import art.intel.soft.utils.PermissionHelper.hasPermission
import art.intel.soft.utils.PermissionHelper.requestPermission
import art.intel.soft.utils.PermissionHelper.showSettingsSnackbar
import art.intel.soft.utils.bitmaputils.captureView
import art.intel.soft.utils.bitmaputils.getBitmapFromImageView
import art.intel.soft.utils.bitmaputils.getCopyBitmap
import art.intel.soft.utils.bitmaputils.saveImage
import art.intel.soft.utils.changeViewSize
import art.intel.soft.utils.fitViewToEdge
import art.intel.soft.utils.touchlisteners.BeforeAfterTouchListener
import art.intel.soft.utils.touchlisteners.ImageTouchListener
import art.intel.soft.view.BaseToolbar
import art.intel.soft.view.SelectiveColorView
import com.canhub.cropper.CropImageView
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.view.PhotoEditorView
import kotlin.math.max

class EditActivity : AdActivityWithBanner(), ImageLoader.SimpleRequestListener<Bitmap>, ProcessorListener,
        FragmentCreatedCallback {
    companion object {
        private const val TAG = "EditActivity"
        private const val IMAGE_PATH = "image_path"
        private const val COLLAGE_PHOTOS = "collage_photos"

        fun start(context: Context, type: OpenEditType, path: String?, collagePhotos: List<String>? = null) =
                Intent(context, EditActivity::class.java).run {
                    putExtra(OpenEditType::class.java.name, type)
                    putExtra(IMAGE_PATH, path)
                    collagePhotos?.let { putStringArrayListExtra(COLLAGE_PHOTOS, ArrayList(collagePhotos)) }
                    context.startActivity(this)
                }
    }

    lateinit var editHelper: EditViewHelper
        private set
    lateinit var currentMode: ModeEdit
        private set

    private val binding: ActivityEditBinding by lazy { ActivityEditBinding.inflate(layoutInflater) }
    private lateinit var photoEditor: PhotoEditor
    private lateinit var originalBitmap: Bitmap
    private var frameContentState: ViewState? = null

    private lateinit var processor: Processor

    fun workspaceSize(): Point {
        return DisplayUtil.workspaceSize(this, binding.navigationTb, binding.toolbarFragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.also { //Создаю активити заново ибо сохранять большой bitmap - проблема
            finish()
            startActivity(intent)
        }
        setContentView(binding.root)
        initView()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        processor = Processor(this)
        editHelper = EditViewHelper()
        initAd()
        with(binding) {
            openMainEditFragment()
            showLoadingDialog()
            val path = intent.getStringExtra(IMAGE_PATH) ?: throw IllegalArgumentException("Path is null")

            ImageLoader.loadImageWithoutCache(photoEditorView.source, path, this@EditActivity)
            initPhotoEditor(photoEditorView)
        }
    }

    private fun openMainEditFragment() {
        val type = intent.getSerializableExtra(OpenEditType::class.java.name) as OpenEditType
        setEditFragment(MODE_MAIN, MainEditFragment.newInstance(type, FunctionItemClick()))
    }

    private fun trySaveImage() {
        if (hasPermission(this, WRITE_GALLERY_PERMISSION)) saveImage()
        else requestPermission(this, WRITE_GALLERY_PERMISSION, REQUEST_STORAGE_PERMISSION)
    }

    fun initPhotoEditor(photoEditorView: PhotoEditorView) {
        photoEditor = PhotoEditor.Builder(this, photoEditorView)
                .setClipSourceImage(true)
                .build()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults.any { it == PERMISSION_GRANTED }) {
                    saveImage()// permission granted
                } else {
                    // permission denied
                    ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_GALLERY_PERMISSION[1]) // TODO
                            .also {
                                if (!it) showSettingsSnackbar(this, binding.root)
                                else finish() // Закрывает activity
                            }
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun replaceFragment(fragment: BaseEditFragment) {
        addBackPressedListener(fragment as OnBackPressed)
        processor.processEdit = fragment

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.toolbar_fragment, fragment, javaClass.name)
                .addToBackStack(javaClass.name)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
    }

    private fun rebootSettingsEditPanel() {
        editHelper.setVisibleMainPanel(true)
        binding.container.isVisible = false
        binding.beforeAfterBtn.isVisible = false
        binding.cropIv.isVisible = false
        editHelper.clearSupportImage()
        editHelper.clearContainer()
        binding.cropIv.clearImage()
        binding.selectiveColorView.isVisible = false
        photoEditor.clearAllViews()
        binding.selectiveColorView.reset()
        photoEditor.setBrushDrawingMode(false)
        binding.photoEditorView.source.clearColorFilter()
    }

    private fun getPreviousFragment(): BaseEditFragment = with(supportFragmentManager) {
        val tag = getBackStackEntryAt(max(0, backStackEntryCount - 2)).name
        findFragmentByTag(tag) as BaseEditFragment
    }

    override fun onBackPressed() {
        val lastListener = backPressedInnerListeners.lastOrNull() ?: return
        if (lastListener.onBackPressed()) onBackPressed(lastListener)
    }

    override fun onBackPressed(listener: OnBackPressed) {
        removeBackPressedListener(listener)
        supportFragmentManager.popBackStack()
        rebootSettingsEditPanel()
        rebootToolbarActions()
        editHelper.rebootToOriginalImage()
    }

    override fun onResourceReady(resource: Bitmap) {
        originalBitmap = resource
        binding.photoEditorView.source.scaleType = ScaleType.FIT_CENTER
        hideLoadingDialog()
        editHelper.bitmapIsReady()
    }

    override fun onLoadFailed(e: Exception) {
        hideLoadingDialog()
        showToast(R.string.edit_error_cant_open_file)
        finish()
    }

    private fun setEditFragment(mode: ModeEdit, fragment: BaseEditFragment) {
        changeModeEdit(mode)
        replaceFragment(fragment)
    }

    private fun changeModeEdit(mode: ModeEdit) {
        this.currentMode = mode
        binding.navigationTb.setRightButtonIcon(
                ContextCompat.getDrawable(
                        this, if (mode == MODE_MAIN) R.drawable.ic_save else R.drawable.ic_accept
                )
        )
    }

    private fun rebootToolbarActions() {
        binding.navigationTb.setLeftButtonAction { onBackPressed() }
        binding.navigationTb.setLeftButtonIcon(AppCompatResources.getDrawable(this, R.drawable.ic_arrow_back))
        binding.navigationTb.setRightButtonAction { trySaveImage() }
        binding.navigationTb.setRightButtonIcon(AppCompatResources.getDrawable(this, R.drawable.ic_save))
    }

    private fun startSaveActivity(imagePath: String) {
        showInterstitial(
                clickAction = {},
                showedAction = {},
                action = {
                    AnalyticEventsUtil.logEvent(ClickEvent.Edit.Save())
                    showToast(R.string.save_label_saved)
                    SaveActivity.start(this@EditActivity, imagePath)
                }
        )
    }

    override fun onProcess(result: Bitmap) {
        rebootSettingsEditPanel()
        editHelper.resizeMainPanel(result)
        binding.photoEditorView.source.setImageBitmap(result)
//        originalBitmap.recycle() Ошибка в SaveActivity при onSupportNavigateUp()
        originalBitmap = result
        backPressedInnerListeners.lastOrNull()?.also(this::removeBackPressedListener)
        supportFragmentManager.popBackStack()
        backStackFragment()
        hideLoadingDialog()
        rebootToolbarActions()
        intent.getStringArrayListExtra(COLLAGE_PHOTOS)?.let { intent.putStringArrayListExtra(COLLAGE_PHOTOS, null) }
    }

    private fun backStackFragment() {
        val bottomFragment = getPreviousFragment()
        if (bottomFragment is MainEditFragment) changeModeEdit(MODE_MAIN)
    }

    private fun saveImage() {
        val result: Bitmap = originalBitmap

        singleFrom { saveImage(this, result) }
                .baseSubscribe(
                        onSubscribe = { showLoadingDialog() },
                        onSuccess = this::startSaveActivity,
                        onError = { showToast(R.string.error_failed_save_image) },
                        onFinally = { hideLoadingDialog() }
                )
    }

    override fun fragmentCreated() = when (currentMode) {
        MODE_EFFECT, MODE_FRAME, MODE_COLLAGE -> editHelper.setupSupportImage(currentMode)
        MODE_CROP -> editHelper.setVisibleMainPanel(false)
        MODE_FORM -> {
            editHelper.setupContainer()
            changeViewSize(originalBitmap, binding.container, workspaceSize())
        }
        MODE_BODY, MODE_FILTER, MODE_IMPROVE -> {
            editHelper.setupContainer()
            changeViewSize(originalBitmap, binding.container, workspaceSize())
            editHelper.setVisibleMainPanel(false)
        }
        else -> Unit
    }

    inner class FunctionItemClick : Action<FunctionItem> {
        override fun action(item: FunctionItem) {
            val callback = this@EditActivity

            val collagePhotoList = intent.getStringArrayListExtra(COLLAGE_PHOTOS)

            when (item) {
                COLLAGE -> setEditFragment(MODE_COLLAGE, CollageEditFragment.newInstance(callback, collagePhotoList))
                FRAME -> setEditFragment(MODE_FRAME, FrameEditFragment.newInstance(callback))
                BACKGROUND -> setEditFragment(MODE_BACKGROUND, BackgroundCutEditFragment.newInstance())
                FILTER -> setEditFragment(MODE_FILTER, FilterEditFragment.newInstance(callback))
                EFFECT -> setEditFragment(MODE_EFFECT, EffectEditFragment.newInstance(callback))
                FORM -> setEditFragment(MODE_FORM, FormEditFragment.newInstance(callback))
                BODY -> setEditFragment(MODE_BODY, BodyEditFragment.newInstance(callback))
                TEXT -> setEditFragment(MODE_TEXT, TextEditFragment.newInstance())
                STICKER -> setEditFragment(MODE_STICKER, StickerEditFragment.newInstance())
                IMPROVE -> setEditFragment(MODE_IMPROVE, ImproveEditFragment.newInstance(callback))
                BRUSH -> setEditFragment(MODE_BRUSH, BrushEditFragment.newInstance())
                CROP -> setEditFragment(MODE_CROP, CropEditFragment.newInstance(callback))
            }
        }
    }

    inner class EditViewHelper {

        private val COLLAGE_INIT_DELAY = 100L // Во избежания мельканий

        private var bitmapReadyListener: (() -> Unit)? = null

        var hasChanges: Boolean = false
            private set

        fun setBitmapReadyListener(listener: () -> Unit) {
            bitmapReadyListener = listener
        }

        fun currentSizeOfMainePanel(): Point = Point(mainImageView().width, mainImageView().height)

        fun photoEditorView(): PhotoEditorView = binding.photoEditorView

        fun mainImageView(): ImageView = binding.photoEditorView.source

        fun supportImageView(): ImageView = binding.supportIv

        fun cropView(): CropImageView = binding.cropIv

        fun workSpace(): Point = workspaceSize()

        fun getPreviewOriginalBitmap(): Bitmap = getCopyOriginalBitmap(1250, 1250)

        fun getCopyOriginalBitmap(): Bitmap = getCopyOriginalBitmap(0, 0)

        fun getCopyOriginalBitmap(reqW: Int, reqH: Int): Bitmap = getCopyBitmap(originalBitmap, reqW, reqH)

        fun getOriginalBitmap(): Bitmap = originalBitmap

        fun currentBitmap(): Bitmap = getBitmapFromImageView(mainImageView())!!

        fun applyProcess() {
            with(binding) {
                showLoadingDialog()
                this@EditActivity.photoEditor.clearHelperBox()

                val result: Bitmap = Bitmap.createBitmap(currentBitmap()) // default

                when (currentMode) {
                    MODE_COLLAGE, MODE_FRAME ->
                        processor.process(captureView(container), captureView(supportIv))
                    MODE_FILTER, MODE_IMPROVE ->
                        processor.process(originalBitmap, null)
                    MODE_BACKGROUND ->
                        processor.process(result, photoEditorBitmap())
                    MODE_EFFECT ->
                        processor.process(captureView(photoEditorView.source), captureView(supportIv))
                    MODE_STICKER, MODE_TEXT ->
                        processor.process(captureView(photoEditorView.source), photoEditorBitmap())
                    MODE_BRUSH ->
                        processor.process(captureView(photoEditorView.source), drawingViewBitmap())
                    MODE_FORM ->
                        processor.process(captureView(container), null)
                    else ->
                        processor.process(result, null)
                }

                hasChanges = true
            }
        }

        fun resizeMainPanel(bitmap: Bitmap) {
            changeViewSize(bitmap, mainImageView(), workspaceSize())
            mainImageView().scaleType = ScaleType.FIT_CENTER
        }

        fun getPhotoEditor(): PhotoEditor {
            if (!::photoEditor.isInitialized) initPhotoEditor(binding.photoEditorView)

            return photoEditor
        }

        fun setImage(image: Bitmap?) {
            mainImageView().setImageBitmap(image)
            image?.also { resizeMainPanel(it) }
            mainImageView().scaleType = ScaleType.FIT_CENTER
        }

        fun setImage(path: String, scaleType: ScaleType?) {
            workspaceSize().apply {
                showLoadingDialog()
                photoEditorView().visibility = INVISIBLE

                ImageLoader.loadImageWithoutCache(
                        this@EditActivity,
                        path,
                        x,
                        y,
                        ImageLoader.ScaleType.FIT_CENTER
                ) { bitmap: Bitmap ->
                    mainImageView().setImageBitmap(bitmap)
                    resizeMainPanel(bitmap)

                    if (scaleType != null) mainImageView().scaleType = scaleType

                    photoEditorView().visibility = VISIBLE
                    hideLoadingDialog()
                }
            }
        }

        fun setupSupportImage(modeEdit: ModeEdit) {
            binding.supportIv.run {
                visibility = VISIBLE
                bringToFront()
                when (modeEdit) {
                    MODE_EFFECT -> {
                        scaleType = ScaleType.CENTER_CROP
                        adjustViewBounds = false
                        changeViewSize(this, mainImageView().width, mainImageView().height)
                    }
                    MODE_FRAME, MODE_COLLAGE -> {
                        setupContainer()
                        binding.container.addView(createFrameContent())
                        scaleType = ScaleType.FIT_CENTER
                        adjustViewBounds = true
                        changeViewSize(this, WRAP_CONTENT, WRAP_CONTENT)
                    }
                    else -> {}
                }
            }
        }

        fun addView(view: View) {
            binding.container.addView(view)
        }

        private fun createFrameContent(): ImageView = with(ImageView(this@EditActivity)) {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            scaleType = ScaleType.FIT_CENTER
            visibility = INVISIBLE
            requestLayout()
            setImageBitmap(originalBitmap)

            setOnTouchListener(ImageTouchListener(this, true))
            post { frameContentState = ViewState(this) }
            this
        }

        fun setupContainer() {
            binding.container.visibility = VISIBLE
        }

        fun container(): FrameLayout = binding.container

        fun clearSupportImage() {
            binding.supportIv.run {
                setImageBitmap(null)
                visibility = GONE
                changeViewSize(this, WRAP_CONTENT, WRAP_CONTENT)
            }
        }

        fun clearContainer() {
            frameContentState = null
            binding.container.apply {
                removeAllViews()
                layoutParams.width = MATCH_PARENT
                layoutParams.height = MATCH_PARENT
                requestLayout()
            }
        }

        fun setFrameImage(frame: String?) {
            if (frame == null) {
                frameContentState?.rebootToDefault()
                setSupportImage(null as Drawable?)

                binding.container.apply {
                    layoutParams.width = MATCH_PARENT
                    layoutParams.height = MATCH_PARENT
                    requestLayout()
                }

                return
            }

            showLoadingDialog()
            ImageLoader.loadImageWithoutCache(
                    this@EditActivity,
                    frame,
            ) {
                setContainerContent(it) { w, h -> setupFrameContent(w, h) }
                setVisibleMainPanel(false)
                hideLoadingDialog()
            }
        }

        fun setCollageImage(collage: Bitmap) {
            setVisibleMainPanel(false)
            clearContainer()
            setContainerContent(collage)
        }

        private fun setContainerContent(bitmap: Bitmap, post: ((Int, Int) -> Unit)? = null) {
            binding.supportIv.setImageBitmap(bitmap)
            binding.supportIv.post { resizeContainer(bitmap, post) }
        }

        fun addItemsInCollage(items: List<View>) {
            binding.container.isVisible = false
            for (item: View in items) binding.container.addView(item)
            binding.container.postDelayed({ binding.container.isVisible = true }, COLLAGE_INIT_DELAY)
        }

        fun showSelectiveColor(isShow: Boolean) {
            binding.selectiveColorView.isVisible = isShow
        }

        fun getSelectiveColor(): SelectiveColorView = binding.selectiveColorView

        fun setSupportImage(drawable: Drawable?) = binding.supportIv.setImageDrawable(drawable)

        fun setSupportImage(bitmap: Bitmap?) = binding.supportIv.setImageBitmap(bitmap)

        fun drawingViewBitmap(): Bitmap = captureView(binding.photoEditorView.drawingView)

        private fun photoEditorBitmap(): Bitmap = captureView(binding.photoEditorView)

        private fun resizeContainer(bitmap: Bitmap, post: ((Int, Int) -> Unit)? = null) =
                binding.apply {
                    supportIv.run {
                        changeViewSize(bitmap, this, workspaceSize())
                        post {
                            post?.invoke(width, height)
                            changeViewSize(container, width, height)
                        }
                    }
                }

        private fun setupFrameContent(width: Int, height: Int) = frameContentState?.run {
            rebootToDefault()
            fitViewToEdge(width, height, originalBitmap, view)
        }

        fun rebootToOriginalImage() {
            setImage(originalBitmap)
            mainImageView().clearColorFilter()
        }

        fun setVisibleMainPanel(isVisible: Boolean) {
            binding.photoEditorView.visibility = if (isVisible) VISIBLE else GONE
        }

        @SuppressLint("ClickableViewAccessibility")
        fun setOnTouchBeforeAfterListener(action: (Boolean) -> Unit) {
            binding.beforeAfterBtn.setOnTouchListener(BeforeAfterTouchListener(action::invoke))
        }

        fun setBeforeAfterButtonVisible(isVisible: Boolean) {
            binding.beforeAfterBtn.isVisible = isVisible
        }

        fun bitmapIsReady() = bitmapReadyListener?.invoke().also { bitmapReadyListener = null }

        fun toolbar(): BaseToolbar = binding.navigationTb

        fun trySaveImage() = this@EditActivity.trySaveImage()

    }
}
