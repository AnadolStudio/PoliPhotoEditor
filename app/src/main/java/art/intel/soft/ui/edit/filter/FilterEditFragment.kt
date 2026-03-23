package art.intel.soft.ui.edit.filter

import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import art.intel.soft.R
import art.intel.soft.base.firebase.AnalyticEventsUtil
import art.intel.soft.base.firebase.events.Screens
import art.intel.soft.base.firebase.events.implementation.OpenItemEvent
import art.intel.soft.databinding.FragmentEditFiltersBinding
import art.intel.soft.model.AssetsDirections
import art.intel.soft.ui.edit.FragmentCreatedCallback
import art.intel.soft.ui.edit.SurfaceEditFragment
import art.intel.soft.ui.edit.crop.CropViewModel
import art.intel.soft.ui.edit.filter.adapter.FilterAdapter
import art.intel.soft.ui.edit.filter.adapter.FilterDataItem
import art.intel.soft.ui.edit.filter.adapter.FilterGroup
import art.intel.soft.ui.edit.filter.group_filter_util.FilterGroupDslAdapter
import art.intel.soft.ui.edit.filter.group_filter_util.SelectGroupListener
import art.intel.soft.utils.AnimateUtil
import art.intel.soft.utils.AnimateUtil.hideTranslationVertical
import art.intel.soft.utils.AnimateUtil.showTranslationVertical
import art.intel.soft.utils.CGENativeLoadImageCallback
import art.intel.soft.utils.bitmaputils.centerCrop
import art.intel.soft.utils.recyclerview_util.ScrollToFirstLinearLayoutManager
import art.intel.soft.utils.setup
import art.intel.soft.view.BaseToolbar
import com.google.android.material.slider.LabelFormatter
import org.wysaid.nativePort.CGENativeLibrary
import kotlin.math.max

class FilterEditFragment : SurfaceEditFragment() {

    override val toolbarTitleId: Int = R.string.edit_filters_title

    companion object {

        fun newInstance(callback: FragmentCreatedCallback?) = FilterEditFragment().apply {
            this.callback = callback
        }
    }

    override fun provideScreenName(): Screens = Screens.FILTERS

    private val viewModel: FilterViewModel by viewModels()
    private lateinit var binding: FragmentEditFiltersBinding
    private lateinit var adapter: FilterAdapter
    private lateinit var selectGroupListener: SelectGroupListener
    private var currentEffectConfig: FilterDataItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CGENativeLibrary.setLoadImageCallback(
                CGENativeLoadImageCallback(requireContext(), AssetsDirections.FILTER_ASSETS_DIR),
                Object()
        ) // Для загрузки ассетов из библиотеки
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditFiltersBinding.inflate(inflater, container, false)

        val thumbnailBitmap = centerCrop(editor().getCopyOriginalBitmap(400, 400)).run {
            val maxSide = max(width, height)

            return@run Bitmap.createScaledBitmap(this, maxSide, maxSide, true)
        }

        setupFilterGroup()

        binding.apply {
            adapter = FilterAdapter(
                    data = FilterDataItem.getFilterDataItemWithoutOriginal(),
                    detailable = { filterItem ->
                        recyclerView.smoothScrollToPosition(filterItem.ordinal - 1) // plus original index
                        previewFilter(filterItem)
                    },
                    intensityAction = { showIntensityState(true) },
                    thumbnailBitmap = thumbnailBitmap
            )
            recyclerView.adapter = adapter
            recyclerView.layoutManager = ScrollToFirstLinearLayoutManager(requireContext())

            setupOriginalButton(thumbnailBitmap)

            viewSupportEdit.slider.apply {
                setup(0F, 1F, 1F)
                labelBehavior = LabelFormatter.LABEL_GONE
                addOnChangeListener { _, value, _ ->
                    currentEffectConfig?.intensity = value
                    surfaceView.setFilterIntensity(value)
                }
            }

            viewSupportEdit.denyButton.isVisible = false
            viewSupportEdit.acceptButton.isVisible = false
        }

