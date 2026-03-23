package art.intel.soft.base.firebase

import art.intel.soft.base.firebase.events.implementation.ApplyItemEvent
import art.intel.soft.base.firebase.events.implementation.OpenItemEvent

interface AnalyticEventFabric<T : Any> {

    fun sentOpenEvent(t: T)

    fun sentAppleEvent(t: T)

    abstract class PathFabric : AnalyticEventFabric<String>

    abstract class NameFabric : AnalyticEventFabric<String>

    class Collage : PathFabric() {
        override fun sentAppleEvent(t: String) = AnalyticEventsUtil.logEventFromPath(t, ApplyItemEvent::Collage)
        override fun sentOpenEvent(t: String) = AnalyticEventsUtil.logEventFromPath(t, OpenItemEvent::Collage)
    }

    class Frame : PathFabric() {
        override fun sentAppleEvent(t: String) = AnalyticEventsUtil.logEventFromPath(t, ApplyItemEvent::Frames)
        override fun sentOpenEvent(t: String) = AnalyticEventsUtil.logEventFromPath(t, OpenItemEvent::Frames)
    }

    class Forms : PathFabric() {
        override fun sentAppleEvent(t: String) = AnalyticEventsUtil.logEventFromPath(t, ApplyItemEvent::Forms)
        override fun sentOpenEvent(t: String) = AnalyticEventsUtil.logEventFromPath(t, OpenItemEvent::Forms)
    }

    class Body : NameFabric() {
        override fun sentAppleEvent(t: String) = AnalyticEventsUtil.logEvent(ApplyItemEvent.Background(t))
        override fun sentOpenEvent(t: String) = AnalyticEventsUtil.logEvent(OpenItemEvent.Background(t))
    }

    class Crop : NameFabric() {
        override fun sentAppleEvent(t: String) = AnalyticEventsUtil.logEvent(ApplyItemEvent.Crop(t))
        override fun sentOpenEvent(t: String) = AnalyticEventsUtil.logEvent(OpenItemEvent.Crop(t))
    }

    class Effect : PathFabric() {
        override fun sentAppleEvent(t: String) = AnalyticEventsUtil.logEventFromPath(t, ApplyItemEvent::Effects)
        override fun sentOpenEvent(t: String) = AnalyticEventsUtil.logEventFromPath(t, OpenItemEvent::Effects)
    }

    class Filter : PathFabric() {
        override fun sentAppleEvent(t: String) = AnalyticEventsUtil.logEvent(ApplyItemEvent.Filters(t))
        override fun sentOpenEvent(t: String) = AnalyticEventsUtil.logEvent(OpenItemEvent.Filters(t))
    }


}
