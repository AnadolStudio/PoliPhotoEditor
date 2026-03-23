package art.intel.soft.ui.gallery

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build.*
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import art.intel.soft.R
import art.intel.soft.base.activity.AdActivityWithBanner
import art.intel.soft.databinding.ActivityGalleryBinding
import art.intel.soft.extention.onTrue
import art.intel.soft.ui.edit.Action
import art.intel.soft.ui.edit.BaseEditFragment.Companion.CHOOSE_PHOTO
import art.intel.soft.ui.edit.BaseEditFragment.Companion.CHOOSE_PHOTO_KEY
import art.intel.soft.ui.edit.BaseEditFragment.Companion.CHOOSE_PHOTO_WITHOUT_AD
import art.intel.soft.ui.edit.BaseEditFragment.Companion.REQUEST_CHOOSE_PHOTO
import art.intel.soft.ui.edit.BaseEditFragment.Companion.REQUEST_CHOOSE_PHOTO_WITHOUT_AD
import art.intel.soft.ui.edit.EditActivity
import art.intel.soft.ui.main.OpenEditType
import art.intel.soft.utils.PermissionHelper.READ_GALLERY_PERMISSION
import art.intel.soft.utils.PermissionHelper.REQUEST_STORAGE_PERMISSION
import art.intel.soft.utils.PermissionHelper.WRITE_GALLERY_PERMISSION
import art.intel.soft.utils.PermissionHelper.hasPermission
import art.intel.soft.utils.PermissionHelper.requestPermission
import art.intel.soft.utils.PermissionHelper.showSettingsSnackbar
import art.intel.soft.view.GalleryToolBar

class GalleryListActivity : AdActivityWithBanner(), Action<String>, ILoadMore {

    companion object {
        private const val MARGIN_ITEM = 5
        private const val MAX_SELECTED_COUNT = 3

        const val TAG = "GalleryListActivity"

        fun start(context: Context, type: OpenEditType) {
            val starter = Intent(context, GalleryListActivity::class.java)
            starter.putExtra(OpenEditType::class.java.name, type)
            context.startActivity(starter)
        }
    }

    private val binding by lazy(LazyThreadSafetyMode.NONE) { ActivityGalleryBinding.inflate(layoutInflater) }
    private lateinit var galleryListAdapter: GalleryListAdapter
    private val viewModel: GalleryListViewModel by viewModels { GalleryListViewModel.Factory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        viewModel.screenState.observe(this, this::render)

        initToolbar()
        initView()
        loadData()
    }

    private fun initToolbar() = with(binding.toolbar) {
        onBackClick { onBackPressed() }
        onCancelClick { galleryListAdapter.clearSelectedPhotos() }
        onApplyClick {
            val photos = galleryListAdapter.getSelectedPhotos()
            when (photos.isEmpty()) {
                true -> showToast(R.string.edit_error_nothing_selected)
                false -> action(galleryListAdapter.getSelectedPhotos())
            }
        }
        onFolderClick(this@GalleryListActivity::onFolderClick)
    }

    private fun onFolderClick(text: String) {
        val currentFolder = when (text) {
            getString(R.string.gallery_spinner_title) -> null
            else -> text
        }

        binding.toolbar.setTitle(currentFolder)

        if (viewModel.folderChanged(currentFolder)) {
            loadImages()
        }
    }

    private fun render(state: GalleryScreenState) = when (state) {
        is GalleryScreenState.Content -> showContent(state).also { hideLoadingDialog() }
        is GalleryScreenState.Error -> initFolder(emptyList()).also { hideLoadingDialog() }
        is GalleryScreenState.Empty -> showEmptyText(true).also { hideLoadingDialog() }
        is GalleryScreenState.Loading -> showLoadingDialog()
    }

