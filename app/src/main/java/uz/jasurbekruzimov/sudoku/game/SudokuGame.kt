package uz.jasurbekruzimov.sudoku.game

import androidx.lifecycle.MutableLiveData
import uz.jasurbekruzimov.sudoku.factories.BoardFactory
import uz.jasurbekruzimov.sudoku.factories.BoardFactory.isValidMove

class SudokuGame {
    /**
     * LiveData que contiene la celda seleccionada
     */
    var selectedCellLiveData = MutableLiveData<Pair<Int, Int>>()
    /**
     * LiveData que contiene las celdas del tablero
     */
    var cellsLiveData = MutableLiveData<List<Cell>>()
    /**
     * LiveData que contiene el estado de tomar notas
     */
    val isTakingNotesLiveData = MutableLiveData<Boolean>()
    /**
     * LiveData que contiene las notas que se deben resaltar en el teclado
     */
    val highlightedKeysLiveData = MutableLiveData<Set<Int>>()

    private var selectedRow = -1
    private var selectedCol = -1
    private var isTakingNotes = false

    private var size = 9

    private lateinit var board: Board

    init {
        startValues(40)
    }

    /**
     * Función que se encarga de manejar la entrada de un número en el tablero,
     * ya sea para introducirlo en la celda seleccionada o para añadirlo a las notas
     * @param number Número a introducir
     */
    fun handleInput(number: Int) {
        if (selectedRow == -1 || selectedCol == -1) return
        if (board.getCell(selectedRow, selectedCol).isStartingCell) return
        val cell = board.getCell(selectedRow, selectedCol)
        if (isTakingNotes){
            if (cell.notes.contains(number))
                cell.notes.remove(number)
            else
                cell.notes.add(number)
            highlightedKeysLiveData.postValue(cell.notes)
        }else{
            cell.value = number
        }
        cellsLiveData.postValue(board.cells)
    }

    /**
     * Función que actualiza la celda seleccionada
     * @param row Fila de la celda
     * @param col Columna de la celda
     */
    fun updateSelectedCell(row: Int, col: Int) {
        val cell = board.getCell(row, col)
        if (!cell.isStartingCell) {
            selectedRow = row
            selectedCol = col
            selectedCellLiveData.postValue(Pair(row, col))
            if (isTakingNotes)
                highlightedKeysLiveData.postValue(cell.notes)
        }
    }

    /**
     * Función que cambia el estado de tomar notas
     */
    fun changeNoteTakingState() {
        isTakingNotes = !isTakingNotes
        isTakingNotesLiveData.postValue(isTakingNotes)
        val curNotes = if(isTakingNotes) board.getCell(selectedRow, selectedCol).notes else setOf()
        highlightedKeysLiveData.postValue(curNotes)
    }

    /**
     * Función que elimina el valor de la celda seleccionada,
     * o las notas si se está tomando notas
     */
    fun delete() {
        if (selectedRow == -1 || selectedCol == -1) return
        val cell = board.getCell(selectedRow, selectedCol)
        if (!cell.isStartingCell) {
            if (isTakingNotes) {  // 622291380
                cell.notes.clear()
                highlightedKeysLiveData.postValue(setOf())
            } else {
                cell.value = 0
            }
            cellsLiveData.postValue(board.cells)
        }
    }

    /**
     * Función que comprueba si el tablero es correcto
     * @return True si el tablero es correcto, false en caso contrario
     */
    fun checkBoard(): Boolean{
        return board.validate()
    }


    fun reroll() {
        startValues(size * size / 2)
    }

    private fun startValues(subtract: Int) {
        selectedCol = -1
        selectedRow = -1
        isTakingNotes = false
        board = BoardFactory.subtractRandomCells(BoardFactory.createRandomSudoku(size), subtract)
        selectedCellLiveData.postValue(Pair(selectedRow, selectedCol))
        cellsLiveData.postValue(board.cells)
        isTakingNotesLiveData.postValue(isTakingNotes)
    }

    fun reset() {
        board.cells.forEach {
            if (!it.isStartingCell) {
                it.value = 0
                it.notes.clear()
            }
        }
        selectedCol = -1
        selectedRow = -1
        isTakingNotes = false
        selectedCellLiveData.postValue(Pair(selectedRow, selectedCol))
        cellsLiveData.postValue(board.cells)
        isTakingNotesLiveData.postValue(isTakingNotes)
    }
}