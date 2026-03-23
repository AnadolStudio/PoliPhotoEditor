package art.intel.soft.ui.edit.collage

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import art.intel.soft.R
import art.intel.soft.base.event.SingleEvent
import art.intel.soft.base.firebase.events.Screens
import art.intel.soft.databinding.FragmentEditCollageBinding
import art.intel.soft.extention.withArgs
import art.intel.soft.ui.edit.BaseEditFragment
import art.intel.soft.ui.edit.FragmentCreatedCallback
import art.intel.soft.ui.edit.collage.state.CollageState
import art.intel.soft.ui.edit.collage.state.InitCollageEvent
import art.intel.soft.ui.edit.collage.state.LoadedPhotoEvent
import art.intel.soft.ui.gallery.GalleryListActivity
import art.intel.soft.utils.bitmaputils.createScaledBitmap
import art.intel.soft.utils.bitmaputils.scaleRatioInscribed
import art.intel.soft.utils.throttleClick
import art.intel.soft.view.BaseToolbar
import art.intel.soft.view.collage.ItemCollageView

class CollageEditFragment : BaseEditFragment() {

    override val toolbarTitleId: Int = R.string.edit_collages_title

    companion object {
        private const val COLLAGE_PHOTOS = "collage_photos"

        fun newInstance(callback: FragmentCreatedCallback?, collagePhotoList: List<String>?): CollageEditFragment =
                CollageEditFragment().apply {
                    this.callback = callback
                    this.withArgs {
                        putStringArrayList(COLLAGE_PHOTOS, collagePhotoList?.let { ArrayList(it) })
                    }
                }
    }

    override fun provideScreenName(): Screens = Screens.COLLAGE

    private var currentState = State.LIST
    private val binding by lazy(LazyThreadSafetyMode.NONE) { FragmentEditCollageBinding.inflate(layoutInflater) }
    private lateinit var launcher: ActivityResultLauncher<String>
    private val viewModel: CollageViewModel by viewModels {
        val bitmap = editor().getCopyOriginalBitmap()
        val scale = scaleRatioInscribed(
                CollageViewModel.DEFAULT_IMAGE_SIDE,
                CollageViewModel.DEFAULT_IMAGE_SIDE,
                bitmap.width,
                bitmap.height
        )
        val pathList: List<String>? = arguments?.getStringArrayList(COLLAGE_PHOTOS)

        CollageViewModel.Factory(
                mainImageBitmap = createScaledBitmap(bitmap, scale),
                pathList = pathList,
                application = requireContext().applicationContext as Application
        )
    }

    private val itemCollageController by lazy(LazyThreadSafetyMode.NONE) {
        ItemCollageController(
                addPhotoAction = { maskId ->
                    viewModel.registerIdToPhoto(maskId)
                    launcher.launch(CHOOSE_PHOTO)
                },
                onSelectAction = { isSelect ->
                    setState(if (isSelect) State.EDIT else State.LIST)
                },
                removePhotoAction = viewModel::removePhoto
        )
    }

    private fun setState(state: State) {
        if (state == State.LIST && currentState == State.EDIT) {
            itemCollageController.previewCollage()
        }

        binding.editFunctionContainer.isVisible = state == State.EDIT
        binding.recyclerView.isVisible = state == State.LIST
        currentState = state
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel.state.observe(viewLifecycleOwner, this::render)
        viewModel.bottomVieState.observe(viewLifecycleOwner, this::renderBottomView)
        viewModel.event.observe(viewLifecycleOwner, this::handleEvent)

        launcher = createLauncher()

        with(binding) {
            reflectVerticalButton.throttleClick { editSelectPhoto(ItemCollageView.EditCommand.FlipVertical) }
            reflectHorizontalButton.throttleClick { editSelectPhoto(ItemCollageView.EditCommand.FlipHorizontal) }
            rotateLeftButton.throttleClick { editSelectPhoto(ItemCollageView.EditCommand.RotateLeft) }
            rotateRightButton.throttleClick { editSelectPhoto(ItemCollageView.EditCommand.RotateRight) }
            deleteButton.throttleClick { editSelectPhoto(ItemCollageView.EditCommand.Delete) }
        }

        return binding.root
    }

    private fun editSelectPhoto(command: ItemCollageView.EditCommand) = itemCollageController.editSelectPhoto(command)

    private fun renderBottomView(state: CollageState.BottomViewState) {
        binding.recyclerView.adapter = CollageDslAdapter(
                collageList = state.collagePathList,
                maskList = state.maskPathList,
                onSelect = { _, data -> loadCollage(data.collage, data.mask) }
        ).setup()
    }

    private fun render(state: CollageState.ImageViewState) = when (state) {
        CollageState.ImageViewState.Loading -> showLoadingDialog()
        is CollageState.ImageViewState.Content -> showContent(state)
    }

    private fun showContent(state: CollageState.ImageViewState.Content) {
        editor().setCollageImage(state.currentCollageBitmap)
        itemCollageController.createMasks(requireContext(), state.currentMaskBitmapList)

        itemCollageController.setPhotoList(state.photoBitmapList)
        editor().addItemsInCollage(itemCollageController.getItems())

        hideLoadingDialog()
    }

    private fun handleEvent(event: SingleEvent) = when (event) {
        is InitCollageEvent -> Unit.also {
            requireView().post { loadCollage(event.collagePath, event.maskPath) }
        }
        is LoadedPhotoEvent -> {
            itemCollageController.addPhoto(event.maskId, event.photo)
        }
        else -> Unit
    }

    private fun loadCollage(collagePath: String, maskPath: String) {
        viewModel.onOpenItem(collagePath)
        viewModel.loadCollage(collagePath = collagePath, maskPath = maskPath, editor().workSpace().y)
    }

    private fun createLauncher(): ActivityResultLauncher<String> =
            registerForActivityResult(GalleryListActivity.GalleryResultContract()) { path ->
                if (path == null) return@registerForActivityResult

                viewModel.loadPhoto(path)
            }

    override fun onSetupToolbar(toolbar: BaseToolbar) {
        super.onSetupToolbar(toolbar)
        toolbar.setRightButtonAction { applyChanges() }
    }

    private fun applyChanges() {
        if (!itemCollageController.collageIsReady()) {
            showToast(R.string.error_collage_is_not_ready, LENGTH_SHORT)

            return
        }

        setState(State.LIST)
        showApplyDialog()
    }

    override fun dialogActionApply() {
        viewModel.applyItem()
        super.dialogActionApply()
    }

    override fun onBackPressed(): Boolean = when (currentState) {
        State.EDIT -> false.also { setState(State.LIST) }
        State.LIST -> false.also { showCancelDialog() }
    }

    private enum class State { LIST, EDIT }
}
