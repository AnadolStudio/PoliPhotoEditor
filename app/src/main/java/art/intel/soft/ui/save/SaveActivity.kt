package art.intel.soft.ui.save

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.content.FileProvider
import art.intel.soft.R
import art.intel.soft.base.activity.AdActivityWithBanner
import art.intel.soft.base.firebase.AnalyticEventsUtil
import art.intel.soft.base.firebase.events.implementation.ClickItemEvent
import art.intel.soft.databinding.ActivitySaveBinding
import art.intel.soft.utils.ImageLoader
import java.io.File

class SaveActivity : AdActivityWithBanner() {

    companion object {
        private const val PATH: String = "path"
        private const val INSTAGRAM_PACKAGE: String = "com.instagram.android"
        private const val FACEBOOK_PACKAGE: String = "com.facebook.katana"
        private const val WHATS_APP_PACKAGE: String = "com.whatsapp"
        private const val TWITTER_PACKAGE: String = "com.twitter.android"

        @JvmStatic
        fun start(context: Context, path: String) {
            val starter = Intent(context, SaveActivity::class.java)
            starter.putExtra(PATH, path)
            context.startActivity(starter)
        }
    }

    private val binding by lazy(LazyThreadSafetyMode.NONE) { ActivitySaveBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val path = intent.getStringExtra(PATH)
        ImageLoader.loadImage(binding.savedImage, path, ImageLoader.ScaleType.FIT_CENTER)
        initView(path)
        setContentView(binding.root)
    }

    private fun initView(path: String?) {
        binding.navigationTb.setLeftButtonAction {
            AnalyticEventsUtil.logEvent(ClickItemEvent.SaveScreen(ClickItemEvent.SaveScreen.ItemName.BACK))
            onBackPressed()
        }
        binding.navigationTb.setRightButtonAction {
            AnalyticEventsUtil.logEvent(ClickItemEvent.SaveScreen(ClickItemEvent.SaveScreen.ItemName.START_AGAIN))
            onSupportNavigateUp()
        }

        if (path == null) return
        binding.share.setOnClickListener { createShareIntent(path, null) }
        binding.instagram.setOnClickListener { createShareIntent(path, INSTAGRAM_PACKAGE) }
        binding.facebook.setOnClickListener { createShareIntent(path, FACEBOOK_PACKAGE) }
        binding.whatsApp.setOnClickListener { createShareIntent(path, WHATS_APP_PACKAGE) }
        binding.twitterApp.setOnClickListener { createShareIntent(path, TWITTER_PACKAGE) }
    }

    private fun createShareIntent(path: String, namePackage: String?) {
        AnalyticEventsUtil.logEvent(ClickItemEvent.SaveScreen(ClickItemEvent.SaveScreen.ItemName.SHARE))

        val type = "image/*"
        val photoURI = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", File(path))
        val share = Intent(Intent.ACTION_SEND)
        share.type = type
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        share.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        share.putExtra(Intent.EXTRA_STREAM, photoURI)

        if (namePackage == null) {
            startActivity(Intent.createChooser(share, getString(R.string.save_func_share)))
        } else {
            share.setPackage(namePackage)
            try {
                startActivity(share)
            } catch (e: ActivityNotFoundException) {
                openGooglePlay(namePackage)
            }
        }
    }

    private fun openGooglePlay(namePackage: String) {
        try {
            startActivity(
                    Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=$namePackage")
                    )
            )
        } catch (ex: ActivityNotFoundException) {
            startActivity(
                    Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$namePackage")
                    )
            )
        }
    }

}
