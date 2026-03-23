package art.intel.soft.base

interface OnBackPressed {

    // True - возвращает контроль родителю и закрывается
    // False - оставляет контроль у себя и остается на экране
    fun onBackPressed(): Boolean
}
