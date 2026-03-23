package art.intel.soft.ui.edit.body

sealed class BodyState {

    object Loading : BodyState()

    class Content(val photoIsBright: Boolean) : BodyState()

}
