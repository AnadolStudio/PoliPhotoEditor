package art.intel.soft.utils.bitmaputils

class Cell(val row: Int = 0, var column: Int = 0) {

    companion object {

        fun toCell(index: Int, width: Int): Cell = Cell(index / width, index % width)

        fun toIndex(cell: Cell, width: Int): Int = toIndex(cell.row, cell.column, width)

        fun toIndex(row: Int, column: Int, width: Int): Int = row * width + column
    }

    fun toIndex(width: Int): Int = toIndex(row, column, width)
}
