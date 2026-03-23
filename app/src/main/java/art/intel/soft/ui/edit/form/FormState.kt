package art.intel.soft.ui.edit.form

sealed class FormState {

    object Loading : FormState()

    class Content(val formDataList: List<FormData>) : FormState()

}
