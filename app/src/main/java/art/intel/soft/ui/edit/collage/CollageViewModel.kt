package art.intel.soft.ui.edit.collage

import android.app.Application
import android.content.res.Resources
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import art.intel.soft.base.firebase.AnalyticEventFabric
import art.intel.soft.base.firebase.RememberItemDelegate
import art.intel.soft.base.view_model.BaseAndroidViewModel
import art.intel.soft.extention.baseSubscribe
import art.intel.soft.extention.onTrueOrNull
import art.intel.soft.extention.singleBy
import art.intel.soft.extention.singleFrom
import art.intel.soft.model.AssetsDirections
import art.intel.soft.model.CollageService
import art.intel.soft.model.getPathList
import art.intel.soft.ui.edit.collage.state.CollageState
import art.intel.soft.ui.edit.collage.state.InitCollageEvent
import art.intel.soft.ui.edit.collage.state.LoadedPhotoEvent
import art.intel.soft.utils.ImageLoader
import art.intel.soft.utils.bitmaputils.getScaleRatio
import art.intel.soft.utils.bitmaputils.scaleRatioInscribed
import art.intel.soft.utils.onNext
import art.intel.soft.utils.toImmutable
import io.reactivex.Single

class CollageViewModel(
        mainImageBitmap: Bitmap,
        application: Application,
        pathList: List<String>
) : BaseAndroidViewModel<CollageState.ImageViewState>(application),
        RememberItemDelegate<String> by RememberItemDelegate.Delegate(AnalyticEventFabric.Collage()) {

    companion object {
        const val DEFAULT_IMAGE_SIDE = 800
        private const val NOT_FOUND = -1
    }

    private val _bottomVieState = MutableLiveData<CollageState.BottomViewState>()
    val bottomVieState = _bottomVieState.toImmutable()

    private val photoBitmapMap = mutableMapOf<Int, Bitmap?>(0 to mainImageBitmap)

    init {
        pathList.forEachIndexed { index, path ->
            if (photoBitmapMap.containsKey(index)) return@forEachIndexed
            registerIdToPhoto(index)
            loadPhoto(
                    path,
                    needEventPredicate = { _state.value as? CollageState.ImageViewState.Content == null }
            )
        }
        loadCollagePath(application)
    }

    fun loadCollage(collagePath: String, maskPath: String, defaultHeight: Int) {
        _state.onNext(CollageState.ImageViewState.Loading)

        val singleCollageBitmap = singleBy<Bitmap> {
            ImageLoader.loadImageWithoutCache(getApplication(), collagePath, this::onSuccess)
        }

        val singleMaskBitmap = singleBy<Bitmap> {
            ImageLoader.loadImageWithoutCache(getApplication(), maskPath, this::onSuccess)
        }.flatMap { bitmap ->
            val displayMetricsWidth = Resources.getSystem().displayMetrics.widthPixels
            // Коэф. который показывает, какой размер будет у вью, где размещаются маски
            val screenScaleRatio = scaleRatioInscribed(displayMetricsWidth, defaultHeight, bitmap.width, bitmap.height)
            val defaultWidth = bitmap.width * screenScaleRatio
            val scaleRatio = getScaleRatio(bitmap.width.toFloat(), defaultWidth)

            CollageService().createCollageMask(
                    maskSource = bitmap,
                    scaleRatio = scaleRatio,
            )
        }

        Single.zip(singleCollageBitmap, singleMaskBitmap) { collageBitmap, dataBitmapWrapperList ->
            CollageState.ImageViewState.Content(
                    currentCollageBitmap = collageBitmap,
                    currentMaskBitmapList = dataBitmapWrapperList,
                    photoBitmapList = photoBitmapMap.values.toList().filterNotNull()
            )
        }
                .baseSubscribe(_state::onNext)
                .disposeOnViewModelDestroy()
    }

    private fun loadCollagePath(application: Application) {
        Single.zip(
                singleFrom { getPathList(application, AssetsDirections.COLLAGE_DIR) },
                singleFrom { getPathList(application, AssetsDirections.COLLAGE_MASK_DIR) }
        ) { collageList, maskList ->
            CollageState.BottomViewState(
                    collagePathList = collageList,
                    maskPathList = maskList
            )
        }
                .baseSubscribe({ bottomVieState ->
                    _bottomVieState.onNext(bottomVieState)

                    val imageLoaded = (_state.value as? CollageState.ImageViewState.Content)?.currentCollageBitmap != null

                    if (!imageLoaded) {
                        _singleEvent.onNext(
                                InitCollageEvent(bottomVieState.collagePathList.first(), bottomVieState.maskPathList.first())
                        )
                    }
                })
                .disposeOnViewModelDestroy()
    }

    fun registerIdToPhoto(maskId: Int) {
        photoBitmapMap[maskId] = null
    }

    fun removePhoto(maskId: Int) {
        photoBitmapMap.remove(maskId)
    }

    fun loadPhoto(path: String) = loadPhoto(path = path, needEventPredicate = null)

    private fun loadPhoto(path: String, needEventPredicate: (() -> Boolean)?) {
        ImageLoader.loadImageWithoutCache(
                getApplication(),
                path,
                DEFAULT_IMAGE_SIDE,
                DEFAULT_IMAGE_SIDE
        ) { bitmap: Bitmap ->
            var idWithNull = NOT_FOUND

            for ((maskId, previousBitmap) in photoBitmapMap) {
                if (previousBitmap == null) {
                    idWithNull = maskId
                    break
                }
            }

            if (idWithNull != NOT_FOUND) {
                photoBitmapMap[idWithNull] = bitmap

                needEventPredicate?.invoke().onTrueOrNull {
                    _singleEvent.onNext(LoadedPhotoEvent(idWithNull, bitmap))
                }
            }
        }
    }

    class Factory(
            private val mainImageBitmap: Bitmap,
            private val application: Application,
            private val pathList: List<String>?
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
                CollageViewModel(
                        mainImageBitmap,
                        application,
                        pathList.orEmpty()
                ) as T
    }

}
