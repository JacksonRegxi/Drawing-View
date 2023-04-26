package com.example.paint.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView


class DrawingView(
    context: Context,
    attrs: AttributeSet
) : AppCompatImageView(context, attrs) {

    private var mDrawPath: CustomPath? = null
    private var mCanvasBitmap: Bitmap? = null
    private var mDrawPaint: Paint? = null
    private var mCanvasPaint: Paint? = null
    private var mBrushSize: Int = 0
    private var currentColor = Color.RED
    private var canvas: Canvas? = null
    private var mAlpha: Int = 255
    private var isCanvasBlank = false

    private var mPaths = ArrayList<CustomPath>()
    private var mUndoPath = ArrayList<CustomPath>()

    init {
        setUpDrawing()
    }

    private fun setUpDrawing() {
        mDrawPaint = Paint()
        mDrawPath = CustomPath(currentColor, mBrushSize, mAlpha)
        mDrawPaint!!.color = currentColor
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.alpha = mAlpha
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeWidth = 20F
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
        mBrushSize = 20
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
        isCanvasBlank = true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        for (path in mPaths) {
            mDrawPaint!!.strokeWidth = path.brushThickness.toFloat()
            mDrawPaint!!.color = path.color
            mDrawPaint!!.alpha = path.alpha

            this.canvas?.drawPath(path, mDrawPaint!!)
        }
        if (!mDrawPath!!.isEmpty) {
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness.toFloat()
            mDrawPaint!!.color = mDrawPath!!.color
            mDrawPaint!!.alpha = mDrawPath!!.alpha
            this.canvas?.drawPath(mDrawPath!!, mDrawPaint!!)
        }
    }

    fun setImageFromUri(uri: Uri?, contentResolver: ContentResolver) {
        clearDrawingBoard()
        val tempBitmap = BitmapFactory.decodeStream(
            contentResolver.openInputStream(uri!!)
        )
        val config = tempBitmap.config ?: Bitmap.Config.ARGB_8888
        mCanvasBitmap = Bitmap.createBitmap(
            tempBitmap.width,
            tempBitmap.height,
            config
        )
        canvas = Canvas(mCanvasBitmap!!)
        canvas?.drawBitmap(tempBitmap, 0f, 0f, mDrawPaint)
        setImageBitmap(mCanvasBitmap)
        isCanvasBlank = true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x ?: 0f
        val touchY = event?.y ?: 0f

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.color = currentColor
                mDrawPath!!.brushThickness = mBrushSize
                mDrawPath!!.alpha = mAlpha
                mDrawPath!!.reset()
                mDrawPath!!.moveTo(touchX, touchY)
            }

            MotionEvent.ACTION_MOVE -> {
                mDrawPath!!.lineTo(touchX, touchY)
            }

            MotionEvent.ACTION_UP -> {
                mPaths.add(mDrawPath!!)
                mDrawPath = CustomPath(currentColor, mBrushSize, mAlpha)
            }
        }

        invalidate()

        return true

    }

    /**
     * Helps setting the thickness of brush stroke
     * @param newSize Int 0-200
     */
    @SuppressLint("SupportAnnotationUsage")
    @androidx.annotation.IntRange(from = 0, to = 200)
    fun setSizeForBrush(newSize: Int) {
        mBrushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            newSize.toFloat(), resources.displayMetrics
        ).toInt()
        mDrawPaint!!.strokeWidth = mBrushSize.toFloat()
    }

    fun getBrushSize(): Int {
        return mBrushSize
    }

    /**
     * Helps setting the transparency of brush stroke
     * @param newAlpha Int 0-255
     */
    @SuppressLint("SupportAnnotationUsage")
    @androidx.annotation.IntRange(from = 0, to = 255)
    fun setBrushAlpha(newAlpha: Int) {
        mAlpha = newAlpha
        mDrawPaint!!.alpha = newAlpha
    }

    fun getBrushAlpha(): Int {
        return mAlpha
    }

    /**
     * Helps to set the color of brush
     * @param color Int
     */
    fun setBrushColor(color: Int) {
        currentColor = color
        mDrawPaint!!.color = color
    }

    /**
     * Helps to set the color of brush
     * @return color Int
     */
    fun getBrushColor(): Int {
        return currentColor
    }

    // you can pass a color according to your background and the default is white
    /**
     * Helps to set the color of brush
     * @param color Int default white
     */
    fun erase(colorBackground: Int = Color.WHITE) {
        mAlpha = 255
        mDrawPaint!!.alpha = 255
        currentColor = colorBackground
        mDrawPaint!!.color = colorBackground
    }

    /**
     * will undo strokes, can be changed by redo()
     */
    fun undo() {
        if (mPaths.size > 0) {
            mUndoPath.add(mPaths[mPaths.size - 1])
            mPaths.removeAt(mPaths.size - 1)
            invalidate()
        }
    }

    /**
     * will redo the undo-ed strokes
     */
    fun redo() {
        if (mUndoPath.size > 0) {
            mPaths.add(mUndoPath[mUndoPath.size - 1])
            mUndoPath.removeAt(mUndoPath.size - 1)
            invalidate()
        }

    }

    fun getBitmap(): Bitmap = Bitmap.createScaledBitmap(mCanvasBitmap!!, mCanvasBitmap?.width!!, mCanvasBitmap?.height!!, true)

    /**
     * will remove all the stores but not those saved in redo()
     */
    fun clearDrawingBoard() {
        mPaths.clear()
        invalidate()
    }

    fun getDrawing(): ArrayList<CustomPath> {
        return mPaths
    }

    inner class CustomPath(var color: Int, var brushThickness: Int, var alpha: Int) : Path() {


    }
}