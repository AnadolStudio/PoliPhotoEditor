package art.intel.soft.base.firebase

import android.content.Context
import android.net.Uri
import art.intel.soft.base.firebase.events.BaseAnalyticsEvent

object AnalyticEventsUtil {

    fun init(context: Context) = Unit

    fun logEvent(analyticsEvent: BaseAnalyticsEvent) = Unit

    fun logEventFromPath(path: String?, analyticsEventCreator: (itemName: String) -> BaseAnalyticsEvent) {
        getNameFromPath(path)?.let(analyticsEventCreator::invoke)?.let(this::logEvent)
    }

    fun getNameFromPath(path: String?): String? = path?.let(Uri::parse)
            ?.lastPathSegment
            ?.let(this::removeFileExtension)

    private fun removeFileExtension(fileName: String): String? = fileName.split(".").firstOrNull()

}
