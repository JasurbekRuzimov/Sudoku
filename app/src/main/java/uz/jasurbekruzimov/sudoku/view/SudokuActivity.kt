package uz.jasurbekruzimov.sudoku.view

import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import uz.jasurbekruzimov.sudoku.R
import uz.jasurbekruzimov.sudoku.databinding.ActivitySudokuBinding
import uz.jasurbekruzimov.sudoku.game.Cell
import uz.jasurbekruzimov.sudoku.sudoku.viewmodel.PlaySudokuViewModel
import uz.jasurbekruzimov.sudoku.view.custom.SudokuBoardView


class SudokuActivity : AppCompatActivity(), SudokuBoardView.OnTouchListener {

    private lateinit var viewModel : PlaySudokuViewModel
    private lateinit var sudokuBoardView: SudokuBoardView

    private lateinit var numberButtons: List<Button>

    private lateinit var binding: ActivitySudokuBinding
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySudokuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        numberButtons = listOf(binding.oneButton, binding.twoButton, binding.threeButton, binding.fourButton, binding.fiveButton, binding.sixButton, binding.sevenButton, binding.eightButton, binding.nineButton)

        sudokuBoardView = findViewById(R.id.sudokuBoardView)

        sudokuBoardView.registerListener(this)

        viewModel = androidx.lifecycle.ViewModelProvider(this)[PlaySudokuViewModel::class.java]

        viewModel.sudokuGame.selectedCellLiveData.observe(this) { updateSelectedCellUI(it) }
        viewModel.sudokuGame.cellsLiveData.observe(this) { updateCells(it) }
        viewModel.sudokuGame.isTakingNotesLiveData.observe(this) { updateNoteTakingUI(it) }
        viewModel.sudokuGame.highlightedKeysLiveData.observe(this) { updateHighlightedKeys(it, viewModel.sudokuGame.isTakingNotesLiveData.value) }

        numberButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                viewModel.sudokuGame.handleInput(index + 1)
            }
        }

        binding.notesButton.setOnClickListener{ viewModel.sudokuGame.changeNoteTakingState() }
        binding.deleteButton.setOnClickListener{ viewModel.sudokuGame.delete() }
        binding.buttonValidate.setOnClickListener { validate() }
        binding.buttonReroll.setOnClickListener { viewModel.sudokuGame.reroll() }
        binding.buttonReset.setOnClickListener { viewModel.sudokuGame.reset() }
    }


    /**
     * Función que se encarga de mostrar un mensaje al usuario indicando si ha ganado o no
     */
    private fun validate() {
        val result = if(viewModel.sudokuGame.checkBoard()) "Siz g'alaba qozondingiz!" else "Bu to'g'ri emas, harakat qilib ko'ring  "
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
    }

    /**
     * Función que se encarga de actualizar el color de los botones del teclado cuando ponen o quitan notas
     */
    private fun updateHighlightedKeys(set: Set<Int>?, isNotTaking: Boolean?) {
        Log.i("Sudoku", "updateHighlightedKeys: $set")
        numberButtons.forEachIndexed { index, button ->
            button.setBackgroundColor(
                if (set?.contains(index + 1) == true)
                    getColor(R.color.success) // Nota puesta
                else{
                    if (isNotTaking == true)
                        getColor(R.color.info) // Nota no puesta
                    else
                        getColor(R.color.secondary) // Nota quitada
                }
            )
        }
    }

    /**
     * Función que se encarga de actualizar el color del botón de notas
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun updateNoteTakingUI(isNotTaking: Boolean?) = isNotTaking?.let {
        val color = if (it) getColor(R.color.secondary) else getColor(R.color.info)
        binding.notesButton.colorFilter = BlendModeColorFilter(color, BlendMode.MULTIPLY)
    }


    private fun updateCells(cells: List<Cell>?) = cells?.let {
        sudokuBoardView.updateCells(cells)
    }

    private fun updateSelectedCellUI(cell: Pair<Int, Int>?) = cell?.let {
        sudokuBoardView.updateSelectedCellUI(cell.first, cell.second)
    }

    override fun onCellTouched(row: Int, col: Int) {
        viewModel.sudokuGame.updateSelectedCell(row, col)
    }
}