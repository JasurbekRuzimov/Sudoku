package uz.jasurbekruzimov.sudoku.game

    data class Cell(val row: Int, val col: Int, var value: Int, var isStartingCell: Boolean = false, var notes: MutableSet<Int> = mutableSetOf())