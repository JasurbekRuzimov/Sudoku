package uz.jasurbekruzimov.sudoku.sudoku.viewmodel

import androidx.lifecycle.ViewModel
import uz.jasurbekruzimov.sudoku.game.SudokuGame

class PlaySudokuViewModel: ViewModel() {
    val sudokuGame = SudokuGame()
}