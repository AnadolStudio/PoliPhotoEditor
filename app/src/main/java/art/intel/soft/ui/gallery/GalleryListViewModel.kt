package art.intel.soft.ui.gallery

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.annotation.SuppressLint
import android.net.Uri
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import art.intel.soft.base.view_model.BaseViewModel
import art.intel.soft.extention.baseSubscribe
import art.intel.soft.model.GalleryService
import art.intel.soft.utils.onNext
import art.intel.soft.utils.toImmutable
import io.reactivex.Single
import java.io.File
import kotlin.math.max

class GalleryListViewModel(private val galleryRepository: GalleryService) : BaseViewModel() {

    private companion object {
        const val ONE_PORTION = 99
    }

    private var currentFolder: String? = null
    private val _screenState = MutableLiveData<GalleryScreenState>(GalleryScreenState.Empty)
    val screenState = _screenState.toImmutable()

    private var lastImageItem: Long? = null

    @SuppressLint("InlinedApi")
    @RequiresPermission(anyOf = [READ_EXTERNAL_STORAGE, READ_MEDIA_IMAGES])
    fun loadData(activity: AppCompatActivity) {
        _screenState.onNext(GalleryScreenState.Loading)

        Single.zip(
                galleryRepository.loadImages(activity = activity, size = ONE_PORTION, folder = null),
                galleryRepository.loadFolders(activity)
        ) { images, folders -> GalleryScreenState.Content(images = images, folders = folders) }
                .baseSubscribe(
                        { data -> setScreenState(data) },
                        { error -> _screenState.onNext(GalleryScreenState.Error(error)) }
                )
                .disposeOnViewModelDestroy()
    }

    private fun setScreenState(data: GalleryScreenState.Content) {
        when (data.images.isEmpty()) {
            true -> if (!data.isLoadMore) _screenState.onNext(GalleryScreenState.Empty)
            false -> updateLastItem(data).also { _screenState.onNext(data) }
        }
    }

    private fun updateLastItem(data: GalleryScreenState.Content) {
        lastImageItem = Uri.parse(data.images.last()).path?.let { File(it).name }
                ?.toLongOrNull()
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(anyOf = [READ_EXTERNAL_STORAGE, READ_MEDIA_IMAGES])
    fun loadImages(activity: AppCompatActivity, size: Int = ONE_PORTION, loadMore: Boolean = false) {
        val lastItem = if (loadMore) lastImageItem else null

        galleryRepository.loadImages(activity = activity, size = size, folder = currentFolder, lastItemIndex = lastItem)
                .baseSubscribe(
                        { images -> setScreenState(GalleryScreenState.Content(images = images, folders = null, loadMore)) },
                        { error -> _screenState.onNext(GalleryScreenState.Error(error)) },
                )
                .disposeOnViewModelDestroy()
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(anyOf = [READ_EXTERNAL_STORAGE, READ_MEDIA_IMAGES])
    fun updateImages(activity: AppCompatActivity, size: Int?) {
        when (_screenState.value) {
            is GalleryScreenState.Error,
            GalleryScreenState.Loading,
            null -> return
            else -> Unit
        }

        loadImages(activity = activity, size = max(size ?: 0, ONE_PORTION), loadMore = false)
    }

    fun folderChanged(folder: String?): Boolean = (currentFolder != folder).also { currentFolder = folder }

    class Factory : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T = GalleryListViewModel(GalleryService()) as T
    }
}
