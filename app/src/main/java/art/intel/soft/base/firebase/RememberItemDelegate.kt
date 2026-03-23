package art.intel.soft.base.firebase

interface RememberItemDelegate<T : Any> {

    fun onApplyItem(t: T)

    fun applyItem()

    fun onOpenItem(t: T)

    open class Delegate<T : Any>(private val fabric: AnalyticEventFabric<T>) : RememberItemDelegate<T> {

        private var currentItem: T? = null

        override fun applyItem() {
            currentItem?.let { onApplyItem(it) }
        }

        override fun onApplyItem(t: T) {
            if (t != currentItem) return
            fabric.sentAppleEvent(t)
        }

        override fun onOpenItem(t: T) {
            currentItem = t
            fabric.sentOpenEvent(t)
        }
    }
}
