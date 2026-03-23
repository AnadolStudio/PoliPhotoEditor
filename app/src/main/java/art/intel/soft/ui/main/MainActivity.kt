package art.intel.soft.ui.main

import android.os.Bundle
import art.intel.soft.BuildConfig.VERSION_CODE
import art.intel.soft.BuildConfig.VERSION_NAME
import art.intel.soft.base.activity.AdActivityWithBanner
import art.intel.soft.base.firebase.AnalyticEventsUtil
import art.intel.soft.base.firebase.events.implementation.OpenItemEvent
import art.intel.soft.databinding.ActivityMainBinding
import art.intel.soft.extention.debugLongClick
import art.intel.soft.ui.gallery.GalleryListActivity
import art.intel.soft.utils.AnimateUtil.scaleAnimationOnClick
import kotlin.LazyThreadSafetyMode.NONE

class MainActivity : AdActivityWithBanner() {

    companion object {
        private const val DEVELOP_INFO = "code $VERSION_CODE v$VERSION_NAME"
    }

    private val binding by lazy(NONE) { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        binding.backgroundBtn.scaleAnimationOnClick { openGalleryWithFlag(OpenEditType.BACKGROUND) }
        binding.bodyBtn.scaleAnimationOnClick { openGalleryWithFlag(OpenEditType.BODY) }
        binding.framesBtn.scaleAnimationOnClick { openGalleryWithFlag(OpenEditType.FRAMES) }
        binding.collagesBtn.scaleAnimationOnClick { openGalleryWithFlag(OpenEditType.COLLAGES) }
        binding.editBtn.scaleAnimationOnClick { openGalleryWithFlag(OpenEditType.EDIT_MENU) }
        binding.titleText.debugLongClick { showToast(DEVELOP_INFO) }
    }

    private fun openGalleryWithFlag(type: OpenEditType) {
        // TODO type.name on obfuscate
        AnalyticEventsUtil.logEvent(OpenItemEvent.StartMenu(type.name))
        GalleryListActivity.start(this, type)
    }

}