    private fun showContent(content: GalleryScreenState.Content) {
        content.folders?.toList()?.let(this::initFolder)
        showEmptyText(false)

        when (content.isLoadMore) {
            true -> galleryListAdapter.addData(content.images)
            false -> galleryListAdapter.setData(content.images)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        withPermission { viewModel.updateImages(this, binding.mainRv.adapter?.itemCount) }
    }

    private fun initView() {
        val editType = intent.getSerializableExtra(OpenEditType::class.java.name) as? OpenEditType
        val isMultiChoice = editType == OpenEditType.COLLAGES

        if (isMultiChoice) {
            binding.toolbar.setState(GalleryToolBar.State.MULTI_CHOICE)
        }

        galleryListAdapter = GalleryListAdapter(
                photoList = mutableListOf(),
                action = this,
                isSelectedMode = isMultiChoice,
                onSelectedListener = { count ->
                    val text = when (count) {
                        0 -> null
                        else -> getString(R.string.gallery_title_count_of_selected_items, count, MAX_SELECTED_COUNT)
                    }
                    binding.toolbar.showSelectableMode(count > 0)
                    binding.toolbar.setTitle(text)
                },
                loadMoreListener = this
        )

        binding.mainRv.apply {
            layoutManager = GridLayoutManager(this@GalleryListActivity, 3)
            setItemViewCacheSize(50)
            addItemDecoration(MarginItemDecoration(MARGIN_ITEM))
            adapter = galleryListAdapter
        }
    }

    override fun loadMore() = loadImages(true)

    @SuppressLint("MissingPermission")
    private fun loadImages(loadMore: Boolean = false) {
        withPermission { viewModel.loadImages(this@GalleryListActivity, loadMore = loadMore) }
    }

    @SuppressLint("MissingPermission")
    private fun loadData() {
        val success = withPermission { viewModel.loadData(this) }

        if (!success) {
            showEmptyText(true)
            requestPermission(this, READ_GALLERY_PERMISSION, REQUEST_STORAGE_PERMISSION)
        }
    }

    private fun initFolder(folders: List<String>) {
        val data = mutableListOf<String>().apply {
            add(getString(R.string.gallery_spinner_title))
            addAll(folders)
        }

        binding.toolbar.setFolders(data)
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_STORAGE_PERMISSION ->
                if (grantResults.isNotEmpty() && grantResults.any { it == PERMISSION_GRANTED }) { // permission granted
                    loadData()
                } else { // permission denied
                    val shouldShow =
                            ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_GALLERY_PERMISSION[0])

                    if (!shouldShow) showSettingsSnackbar(this, binding.root)
                    else finish() // Закрывает activity
                }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun showEmptyText(isShow: Boolean) {
        binding.emptyText.isVisible = isShow
        binding.mainRv.isVisible = !isShow
    }

    override fun onSupportNavigateUp(): Boolean {
        when (intent.getIntExtra(CHOOSE_PHOTO_KEY, 0)) {
            REQUEST_CHOOSE_PHOTO, REQUEST_CHOOSE_PHOTO_WITHOUT_AD -> {
                onBackPressed()
            }
            else -> showInterstitial(
                    clickAction = {},
                    showedAction = {},
                    action = { super.onSupportNavigateUp() }
            )
        }

        return true
    }

    override fun action(path: String) {
        val editType = intent.getSerializableExtra(OpenEditType::class.java.name) as? OpenEditType
        val request = intent.getIntExtra(CHOOSE_PHOTO_KEY, 0)

        when {
            editType != null -> showInterstitial(
                    clickAction = {},
                    showedAction = {},
                    action = { EditActivity.start(this, editType, path) }
            )
            request == REQUEST_CHOOSE_PHOTO -> showInterstitial(
                    clickAction = {},
                    showedAction = {},
                    action = { navigateBackWithResult(path) }
            )
            request == REQUEST_CHOOSE_PHOTO_WITHOUT_AD -> {
                navigateBackWithResult(path)
            }
            else -> Unit
        }
    }

    private fun navigateBackWithResult(path: String) {
        setResult(RESULT_OK, Intent().putExtra(CHOOSE_PHOTO_KEY, path))
        finish()
    }

    fun action(pathList: List<String>) {
        val editType = intent.getSerializableExtra(OpenEditType::class.java.name) as? OpenEditType ?: return
        val path = pathList.first()

        showInterstitial(
                clickAction = {},
                showedAction = {},
                action = { EditActivity.start(this, editType, path, pathList) }
        )
    }

    @SuppressLint("InlinedApi")
    private fun withPermission(action: () -> Unit): Boolean = when (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
        true -> hasPermission(this, Manifest.permission.READ_MEDIA_IMAGES).onTrue { action.invoke() }
        else -> hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE).onTrue { action.invoke() }
    }

    class GalleryResultContract : ActivityResultContract<String, String?>() {

        override fun createIntent(context: Context, input: String): Intent =
                Intent(context, GalleryListActivity::class.java).apply {
                    when (input) {
                        CHOOSE_PHOTO -> putExtra(CHOOSE_PHOTO_KEY, REQUEST_CHOOSE_PHOTO)
                        CHOOSE_PHOTO_WITHOUT_AD -> putExtra(CHOOSE_PHOTO_KEY, REQUEST_CHOOSE_PHOTO_WITHOUT_AD)
                        else -> Unit
                    }
                }

        override fun parseResult(resultCode: Int, data: Intent?): String? = when {
            resultCode != RESULT_OK || data == null -> null
            else -> data.getStringExtra(CHOOSE_PHOTO_KEY)
        }
    }
}