        return binding.root
    }

    private fun FragmentEditFiltersBinding.setupOriginalButton(thumbnailBitmap: Bitmap?) {
        originalImage.imageView.setImageBitmap(thumbnailBitmap)
        originalImage.textView.text = FilterDataItem.ORIGINAL.name.uppercase()
        originalImage.textView.background = ColorDrawable(FilterDataItem.ORIGINAL.group.color.toColorInt())

        originalImage.root.setOnClickListener {
            previewFilter(FilterDataItem.ORIGINAL)
            adapter.clearSelectedItem()
        }
    }

    private fun showIntensityState(isShow: Boolean, needClearIntensity: Boolean = false) {
        val action: () -> Unit = { surfaceView.post { surfaceView.updateLayoutSize() } }

        if (isShow) {
            binding.mainGroup.hideTranslationVertical(AnimateUtil.Vertical.TO_BOTTOM, View.INVISIBLE, action)
            binding.intensitySliderContainer.showTranslationVertical(AnimateUtil.Vertical.TO_TOP)
        } else {
            binding.mainGroup.showTranslationVertical(AnimateUtil.Vertical.TO_TOP)
            binding.intensitySliderContainer.hideTranslationVertical(
                    AnimateUtil.Vertical.TO_BOTTOM,
                    View.INVISIBLE,
                    action
            )
        }

        if (needClearIntensity) {
            clearIntensity()
        }
    }

    private fun setupFilterGroup() {
        selectGroupListener = SelectGroupListener(binding.recyclerView)

        val dslAdapter = FilterGroupDslAdapter(
                onSelect = { index, _ ->
                    selectGroupListener.scrollToPosition(
                            FilterDataItem.getFilterDataItemWithoutOriginal()
                                    .first { it.group == FilterGroup.values()[index + 1] }
                                    .ordinal - 1 // Первая позиция всегда видна частично, поэтому тут мотаю до 2 позици в recyclerView
                    )
                }
        ).setup()

        binding.filterGroupList.adapter = dslAdapter

        selectGroupListener.setOnScrollListener { recyclerView ->
            if (recyclerView.childCount >= 2) {
                val firstItem = recyclerView.getChildAt(1)
                val currentGroup = adapter.getGroupFromIndex(recyclerView.getChildAdapterPosition(firstItem))
                dslAdapter.getTabFromPosition(0)
                        ?.setCurrentItem(currentGroup.ordinal - 1) // Т.к. не включается Original
            }
        }

    }

    override fun onSetupToolbar(toolbar: BaseToolbar) {
        super.onSetupToolbar(toolbar)
        toolbar.setRightButtonAction(this::applyChanges)
    }

    private fun applyChanges() {
        if (!hasChanges) {
            nothingIsSelectedToast()

            return
        }

        if (binding.intensitySliderContainer.isVisible) {
            showIntensityState(isShow = false)
            return
        }

        showApplyDialog()
    }

    override fun onBackPressed(): Boolean = when {
        !hasChanges -> true
        binding.intensitySliderContainer.isVisible -> {
            showIntensityState(isShow = false, needClearIntensity = true)

            false
        }
        else -> false.also { showCancelDialog() }
    }

    override fun process(main: Bitmap, support: Bitmap?): Bitmap =
            CGENativeLibrary.filterImage_MultipleEffects(
                    main,
                    (currentEffectConfig ?: FilterDataItem.ORIGINAL).effectConfig,
                    (currentEffectConfig ?: FilterDataItem.ORIGINAL).intensity
            )

    private fun previewFilter(item: FilterDataItem) {
        currentEffectConfig = item

        clearIntensity()
        when (item) {
            FilterDataItem.ORIGINAL -> clearChanges()
            else -> setChanges()
        }

        viewModel.onOpenItem(item.name)

        surfaceView.queueEvent {
            surfaceView.setFilterWithConfig(item.effectConfig)
        }

    }

    override fun dialogActionApply() {
        viewModel.applyItem()
        super.dialogActionApply()
    }

    private fun clearIntensity() {
        binding.viewSupportEdit.slider.value = 1F
    }

}
