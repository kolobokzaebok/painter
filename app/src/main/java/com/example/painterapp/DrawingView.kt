package com.example.painterapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attributes: AttributeSet) : View(context, attributes) {

    private var mDrawPath: CustomPath? = null
    private var mBitMap: Bitmap? = null
    private var mDrawPaint: Paint? = null
    private var mCanvasPaint: Paint? = null
    private var mBrushSize: Float = 0.toFloat()
    private var mColor: Int = Color.BLACK
    private var mCanvas: Canvas? = null
    private val mPaths = ArrayList<CustomPath>()

    init {
        setUpDrawing()
    }

    fun setBrushSize(brushSize: Float) {
        mBrushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            brushSize,
            resources.displayMetrics
        )
        mDrawPaint!!.strokeWidth = mBrushSize
    }

    fun setBrushColor(brushColor: String) {
        mColor = Color.parseColor(brushColor)
        mDrawPaint!!.color = mColor
    }

    private fun setUpDrawing() {
        mDrawPath = CustomPath(mColor, mBrushSize)
        mDrawPaint = Paint()
        mDrawPaint!!.color = mColor
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mBitMap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitMap!!)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(mBitMap!!, 0f, 0f, mCanvasPaint)

        for (path in mPaths) {
            mDrawPaint!!.strokeWidth = path.brushThickness
            mDrawPaint!!.color = path.color
            canvas?.drawPath(path, mDrawPaint!!)
        }

        if (!mDrawPath!!.isEmpty) {
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            mDrawPaint!!.color = mDrawPath!!.color
            canvas?.drawPath(mDrawPath!!, mDrawPaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX: Float? = event?.x
        val touchY: Float? = event?.y

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.color = mColor
                mDrawPath!!.brushThickness = mBrushSize
                mDrawPath!!.reset()
                mDrawPath!!.moveTo(touchX!!, touchY!!)
            }
            MotionEvent.ACTION_MOVE -> {
                mDrawPath!!.lineTo(touchX!!, touchY!!)
            }
            MotionEvent.ACTION_UP -> {
                mPaths.add(mDrawPath!!)
                mDrawPath = CustomPath(mColor, mBrushSize)
            }
            else -> return false
        }

        invalidate()

        return true
    }

    fun undo() {
        this.mPaths.removeLast()
        invalidate()
    }

    inner class CustomPath(var color: Int, var brushThickness: Float) : Path() {

    }

}