package uz.jasurbekruzimov.sudoku.game

import kotlin.math.sqrt

data class Board(val size: Int = 9, var cells: List<Cell>) {
    /**
     * Función que devuelve una celda del tablero
     * @param row Fila de la celda
     * @param col Columna de la celda
     * @return Celda del tablero
     */
    fun getCell(row: Int, col: Int) = cells[row * size + col]

    /**
     * Variable que contiene el tamaño de la raíz cuadrada del tablero
     * @return Tamaño de la raíz cuadrada del tablero
     */
    val sqrtSize get() = sqrt(size.toDouble()).toInt()

    /**
     * Función que valida el tablero
     * @return True si el tablero es válido, false en caso contrario
     */
    fun validate(): Boolean{
        cells.forEach {main ->
            if (main.isStartingCell) return@forEach
            cells.forEach{
                val value = it.value
                if (value == 0) return false
                if (value != main.value) return@forEach
                when{
                    it.isStartingCell -> return@forEach
                    it.row == main.row && it.col == main.col -> return@forEach
                    it.row == main.row || it.col == main.col -> return false
                    it.row / sqrtSize == main.row / sqrtSize && it.col / sqrtSize == main.col / sqrtSize -> return false
                    else -> return@forEach
                }
            }
        }
        return true
    }
}