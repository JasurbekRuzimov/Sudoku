package uz.jasurbekruzimov.sudoku.view.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import uz.jasurbekruzimov.sudoku.R
import uz.jasurbekruzimov.sudoku.game.Cell
import kotlin.math.sqrt

class SudokuBoardView(context: Context, attributeView: AttributeSet): View(context, attributeView){
    private  var sqrtSize = 3
    private var size = 9

    private var cellSizePixels = 0F
    private var noteSize = 0F

    private var selectedRow = 0
    private var selectedCol = 0

    private var listener: OnTouchListener? = null

    private var cells: List<Cell>? = null

    /**
     * Línea gruesa
     */
    private val thickLinePath = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth = 6F
    }

    /**
     * Línea fina
     */
    private val thinLinePath = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth = 2F
    }

    /**
     * Celda seleccionada
     */
    private val selectedCellPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = context.getColor(R.color.primary)
    }

    /**
     * Celda en conflicto
     */
    private val conflictingCellPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = context.getColor(R.color.danger)
    }

    /**
     * Texto normal
     */
    private val textPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = context.getColor(R.color.light)
    }

    /**
     * Texto de celda inicial
     */
    private val startingCellTextPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = context.getColor(R.color.light)
        typeface = Typeface.DEFAULT_BOLD
    }

    /**
     * Celda inicial
     */
    private val startingCellPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = context.getColor(R.color.info)
    }

    /**
     * Texto de las notas
     */
    private val noteTextPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = context.getColor(R.color.light)
    }

    /**
     * Celda no seleccionada
     */
    private val unselectedCellPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = context.getColor(R.color.dark)
    }

    /**
     * Función que se ejecuta cuando se cambia el tamaño de la vista
     * @param widthMeasureSpec Ancho de la vista
     * @param heightMeasureSpec Alto de la vista
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val sizePixels = widthMeasureSpec.coerceAtMost(heightMeasureSpec)
        setMeasuredDimension(sizePixels, sizePixels)
    }

    /**
     * Función que se ejecuta cuando se dibuja la vista
     * @param canvas Canvas donde se dibuja la vista
     */
    override fun onDraw(canvas: Canvas) {
        updateMeasurements(width)
        fillCells(canvas)
        drawLines(canvas)
        drawText(canvas)
    }

    /**
     * Función que actualiza las medidas de la vista
     * @param width Ancho de la vista
     */
    private fun updateMeasurements(width: Int) {
        cellSizePixels = (width / size).toFloat()
        noteSize = cellSizePixels / sqrtSize.toFloat()
        noteTextPaint.textSize = cellSizePixels / sqrtSize.toFloat()
        textPaint.textSize = cellSizePixels / 1.5F
        startingCellTextPaint.textSize = cellSizePixels / 1.5F
    }

    /**
     * Función que rellena las celdas.
     * @param canvas Canvas donde se dibuja la vista
     * @see fillCell
     */
    private fun fillCells(canvas: Canvas) {
        if(selectedCol == -1 || selectedRow == -1) return
        cells?.forEach {
            val r = it.row
            val c = it.col
            fillCell(
                canvas,
                r,
                c,
                when{
                    it.isStartingCell -> startingCellPaint
                    r == selectedRow && c == selectedCol -> selectedCellPaint
                    r == selectedRow || c == selectedCol -> conflictingCellPaint
                    r / sqrtSize == selectedRow / sqrtSize && c / sqrtSize == selectedCol / sqrtSize -> conflictingCellPaint
                    else -> unselectedCellPaint
                }
            )
        }
    }

    /**
     * Función que rellena una celda.
     * @param canvas Canvas donde se dibuja la vista
     * @param r Fila de la celda
     * @param c Columna de la celda
     * @param selectedCellPaint Paint que se usa para rellenar la celda
     */
    private fun fillCell(canvas: Canvas, r: Int, c: Int, selectedCellPaint: Paint) {
        canvas.drawRect(c * cellSizePixels,
            r * cellSizePixels,
            (c + 1) * cellSizePixels,
            (r + 1) * cellSizePixels,
            selectedCellPaint
        )
    }

    /**
     * Función que dibuja las líneas de la vista.
     * @param canvas Canvas donde se dibuja la vista
     * @see thickLinePath
     * @see thinLinePath
     */
    private fun drawLines(canvas: Canvas){
        canvas.drawRect(0F, 0F, width.toFloat(), height.toFloat(), thickLinePath)

        for(i in 1 until size){
            val paintToUse = when(i % sqrtSize){
                0 -> thickLinePath
                else -> thinLinePath
            }

            canvas.drawLine(
                i * cellSizePixels,
                0F,
                i * cellSizePixels,
                height.toFloat(),
                paintToUse
            )
            canvas.drawLine(
                0F,
                i * cellSizePixels,
                width.toFloat(),
                i * cellSizePixels,
                paintToUse
            )
        }
    }

    /**
     * Función que dibuja el texto de la vista.
     * @param canvas Canvas donde se dibuja la vista
     * @see writeNotes
     */
    private fun drawText(canvas: Canvas){
        cells?.forEach {
            val value = it.value
            val textBounds = Rect()

            if(value == 0){
                writeNotes(it, textBounds, canvas)
            }else{
                val valueString = it.value.toString()
                val paintToUse = if(it.isStartingCell) startingCellTextPaint else textPaint
                paintToUse.getTextBounds(valueString, 0, valueString.length, textBounds)
                val textWidth = paintToUse.measureText(valueString)
                val textHeight = textBounds.height()

                canvas.drawText(
                    valueString,
                    (it.col * cellSizePixels) + cellSizePixels / 2 - textWidth / 2,
                    (it.row * cellSizePixels) + cellSizePixels / 2 + textHeight / 2,
                    paintToUse
                )
            }
        }
    }

    /**
     * Función que escribe las notas de una celda.
     * @param cell Celda donde se escriben las notas
     * @param textBounds Rectángulo que contiene el texto
     * @param canvas Canvas donde se dibuja la vista
     * @see drawText
     * @see noteTextPaint
     */
    private fun writeNotes(
        cell: Cell,
        textBounds: Rect,
        canvas: Canvas
    ) {
        cell.notes.forEach { note ->
            val valueString = note.toString()
            val rowInCell = (note - 1) / sqrtSize
            val colInCell = (note - 1) % sqrtSize
            noteTextPaint.getTextBounds(note.toString(), 0, note.toString().length, textBounds)
            val textWidth = noteTextPaint.measureText(valueString)
            val textHeight = textBounds.height()

            canvas.drawText(
                valueString,
                (cell.col * cellSizePixels) + (colInCell * noteSize) + noteSize / 2 - textWidth / 2,
                (cell.row * cellSizePixels) + (rowInCell * noteSize) + noteSize / 2 + textHeight / 2,
                noteTextPaint
            )
        }
    }

    /**
     * Función que se ejecuta cuando se toca la vista.
     * @param event Evento de la vista
     * @see handleTouchEvent
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when(event.action){
            MotionEvent.ACTION_DOWN -> {
                handleTouchEvent(event.x, event.y)
                true
            }
            else -> false
        }
    }

    /**
     * Función que maneja el evento de tocar la vista.
     * @param x Coordenada x del evento
     * @param y Coordenada y del evento
     */
    private fun handleTouchEvent(x: Float, y: Float) {
        val possibleSelectedRow = (y / cellSizePixels).toInt()
        val possibleSelectedCol = (x / cellSizePixels).toInt()
        listener?.onCellTouched(possibleSelectedRow, possibleSelectedCol)
    }

    /**
     * Función que actualiza la celda seleccionada.
     * @param row Fila de la celda
     * @param col Columna de la celda
     */
    fun updateSelectedCellUI(row: Int, col: Int) {
        selectedRow = row
        selectedCol = col
        invalidate()
    }

    /**
     * Función que actualiza las celdas.
     * @param cells Lista de celdas
     */
    fun updateCells(cells: List<Cell>) {
        this.cells = cells
        invalidate()
    }

    /**
     * Función que registra un listener.
     * @param listener Listener que se registra
     */
    fun registerListener(listener: OnTouchListener){
        this.listener = listener
    }

    /**
     * Interfaz que implementa el patron listener.
     */
    interface OnTouchListener{
        fun onCellTouched(row: Int, col: Int)
    }
}