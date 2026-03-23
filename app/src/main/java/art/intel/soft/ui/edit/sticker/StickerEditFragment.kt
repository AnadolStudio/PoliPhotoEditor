package art.intel.soft.ui.edit.sticker

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import art.intel.soft.R
import art.intel.soft.base.firebase.AnalyticEventsUtil
import art.intel.soft.base.firebase.events.Screens
import art.intel.soft.base.firebase.events.implementation.OpenItemEvent
import art.intel.soft.databinding.FragmentEditStickerBinding
import art.intel.soft.ui.edit.BaseEditFragment
import art.intel.soft.ui.gallery.GalleryListActivity
import art.intel.soft.utils.ImageLoader
import art.intel.soft.utils.bitmaputils.getCopyBitmap
import art.intel.soft.utils.throttleClick
import art.intel.soft.view.BaseToolbar
import ja.burhanrashid52.photoeditor.graphic.GraphicBorderActions
import ja.burhanrashid52.photoeditor.graphic.GraphicBorderActions.GraphicBorderData
import ja.burhanrashid52.photoeditor.graphic.Image

class StickerEditFragment : BaseEditFragment() {

    companion object {
        fun newInstance(): StickerEditFragment = StickerEditFragment()
        const val DEFAULT_IMAGE_SIDE = 400
    }

    override fun provideScreenName(): Screens = Screens.STICKERS

    override val toolbarTitleId: Int = R.string.edit_sticker_title
    private val viewModel: StickerViewModel by viewModels()
    private val binding by lazy(LazyThreadSafetyMode.NONE) { FragmentEditStickerBinding.inflate(layoutInflater) }
    private lateinit var launcher: ActivityResultLauncher<String>

    private val borderData: GraphicBorderActions by lazy {
        GraphicBorderActions(
                leftTopButton = GraphicBorderData(R.drawable.ic_border_delete, isMovable = false) { imageWrapper, _, _ ->
                    imageWrapper.remove()
                    if (editor().getPhotoEditor().childIsEmpty()) {
                        clearChanges()
                    }
                    val name = (imageWrapper as? Image)?.name ?: return@GraphicBorderData
                    viewModel.removeSticker(name)
                },
                rightTopButton = GraphicBorderData(R.drawable.ic_border_flip, isMovable = false) { imageWrapper, _, _ ->
                    imageWrapper.flip()
                },
                leftBottomButton = GraphicBorderData(R.drawable.ic_border_copy, isMovable = false) { imageWrapper, _, _ ->
                    val image = (imageWrapper as? Image) ?: return@GraphicBorderData
                    val bitmap = image.image ?: return@GraphicBorderData
                    val name = image.name ?: return@GraphicBorderData
                    addSticker(sticker = bitmap, name = name)
                },
                rightBottomButton = GraphicBorderData(
                        R.drawable.ic_border_change_size,
                        isMovable = true
                ) { imageWrapper, x, y ->
                    imageWrapper.scale(x, y)
                }
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel.state.observe(viewLifecycleOwner, this::render)
        launcher = registerForActivityResult(GalleryListActivity.GalleryResultContract(), this::setOwnSticker)

        binding.selectImageContainer.throttleClick {
            AnalyticEventsUtil.logEvent(OpenItemEvent.Stickers.Category(OpenItemEvent.SubItems.OWN_PHOTO.value))
            launcher.launch(CHOOSE_PHOTO)
        }

        return binding.root
    }

    private fun setOwnSticker(path: String?) {
        if (path == null) return
        AnalyticEventsUtil.logEvent(OpenItemEvent.Stickers(OpenItemEvent.SubItems.OWN_PHOTO.value))

        ImageLoader.loadImageWithoutCache(
                requireContext(),
                path,
                DEFAULT_IMAGE_SIDE,
                DEFAULT_IMAGE_SIDE,
        ) { bitmap ->
            addSticker(sticker = bitmap, name = OpenItemEvent.SubItems.OWN_PHOTO.value)
        }
    }

    private fun render(state: StickerState) = when (state) {
        is StickerState.Content -> showContent(state)
        StickerState.Loading -> showLoadingDialog()
    }

    private fun showContent(content: StickerState.Content) {
        hideLoadingDialog()
        val groupList = content.data.map { it.groupDrawableRes }

        binding.stickersGroupGroup.isVisible = true
        binding.stickersGroupRecyclerView.adapter = StickerGroupDslAdapter(groupList) { index, _ ->
            val group = content.data[index]
            AnalyticEventsUtil.logEvent(OpenItemEvent.Stickers.Category(group.groupName))

            binding.stickersRecyclerView.adapter = StickerAdapter(group.list, this::action)
        }.setup()
        binding.stickersRecyclerView.adapter = StickerAdapter(content.data.first().list, this::action)
    }

    fun action(path: String) {
        AnalyticEventsUtil.logEventFromPath(path, OpenItemEvent::Stickers)
        val name = AnalyticEventsUtil.getNameFromPath(path) ?: return

        ImageLoader.loadImageWithoutCache(requireContext(), path) { sticker ->
            addSticker(sticker = sticker, name = name, withScale = true)
        }
    }

    private fun addSticker(sticker: Bitmap?, name: String, withScale: Boolean = false) {
        sticker ?: return

        viewModel.addSticker(name)

        val correctBitmap = if (withScale) {
            getCopyBitmap(sticker, sticker.width / 2, sticker.height / 2).also { sticker.recycle() }
        } else {
            sticker
        }

        editor().getPhotoEditor().addImage(correctBitmap, name, borderData)
        setChanges()
    }

    override fun onSetupToolbar(toolbar: BaseToolbar) {
        super.onSetupToolbar(toolbar)
        toolbar.setRightButtonAction { applyChanges() }
    }

    private fun applyChanges() {
        if (!hasChanges) {
            nothingIsSelectedToast()

            return
        }

        showApplyDialog()
    }

    override fun dialogActionApply() {
        super.dialogActionApply()
        viewModel.logApplyStickerEvent()
    }
    override fun onBackPressed(): Boolean = when {
        hasChanges -> false.also { showCancelDialog() }
        else -> true
    }

}
