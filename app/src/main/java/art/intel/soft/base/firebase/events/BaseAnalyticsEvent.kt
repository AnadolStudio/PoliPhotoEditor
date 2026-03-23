package art.intel.soft.base.firebase.events

import android.os.Bundle
import com.anadolstudio.mapper.util.ifNotEmptyAddPrefix

abstract class BaseAnalyticsEvent(
        val name: String,
        val params: Bundle? = null,
) {

    constructor(
            name: String,
            params: Bundle? = null,
            action: String? = null
    ) : this(
            name = name.concat(action),
            params = params
    )

    override fun toString(): String = name

    companion object {

        @JvmStatic
        protected fun String.concat(eventName: String?): String = this + eventName.orEmpty().lowercase().ifNotEmptyAddPrefix("_")

    }

    abstract class WithItem(name: String, itemName: String, action: String? = null) : BaseAnalyticsEvent(
            name = name,
            action = action,
            params = Bundle().apply { putString(EventParams.ITEM_NAME, itemName) }
    )

}

