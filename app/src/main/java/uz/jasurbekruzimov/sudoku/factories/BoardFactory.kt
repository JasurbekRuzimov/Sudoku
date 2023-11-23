package uz.jasurbekruzimov.sudoku.factories

import android.util.Log
import uz.jasurbekruzimov.sudoku.game.Board
import uz.jasurbekruzimov.sudoku.game.Cell
import kotlin.math.sqrt

object BoardFactory {
    /**
     * Función que genera un tablero de sudoku aleatorio
     * @param size Tamaño del tablero
     * @return Tablero de sudoku aleatorio
     */
    fun createRandomSudoku(size: Int): Board {
        val board = generateEmptyBoard(size)
        val milliseconds = System.currentTimeMillis()
        solveSudoku(board)
        Log.i("Sudoku", "Sudoku solved in ${System.currentTimeMillis() - milliseconds} ms")
        return board
    }

    /**
     * Función que genera un tablero vacío
     * @param size Tamaño del tablero
     * @return Tablero vacío
     */
    private fun generateEmptyBoard(size: Int): Board {
        val cells = mutableListOf<Cell>()
        for (row in 0 until size) {
            for (col in 0 until size) {
                cells.add(Cell(row, col, 0))
            }
        }
        return Board(size, cells)
    }

    /**
     * Función que rellena un tablero de sudoku de forma aleatoria y lo resuelve
     * @param board Tablero a resolver
     * @return True si se ha resuelto el tablero, false en caso contrario
     */
    private fun solveSudoku(board: Board): Boolean {
        //if (board.validate()) return true

        for (row in 0 until board.size) {
            for (col in 0 until board.size) {
                val cell = board.getCell(row, col)
                if (cell.value != 0) continue
                val values = (1..board.size).shuffled()
                for (value in values) {
                    if (isValidMove(board, cell, value)) {
                        cell.value = value
                        if (solveSudoku(board)) {
                            return true
                        }
                        cell.value = 0
                    }
                }
                return false
            }
        }
        return true
    }

    /**
     * Función que comprueba si un movimiento es válido
     * @param board Tablero
     * @param cell Celda
     * @param value Valor
     * @return True si el movimiento es válido, false en caso contrario
     */
    fun isValidMove(board: Board, cell: Cell, value: Int): Boolean {
        for (i in 0 until board.size) {
            if (board.getCell(cell.row, i).value == value ||
                board.getCell(i, cell.col).value == value ||
                board.getCell(
                    cell.row - cell.row % board.sqrtSize + i / board.sqrtSize,
                    cell.col - cell.col % board.sqrtSize + i % board.sqrtSize).value == value) {
                return false
            }
        }
        return true
    }

    /**
     * Función que elimina celdas aleatorias del tablero
     * @param board Tablero
     * @param numMissingCells Número de celdas a eliminar
     * @return Tablero con celdas eliminadas
     */
    fun subtractRandomCells(board: Board, numMissingCells: Int): Board {
        val size = board.size
        require(numMissingCells in 0 .. ((size * size) * 0.5f).toInt())
        if (numMissingCells == 0) return board

        val cellsToKeep = (0 until size * size).shuffled().take(size * size - numMissingCells)
        val modifiedCells = board.cells.mapIndexed { index, cell ->
            if (index in cellsToKeep) {
                cell.copy(isStartingCell = true)
            } else {
                cell.copy(value = 0)
            }
        }.toList()

        return Board(size, modifiedCells)
    }
}