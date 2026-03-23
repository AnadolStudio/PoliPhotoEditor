package art.intel.soft.ui.edit.filter.adapter

enum class FilterGroup(val color: String, val comment: String? = null) {
    ORIGINAL("#000000", "Original"),
    SD("#4B4B4B", "Simple Dark"),
    SP1("#9A9A9A", "Sepia1"),
    SP2("#C29800", "Sepia2"),
    SP3("#B9BD00", "Sepia3"),
    LG("#DDDDDD", "Light"),
    BL("#5C759A", "Blur"),
    SH("#608D00", "Sharp"),
    NT("#636363", "Negative"),
    MZ("#661F00", "Mozaic"),
    DT("#965F0D", "Desert"),
    WV("#5200FF", "Waves"),
    SL("#91DD6E", "Soft Light"),
    BW("#A4A4A4", "Black&White"),
    CL("#6EE163", "Color"),
    RB("#9966CB", "Rainbow"),
    GR("#29AB87", "Green"),
    OC("#86A1FF", "Ocean"),
    WM("#C948C4", "Warmth"),
    BB("#54A3FF", "Bright Blue"),
    PK("#FD8FFF", "Pink")
}
